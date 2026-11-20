package com.yangdb.fuse.unipop.controller.discrete.converter;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.common.context.VertexControllerContext;
import com.yangdb.fuse.unipop.controller.common.converter.DataItem;
import com.yangdb.fuse.unipop.controller.common.converter.ElementConverter;
import com.yangdb.fuse.unipop.controller.common.converter.SearchHitDataItem;
import com.yangdb.fuse.unipop.controller.utils.idProvider.EdgeIdProvider;
import com.yangdb.fuse.unipop.controller.utils.idProvider.SimpleEdgeIdProvider;
import com.yangdb.fuse.unipop.controller.utils.map.MapHelper;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalHasStepFinder;
import com.yangdb.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.yangdb.fuse.unipop.structure.discrete.DiscreteEdge;
import com.yangdb.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.unipop.process.Profiler;
import org.unipop.process.predicate.DistinctFilterP;

import java.util.*;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.unipop.controller.common.appender.EdgeUtils.getLabel;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdgeConverter<E extends Element> implements ElementConverter<DataItem, E> {
    //region Constructors
    public DiscreteEdgeConverter(VertexControllerContext context, Profiler profiler) {
        this.context = context;
        // only for edges without a document of their own (see edgeId); delimited, unlike HashEdgeIdProvider's label+outId+inId
        this.edgeIdProvider = new SimpleEdgeIdProvider();

        //currently assuming a single vertex label in bulk
        this.contextVertexLabel = getLabel(context, "?");

        Set<String> labels = this.context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(this.context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        this.contextEdgeLabel = Stream.ofAll(labels).get(0);

        this.profiler = profiler;
        context.getStepDescriptor().getDescription()
                .ifPresent(v -> this.profiler.get().setCount(v, 0));
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<E> convert(DataItem dataItem) {
        Map<String, Object> dataItemProperties = dataItem.properties();

        List<GraphEdgeSchema> edgeSchemas = Stream.ofAll(context.getSchemaProvider()
                .getEdgeSchemas(this.contextVertexLabel, context.getDirection(), this.contextEdgeLabel)).toJavaList();
        if (edgeSchemas.isEmpty()) {
            return null;
        }

        // undirected step: each document records which way its relationship points, use that direction's schemas
        if (context.getDirection().equals(Direction.BOTH)) {
            Optional<Direction> storedDirection = storedDirection(edgeSchemas.get(0), dataItemProperties);
            if (storedDirection.isPresent()) {
                edgeSchemas = Stream.ofAll(context.getSchemaProvider()
                        .getEdgeSchemas(this.contextVertexLabel, storedDirection.get(), this.contextEdgeLabel)).toJavaList();
            }
        }

        // a relation may connect several type pairs (Own: Person->Dragon, Person->Horse): pick the schema of the
        // pair this document was stored for; a document of another pair (e.g. a same-id entity of another type) is skipped
        Optional<GraphEdgeSchema> matchingSchema = Stream.ofAll(edgeSchemas)
                .find(schema -> matchesStoredTypes(schema, dataItemProperties)).toJavaOptional();
        if (!matchingSchema.isPresent()) {
            return Collections.emptyList();
        }
        GraphEdgeSchema edgeSchema = matchingSchema.get();

        Vertex outV = null;
        Vertex inV = null;

        Collection<E> edges = new ArrayList<>();

        if (edgeSchema.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndA().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndB().get();


            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, dataItemProperties);
            Map<String, Object> edgeProperties = createEdgeProperties(edgeSchema, dataItemProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for (Object outId : outIds) {
                for (Object inId : inIds) {
                    outV = context.getVertex(outId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (outV == null) {
                        continue;
                    }

                    inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);

                    edges.add((E) new DiscreteEdge(
                            edgeId(dataItem, edgeSchema, outV, inV, edgeProperties),
                            edgeSchema.getLabel(),
                            outV,
                            inV,
                            inV,
                            context.getGraph(),
                            edgeProperties));
                }
            }

        } else {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndB().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndA().get();

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, dataItemProperties);
            Map<String, Object> edgeProperties = createEdgeProperties(edgeSchema, dataItemProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for (Object outId : outIds) {
                for (Object inId : inIds) {
                    inV = context.getVertex(inId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (inV == null) {
                        continue;
                    }

                    outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);

                    edges.add((E) new DiscreteEdge(
                            edgeId(dataItem, edgeSchema, outV, inV, edgeProperties),
                            edgeSchema.getLabel(),
                            outV,
                            inV,
                            outV,
                            context.getGraph(),
                            edgeProperties));
                }
            }
        }
        String stepName = context.getStepDescriptor().getDescription().orElse(contextEdgeLabel);
        profiler.get().incrementCount(stepName, 1);

        return edges;
    }

    //endregion

    //region Private Methods

    /**
     * The id of a relationship read from an edge document is the document id without its direction suffix
     * ({@code <id>.out} / {@code <id>.in}), so it is unique per stored relationship and the same from both ends.
     * Edges built without a document of their own (from redundant fields on a vertex) fall back to a delimited
     * {@code label.outId.inId}.
     */
    private String edgeId(DataItem dataItem, GraphEdgeSchema edgeSchema, Vertex outV, Vertex inV, Map<String, Object> edgeProperties) {
        if (!(dataItem instanceof SearchHitDataItem) || dataItem.id() == null) {
            return this.edgeIdProvider.get(edgeSchema.getLabel(), outV, inV, edgeProperties);
        }

        String documentId = dataItem.id().toString();
        if (edgeSchema.getDirectionSchema().isPresent()) {
            for (Object directionValue : Arrays.asList(
                    edgeSchema.getDirectionSchema().get().getOutValue(),
                    edgeSchema.getDirectionSchema().get().getInValue())) {
                String suffix = "." + directionValue;
                if (documentId.endsWith(suffix)) {
                    documentId = documentId.substring(0, documentId.length() - suffix.length());
                    break;
                }
            }
        }
        return edgeSchema.getLabel() + ":" + documentId;
    }

    /** the direction the document's relationship was stored with, if the schema and document record it */
    private Optional<Direction> storedDirection(GraphEdgeSchema edgeSchema, Map<String, Object> properties) {
        if (!edgeSchema.getDirectionSchema().isPresent()) {
            return Optional.empty();
        }
        GraphEdgeSchema.DirectionSchema directionSchema = edgeSchema.getDirectionSchema().get();
        Object value = properties.get(directionSchema.getField());
        if (value == null) {
            return Optional.empty();
        }
        if (value.toString().equals(directionSchema.getOutValue().toString())) {
            return Optional.of(Direction.OUT);
        }
        if (value.toString().equals(directionSchema.getInValue().toString())) {
            return Optional.of(Direction.IN);
        }
        return Optional.empty();
    }

    /**
     * true unless the document stores an endpoint type that differs from the schema's end label. End A is always the
     * side of the vertex being expanded, end B the other side.
     */
    private boolean matchesStoredTypes(GraphEdgeSchema edgeSchema, Map<String, Object> properties) {
        return matchesStoredType(edgeSchema.getEndA(), properties) && matchesStoredType(edgeSchema.getEndB(), properties);
    }

    private boolean matchesStoredType(Optional<GraphEdgeSchema.End> end, Map<String, Object> properties) {
        if (!end.isPresent() || !end.get().getLabel().isPresent()) {
            return true;
        }
        Optional<String> storedType = storedType(end.get(), properties);
        return !storedType.isPresent() || storedType.get().equals(end.get().getLabel().get());
    }

    /** the stored type of an end, read from the {@code <side>.type} field next to its {@code <side>.id} field */
    private Optional<String> storedType(GraphEdgeSchema.End end, Map<String, Object> properties) {
        List<String> idFields = Stream.ofAll(end.getIdFields()).toJavaList();
        if (idFields.size() != 1 || !idFields.get(0).endsWith("." + GlobalConstants.ID)) {
            return Optional.empty();
        }
        String idField = idFields.get(0);
        String typeField = idField.substring(0, idField.length() - GlobalConstants.ID.length()) + GlobalConstants.TYPE;
        List<Object> values = MapHelper.values(properties, typeField);
        return values.isEmpty() || values.get(0) == null ? Optional.empty() : Optional.of(values.get(0).toString());
    }

    private Iterable<Object> getIdFieldValues(DataItem dataItem, Iterable<String> idFields) {
        List<Object> idFieldValues = Collections.emptyList();
        boolean isfirst = true;
        boolean isSecond = false;
        for (String idField : idFields) {
            if (isfirst) {
                idFieldValues = getIdFieldValues(dataItem, idField);
                isfirst = false;
                isSecond = true;
            } else if (isSecond) {
                idFieldValues = new ArrayList<>(idFieldValues);
                idFieldValues.addAll(getIdFieldValues(dataItem, idField));
                isSecond = false;
            } else {
                idFieldValues.addAll(getIdFieldValues(dataItem, idField));
            }
        }

        return idFieldValues;
    }

    private List<Object> getIdFieldValues(DataItem dataItem, String idField) {
        if (idField.equals(GlobalConstants._ID)) {
            return Collections.singletonList(dataItem.id());
        } else {
            return MapHelper.values(dataItem.properties(), idField);
        }
    }

    private Map<String, Object> createVertexProperties(GraphEdgeSchema.End endSchema, Map<String, Object> properties) {
        Optional<GraphRedundantPropertySchema> partitionField = endSchema.getIndexPartitions().isPresent() ?
                Optional.of(new GraphRedundantPropertySchema.Impl(
                        endSchema.getIndexPartitions().get().getPartitionField().get(),
                        endSchema.getIndexPartitions().get().getPartitionField().get(),
                        "string")) :
                Optional.empty();

        Optional<GraphRedundantPropertySchema> routingField = endSchema.getRouting().isPresent() ?
                Optional.of(new GraphRedundantPropertySchema.Impl(
                        endSchema.getRouting().get().getRoutingProperty().getName(),
                        endSchema.getRouting().get().getRoutingProperty().getName(),
                        "string")) :
                Optional.empty();

        Map<String, Object> vertexProperties = new HashMap<>();
        Iterable<GraphRedundantPropertySchema> redundantPropertySchemas = endSchema.getRedundantProperties();

        if (partitionField.isPresent()) {
            Object propertyValue = properties.get(partitionField.get().getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(partitionField.get().getName(), propertyValue);
            }
        }

        if (routingField.isPresent()) {
            Object propertyValue = properties.get(routingField.get().getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(routingField.get().getName(), propertyValue);
            }
        }

        for (GraphRedundantPropertySchema redundantPropertySchema : redundantPropertySchemas) {
            Object propertyValue = properties.get(redundantPropertySchema.getPropertyRedundantName());
            if (propertyValue != null) {
                vertexProperties.put(redundantPropertySchema.getName(), propertyValue);
            }
        }

        return vertexProperties;
    }

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema schema, Map<String, Object> properties) {
        Map<String, Object> edgeProperties = new HashMap<>();
        for (GraphElementPropertySchema property : schema.getProperties()) {
            if (properties.containsKey(property.getName())) {
                edgeProperties.put(property.getName(), properties.get(property.getName()));
            }
        }
        return edgeProperties;
    }
    //endregion

    //endregion
    @Override
    public Profiler getProfiler() {
        return this.profiler;
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.profiler = profiler;
    }

    //region Fields
    private Profiler profiler = Profiler.Noop.instance;
    private VertexControllerContext context;
    private EdgeIdProvider<String> edgeIdProvider;

    private String contextVertexLabel;
    private String contextEdgeLabel;
    //endregion
}
