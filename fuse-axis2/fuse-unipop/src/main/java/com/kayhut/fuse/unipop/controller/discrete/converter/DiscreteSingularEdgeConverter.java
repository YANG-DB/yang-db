package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class DiscreteSingularEdgeConverter<E extends Element> implements ElementConverter<SearchHit, E> {
    //region Constructors
    public DiscreteSingularEdgeConverter(VertexControllerContext context) {
        this.context = context;
    }
    //endregion

    //region ElementConverter Implementation
    @Override
    public E convert(SearchHit searchHit) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().singular().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        GraphVertexSchema sourceVertexSchema = context.getSchemaProvider().getVertexSchema(edgeSchema.getSource().get().getLabel().get()).get();
        GraphVertexSchema destVertexSchema = context.getSchemaProvider().getVertexSchema(edgeSchema.getDestination().get().getLabel().get()).get();

        Vertex outV = null;
        Vertex inV = null;
        Map<String, Object> properties = new HashMap<>(searchHit.sourceAsMap());
        Object sourceId = getIdFieldValue(searchHit, properties, edgeSchema.getSource().get().getIdField());
        Object destId = getIdFieldValue(searchHit, properties, edgeSchema.getDestination().get().getIdField());

        if (context.getDirection().equals(Direction.OUT)) {
            outV = context.getVertex(sourceId);
            Map<String, Object> vertexProperties = createVertexProperties(edgeSchema.getDestination().get(), properties);
            properties = createEdgeProperties(edgeSchema.getDestination().get(), properties);
            inV = new DiscreteVertex(destId, edgeSchema.getDestination().get().getLabel().get(), context.getGraph(), vertexProperties);
        } else {
            inV = context.getVertex(destId);
            Map<String, Object> vertexProperties = createVertexProperties(edgeSchema.getSource().get(), properties);
            properties = createEdgeProperties(edgeSchema.getSource().get(), properties);
            outV = new DiscreteVertex(sourceId, edgeSchema.getSource().get().getLabel().get(), context.getGraph(), vertexProperties);
        }

        return (E)new DiscreteEdge(searchHit.getId(), edgeSchema.getLabel(), outV, inV, context.getGraph(), properties);
    }
    //endregion

    //region Private Methods
    private Object getIdFieldValue(SearchHit searchHit, Map<String, Object> properties, String idField) {
        if (idField.equals("_id")) {
            return searchHit.id();
        } else {
            return properties.get(idField);
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
