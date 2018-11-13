package com.kayhut.fuse.unipop.controller.discrete.converter;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.DataItem;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.utils.idProvider.EdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.idProvider.HashEdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.idProvider.SimpleEdgeIdProvider;
import com.kayhut.fuse.unipop.controller.utils.map.MapHelper;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import com.sun.corba.se.spi.ior.ObjectId;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.search.SearchHit;
import org.unipop.process.Profiler;

import java.util.*;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdgeConverter<E extends Element> implements ElementConverter<DataItem, E> {
    //region Constructors
    public DiscreteEdgeConverter(VertexControllerContext context, Profiler profiler) {
        this.context = context;
        try {
            this.edgeIdProvider = new HashEdgeIdProvider(context.getConstraint());
        } catch (Exception e) {
            e.printStackTrace();
            this.edgeIdProvider = new SimpleEdgeIdProvider();
        }

        //currently assuming a single vertex label in bulk
        this.contextVertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        Set<String> labels = this.context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(this.context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        this.contextEdgeLabel = Stream.ofAll(labels).get(0);

        this.profiler = profiler;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<E> convert(DataItem dataItem) {
        Map<String, Object> dataItemProperties = dataItem.properties();

        Iterator<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(this.contextVertexLabel, context.getDirection(), this.contextEdgeLabel).iterator();
        if (!edgeSchemas.hasNext()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = edgeSchemas.next();

        Vertex outV = null;
        Vertex inV = null;

        List<E> edges = new ArrayList<>();

        if (edgeSchema.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndA().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndB().get();


            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, dataItemProperties);
            Map<String, Object> edgeProperties = createEdgeProperties(inEndSchema, dataItemProperties, inVertexProperties);

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
                            this.edgeIdProvider.get(edgeSchema.getLabel(), outV, inV, edgeProperties),
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
            Map<String, Object> edgeProperties = createEdgeProperties(outEndSchema, dataItemProperties, outVertexProperties);

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
                            this.edgeIdProvider.get(edgeSchema.getLabel(), outV, inV, edgeProperties),
                            edgeSchema.getLabel(),
                            outV,
                            inV,
                            outV,
                            context.getGraph(),
                            edgeProperties));
                }
            }
        }

        return edges;
    }
    //endregion

    //region Private Methods
    private Iterable<Object> getIdFieldValues(DataItem dataItem, Iterable<String> idFields) {
        List<Object> idFieldValues = Collections.emptyList();
        boolean isfirst = true;
        boolean isSecond = false;
        for(String idField : idFields) {
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
        if (idField.equals("_id")) {
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

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema.End endSchema, Map<String, Object> properties, Map<String, Object> vertexProperties) {
        Map<String, Object> edgeProperties = Collections.emptyMap();
        boolean isFirst = true;

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!vertexProperties.containsKey(entry.getKey())) {
                if (isFirst) {
                    edgeProperties = new HashMap<>();
                    isFirst = false;
                }

                edgeProperties.put(entry.getKey(), entry.getValue());
            }
        }

        return edgeProperties;
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    private EdgeIdProvider<String> edgeIdProvider;

    private String contextVertexLabel;
    private String contextEdgeLabel;

    private Profiler profiler;
    //endregion
}
