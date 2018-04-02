package com.kayhut.fuse.unipop.controller.discrete.converter;

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

import java.util.*;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdgeConverter<E extends Element> implements ElementConverter<DataItem, E> {
    //region Constructors
    public DiscreteEdgeConverter(VertexControllerContext context) {
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
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<E> convert(DataItem dataItem) {
        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(this.contextVertexLabel, context.getDirection(), this.contextEdgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        Vertex outV = null;
        Vertex inV = null;

        List<E> edges = new ArrayList<>();

        if (edgeSchema.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getEndA().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getEndB().get();

            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, dataItem.properties());
            Map<String, Object> edgeProperties = createEdgeProperties(inEndSchema, dataItem.properties(), inVertexProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    outV = context.getVertex(outId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (outV == null) {
                        continue;
                    }

                    inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);

                    edges.add((E)new DiscreteEdge(
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

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, dataItem.properties());
            Map<String, Object> edgeProperties = createEdgeProperties(outEndSchema, dataItem.properties(), outVertexProperties);

            Iterable<Object> outIds = getIdFieldValues(dataItem, outEndSchema.getIdFields());
            Iterable<Object> inIds = getIdFieldValues(dataItem, inEndSchema.getIdFields());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    inV = context.getVertex(inId);

                    // could happen in multi value relation fields where the document contains additional values
                    // than those available in the query context
                    if (inV == null) {
                        continue;
                    }

                    outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);

                    edges.add((E)new DiscreteEdge(
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
        return Stream.ofAll(idFields)
                .<Object>flatMap(idField -> {
                    if (idField.equals("_id")) {
                        return Collections.singletonList(dataItem.id());
                    } else {
                        return MapHelper.values(dataItem.properties(), idField);
                    }
                }).toJavaList();
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

        return Stream.ofAll(endSchema.getRedundantProperties())
                .appendAll(partitionField.map(Collections::singletonList).orElseGet(Collections::emptyList))
                .appendAll(routingField.map(Collections::singletonList).orElseGet(Collections::emptyList))
                .filter(property -> properties.containsKey(property.getPropertyRedundantName()))
                .toJavaMap(property -> new Tuple2<>(property.getName(), properties.get(property.getPropertyRedundantName())));
    }

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema.End endSchema, Map<String, Object> hitProperties, Map<String, Object> vertexProperties) {
        return Stream.ofAll(hitProperties.entrySet())
                .filter(entry -> !vertexProperties.containsKey(entry.getKey()))
                .toJavaMap(entry -> new Tuple2<>(entry.getKey(), entry.getValue()));
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    private EdgeIdProvider<String> edgeIdProvider;

    private String contextVertexLabel;
    private String contextEdgeLabel;
    //endregion
}
