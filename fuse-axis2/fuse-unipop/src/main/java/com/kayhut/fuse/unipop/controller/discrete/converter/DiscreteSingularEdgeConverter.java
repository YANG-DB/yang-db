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
        if (context.getDirection().equals(Direction.OUT)) {
            outV = context.getVertex(properties.get(edgeSchema.getSource().get().getIdField()));
            inV = new DiscreteVertex(properties.get(edgeSchema.getDestination().get().getIdField()),
                    edgeSchema.getDestination().get().getType().get(),
                    context.getGraph(),
                    null);
        } else {
            inV = context.getVertex(properties.get(edgeSchema.getDestination().get().getIdField()));
            outV = new DiscreteVertex(properties.get(edgeSchema.getSource().get().getIdField()),
                    edgeSchema.getDestination().get().getType().get(),
                    context.getGraph(),
                    null);
        }

        return (E)new DiscreteEdge(searchHit.getId(), edgeSchema.getType(), outV, inV, context.getGraph(), properties);
    }
    //endregion

    //region Fields
    private VertexControllerContext context;
    //endregion
}
