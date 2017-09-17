package com.kayhut.fuse.unipop.controller.discrete.converter;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.common.converter.ElementConverter;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteEdge;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.elasticsearch.search.SearchHit;

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
        Iterable<GraphEdgeSchema> edgeSchemas = SchemaUtil.getRelevantSingularEdgeSchemas(context);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return null;
        }

        //currently assuming only one relevant edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        Vertex outV = null;
        Vertex inV = null;
        Map<String, Object> properties = searchHit.sourceAsMap();
        Object sourceId = getIdFieldValue(searchHit, properties, edgeSchema.getSource().get().getIdField());
        Object destId = getIdFieldValue(searchHit, properties, edgeSchema.getDestination().get().getIdField());

        if (context.getDirection().equals(Direction.OUT)) {
            outV = context.getVertex(sourceId);
            inV = new DiscreteVertex(destId, edgeSchema.getDestination().get().getLabel().get(), context.getGraph(), null);
        } else {
            inV = context.getVertex(destId);
            outV = new DiscreteVertex(sourceId, edgeSchema.getDestination().get().getLabel().get(), context.getGraph(), null);
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
    //endregion

    //region Fields
    private VertexControllerContext context;
    //endregion
}
