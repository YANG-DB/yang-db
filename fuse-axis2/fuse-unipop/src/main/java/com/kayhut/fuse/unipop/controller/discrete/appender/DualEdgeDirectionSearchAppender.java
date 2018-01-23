package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.EdgeSchemaSupplier;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;

/**
 * Created by roman.margolis on 22/01/2018.
 */
public class DualEdgeDirectionSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = new EdgeSchemaSupplier(context).labels().dual().applicable().get();
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        // currently assuming only one applicable edge schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        if (context.getDirection().equals(Direction.BOTH)) {
            return false;
        }

        Traversal directionConstraint = __.start();
        switch (context.getDirection()) {
            case OUT:
                    directionConstraint = __.has(edgeSchema.getDirection().get().getField(), edgeSchema.getDirection().get().getOutValue());
                break;
            case IN:
                    directionConstraint = __.has(edgeSchema.getDirection().get().getField(), edgeSchema.getDirection().get().getInValue());
                break;
        }

        TraversalQueryTranslator traversalQueryTranslator =
                new TraversalQueryTranslator(queryBuilder.seekRoot().query().filtered().filter().bool().must(), false);
        traversalQueryTranslator.visit(directionConstraint);
        return true;
    }
    //endregion
}
