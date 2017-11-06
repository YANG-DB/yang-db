package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Set;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End endSchema = edgeSchema.getDirection().isPresent() ?
                edgeSchema.getSource().get() :
                context.getDirection().equals(Direction.OUT) ?
                    edgeSchema.getSource().get() :
                    edgeSchema.getDestination().get();

        if (endSchema.getRouting().isPresent()) {
            Set<String> routingValues =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    endSchema.getRouting().get().getRoutingProperty().getName().equals("_id") ?
                                            T.id.getAccessor() :
                                            endSchema.getRouting().get().getRoutingProperty().getName()))
                            .toJavaSet();

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion
}
