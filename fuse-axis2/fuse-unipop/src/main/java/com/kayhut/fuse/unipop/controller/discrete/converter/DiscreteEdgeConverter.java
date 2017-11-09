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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        if (context.getDirection().equals(Direction.OUT)) {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDestination().get();

            Object outId = getIdFieldValue(searchHit, properties, outEndSchema.getIdField());
            Object inId = getIdFieldValue(searchHit, properties, inEndSchema.getIdField());

            outV = context.getVertex(outId);

            Map<String, Object> inVertexProperties = createVertexProperties(inEndSchema, properties);
            properties = createEdgeProperties(inEndSchema, properties);
            inV = new DiscreteVertex(inId, inEndSchema.getLabel().get(), context.getGraph(), inVertexProperties);
        } else {
            GraphEdgeSchema.End outEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getDestination().get() : edgeSchema.getSource().get();
            GraphEdgeSchema.End inEndSchema = edgeSchema.getDirection().isPresent() ? edgeSchema.getSource().get() : edgeSchema.getDestination().get();

            Object outId = getIdFieldValue(searchHit, properties, outEndSchema.getIdField());
            Object inId = getIdFieldValue(searchHit, properties, inEndSchema.getIdField());

            inV = context.getVertex(inId);

            Map<String, Object> outVertexProperties = createVertexProperties(outEndSchema, properties);
            properties = createEdgeProperties(outEndSchema, properties);
            outV = new DiscreteVertex(outId, outEndSchema.getLabel().get(), context.getGraph(), outVertexProperties);
        }

        return Arrays.asList((E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), properties));
    }
    //endregion

    //region Private Methods
    private Object getIdFieldValue(SearchHit searchHit, Map<String, Object> properties, String idField) {
        if (idField.equals("_id")) {
            return searchHit.id();
        } else {
            return MapHelper.value(properties, idField).orElse(null);
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
