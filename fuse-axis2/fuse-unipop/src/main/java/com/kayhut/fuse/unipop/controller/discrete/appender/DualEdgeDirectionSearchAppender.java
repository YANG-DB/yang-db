package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Set;

/**
 * Created by roman.margolis on 22/01/2018.
 */
public class DualEdgeDirectionSearchAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Set<String> labels = context.getConstraint().isPresent() ?
                new TraversalValuesByKeyProvider().getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor()) :
                Stream.ofAll(context.getSchemaProvider().getEdgeLabels()).toJavaSet();

        //currently assuming a single edge label
        String edgeLabel = Stream.ofAll(labels).get(0);

        //currently assuming a single vertex label in bulk
        String contextVertexLabel = Stream.ofAll(context.getBulkVertices()).get(0).label();


        Iterable<GraphEdgeSchema> edgeSchemas = context.getSchemaProvider().getEdgeSchemas(contextVertexLabel, context.getDirection(), edgeLabel);
        if (Stream.ofAll(edgeSchemas).isEmpty()) {
            return false;
        }

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);
        if (!edgeSchema.getDirectionSchema().isPresent()) {
            return false;
        }

        if (context.getDirection().equals(Direction.BOTH)) {
            return false;
        }

        Traversal directionConstraint = __.start();
        switch (context.getDirection()) {
            case OUT:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getOutValue());
                break;
            case IN:
                    directionConstraint = __.has(edgeSchema.getDirectionSchema().get().getField(), edgeSchema.getDirectionSchema().get().getInValue());
                break;
        }

        TraversalQueryTranslator traversalQueryTranslator =
                new TraversalQueryTranslator(queryBuilder.seekRoot().query().filtered().filter().bool().must(), false);
        traversalQueryTranslator.visit(directionConstraint);
        return true;
    }
    //endregion
}
