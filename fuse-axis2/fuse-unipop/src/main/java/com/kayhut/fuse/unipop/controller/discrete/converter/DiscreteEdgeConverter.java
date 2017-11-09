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
        Map<String, Object> properties = new HashMap<>(searchHit.sourceAsMap());

        List<E> edges = new ArrayList<>();

        if (context.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDestination().get();

            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, properties);
            properties = createEdgeProperties(inEndSchema, properties);

            Iterable<Object> outIds = getIdFieldValues(searchHit, properties, outEndSchema.getIdField());
            Iterable<Object> inIds = getIdFieldValues(searchHit, properties, inEndSchema.getIdField());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    outV = context.getVertex(outId);
                    inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);

                    edges.add((E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), properties));
                }
            }

        } else {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getDestination().get() : edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getSource().get() : edgeSchema.getDestination().get();

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, properties);
            properties = createEdgeProperties(outEndSchema, properties);

            Iterable<Object> outIds = getIdFieldValues(searchHit, properties, outEndSchema.getIdField());
            Iterable<Object> inIds = getIdFieldValues(searchHit, properties, inEndSchema.getIdField());

            for(Object outId : outIds) {
                for(Object inId : inIds) {
                    inV = context.getVertex(inId);
                    outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);

                    edges.add((E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), properties));
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
        return Stream.ofAll(endSchema.getRedundantProperties())
                .filter(redundantProperty -> properties.containsKey(redundantProperty.getPropertyRedundantName()))
                .toJavaMap(redundantProperty -> new Tuple2<>(redundantProperty.getName(), properties.get(redundantProperty.getPropertyRedundantName())));
    }

    private Map<String, Object> createEdgeProperties(GraphEdgeSchema.End endSchema, Map<String, Object> properties) {
        for(GraphRedundantPropertySchema redundantPropertySchema : endSchema.getRedundantProperties()) {
            properties.remove(redundantPropertySchema.getPropertyRedundantName());
        }
        return properties;
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    //endregion
}
