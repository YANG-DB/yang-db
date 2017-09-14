package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class SingularEdgeSourceSearchAppender implements SearchAppender<VertexControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = SchemaUtil.getRelevantSingularEdgeSchemas(context);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        searchBuilder.getIncludeSourceFields().add(edgeSchema.getSource().get().getIdField());
        searchBuilder.getIncludeSourceFields().add(edgeSchema.getDestination().get().getIdField());
        return true;
    }
    //endregion
}
