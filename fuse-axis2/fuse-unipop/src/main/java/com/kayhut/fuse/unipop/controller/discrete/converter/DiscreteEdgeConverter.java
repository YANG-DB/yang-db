package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.map.MapHelper;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.search.SearchHit;

import java.util.*;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteEdgeConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public DiscreteEdgeConverter(VertexControllerContext context) {
        this.context = context;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public Iterable<E> convert(SearchHit searchHit) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        Vertex outV = null;
        Vertex inV = null;

        List<E> edges = new ArrayList<>();

        if (context.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDestination().get();

            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, searchHit.sourceAsMap());
            Map<String, Object> edgeProperties = createEdgeProperties(inEndSchema, searchHit.sourceAsMap(), inVertexProperties);

            Iterable<Object> outIds = getIdFieldValues(searchHit, searchHit.sourceAsMap(), outEndSchema.getIdField());
            Iterable<Object> inIds = getIdFieldValues(searchHit, searchHit.sourceAsMap(), inEndSchema.getIdField());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    outV = context.getVertex(outId);
                    inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);

                    edges.add((E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), edgeProperties));
                }
            }

        } else {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getDestination().get() : edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getSource().get() : edgeSchema.getDestination().get();

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, searchHit.sourceAsMap());
            Map<String, Object> edgeProperties = createEdgeProperties(outEndSchema, searchHit.sourceAsMap(), outVertexProperties);

            Iterable<Object> outIds = getIdFieldValues(searchHit, searchHit.sourceAsMap(), outEndSchema.getIdField());
            Iterable<Object> inIds = getIdFieldValues(searchHit, searchHit.sourceAsMap(), inEndSchema.getIdField());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    inV = context.getVertex(inId);
                    outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);

                    edges.add((E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), edgeProperties));
                }
            }
        }

        return edges;
    }
    //endregion

    //region Private Methods
    private Iterable<Object> getIdFieldValues(SearchHit searchHit, Map<String, Object> properties, String idField) {
        if (idField.equals("_id")) {
            return Collections.singletonList(searchHit.id());
        } else {
            return MapHelper.values(properties, idField);
        }
    }

    private Map<String, Object> createVertexProperties(GraphEdgeSchema.End endSchema, Map<String, Object> properties) {
        Optional<String> partitionField = endSchema.getIndexPartitions().isPresent() ?
                endSchema.getIndexPartitions().get().getPartitionField() :
                Optional.empty();

        Optional<String> routingField = endSchema.getRouting().isPresent() ?
                Optional.of(endSchema.getRouting().get().getRoutingProperty().getName()) :
                Optional.empty();

        return Stream.ofAll(endSchema.getRedundantProperties())
                .map(GraphRedundantPropertySchema::getPropertyRedundantName)
                .appendAll(partitionField.map(Collections::singletonList).orElseGet(Collections::emptyList))
                .appendAll(routingField.map(Collections::singletonList).orElseGet(Collections::emptyList))
                .distinct()
                .filter(properties::containsKey)
                .toJavaMap(fieldName -> new Tuple2<>(fieldName, properties.get(fieldName)));
    }

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema.End endSchema, Map<String, Object> hitProperties, Map<String, Object> vertexProperties) {
        return Stream.ofAll(hitProperties.entrySet())
                .filter(entry -> !vertexProperties.containsKey(entry.getKey()))
                .toJavaMap(entry -> new Tuple2<>(entry.getKey(), entry.getValue()));
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    //endregion
}
