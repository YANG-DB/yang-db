package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.ElementUtil;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;
import java.util.Set;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class FilterRoutingSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        // currently assuming homogeneous vertex bulk
        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        Optional<GraphVertexSchema> vertexSchema = context.getSchemaProvider().getVertexSchema(vertexLabel);
        if (!vertexSchema.isPresent()) {
            return false;
        }

        if (vertexSchema.get().getRouting().isPresent()) {
            Set<String> routingValues =
                    Stream.ofAll(context.getBulkVertices())
                            .map(vertex -> ElementUtil.<String>value(vertex,
                                    vertexSchema.get().getRouting().get().getRoutingProperty().getName().equals("_id") ?
                                            T.id.getAccessor() :
                                            vertexSchema.get().getRouting().get().getRoutingProperty().getName()))
                            .toJavaSet();

            searchBuilder.getRouting().addAll(routingValues);
            return routingValues.size() > 0;
        }

        return false;
    }
    //endregion
}
