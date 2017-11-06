package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;

/**
 * Created by roman.margolis on 18/10/2017.
 */
public class EdgeBulkSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region VertexControllerContext Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        GraphEdgeSchema.End endSchema = edgeSchema.getDirection().isPresent() ?
                edgeSchema.getSource().get() :
                context.getDirection().equals(Direction.OUT) ?
                        edgeSchema.getSource().get() :
                        edgeSchema.getDestination().get();

        queryBuilder.seekRoot().query().filtered().filter().bool().must()
                .terms(endSchema.getIdField(),
                        endSchema.getIdField(),
                        Stream.ofAll(context.getBulkVertices()).map(vertex -> vertex.id().toString()).toJavaList());

        return true;
    }
    //endregion
}
