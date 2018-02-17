package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 14/09/2017.
 */
@Deprecated
public class SingularEdgeSourceSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().singular().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        searchBuilder.getIncludeSourceFields().addAll(Stream.ofAll(edgeSchema.getEndA().get().getIdFields()).toJavaList());
        searchBuilder.getIncludeSourceFields().addAll(Stream.ofAll(edgeSchema.getEndB().get().getIdFields()).toJavaList());
        return true;
    }
    //endregion
}
