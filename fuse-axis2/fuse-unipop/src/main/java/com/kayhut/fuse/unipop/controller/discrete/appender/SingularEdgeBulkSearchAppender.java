package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 13/09/2017.
 */
@Deprecated
public class SingularEdgeBulkSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region VertexControllerContext Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().singular().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);


        String vertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();

        GraphEdgeSchema.End endSchema = edgeSchema.getEndA().get().getLabel().get().equals(vertexLabel) ?
                edgeSchema.getEndA().get() :
                edgeSchema.getEndB().get();

        String idField = Stream.ofAll(endSchema.getIdFields()).get(0);

        // currently, taking the first id field for query
        // TODO: add support for querying multiple id fields
        queryBuilder.seekRoot().query().filtered().filter().bool().must()
                .terms(idField,
                        idField,
                        Stream.ofAll(context.getBulkVertices()).map(vertex -> vertex.id().toString()).toJavaList());

        return true;
    }
    //endregion
}
