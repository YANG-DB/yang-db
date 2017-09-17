package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.discrete.util.SchemaUtil;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class SingularEdgeAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region VertexControllerContext Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = SchemaUtil.getRelevantSingularEdgeSchemas(context);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();
        if (edgeSchema.getSource().get().getLabel().get().equals(vertexLabel)) {
            queryBuilder.seekRoot().query().filtered().filter().bool().must()
                    .terms(edgeSchema.getSource().get().getIdField(),
                            edgeSchema.getSource().get().getIdField(),
                            Stream.ofAll(context.getBulkVertices()).map(vertex -> vertex.id().toString()).toJavaList());
        } else {
            queryBuilder.seekRoot().query().filtered().filter().bool().must()
                    .terms(edgeSchema.getDestination().get().getIdField(),
                            edgeSchema.getDestination().get().getIdField(),
                            Stream.ofAll(context.getBulkVertices()).map(vertex -> vertex.id().toString()).toJavaList());
        }

        return true;
    }
    //endregion
}
