package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.promise.appender.SearchQueryAppenderBase;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalValuesByKeyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphEdgeSchema;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by roman.margolis on 13/09/2017.
 */
public class SingularEdgeAppender extends SearchQueryAppenderBase<VertexControllerContext> {
    //region VertexControllerContext Implementation
    @Override
    protected boolean append(QueryBuilder queryBuilder, VertexControllerContext context) {
        Iterable<GraphEdgeSchema> edgeSchemas = getRelevantSingularEdgeSchemas(context);

        //currently assuming only one relevant schema
        GraphEdgeSchema edgeSchema = Stream.ofAll(edgeSchemas).get(0);

        String vertexLabel = context.getBulkVertices().get(0).label();
        if (edgeSchema.getSource().get().getType().get().equals(vertexLabel)) {
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

    //region Private Methods
    private Iterable<GraphEdgeSchema> getEdgeSchemas(VertexControllerContext context) {
        Set<String> types = Collections.emptySet();
        if (context.getConstraint().isPresent()) {
            TraversalValuesByKeyProvider traversalValuesByKeyProvider = new TraversalValuesByKeyProvider();
            types = traversalValuesByKeyProvider.getValueByKey(context.getConstraint().get().getTraversal(), T.label.getAccessor());
        }

        if (types.isEmpty()) {
            types = Stream.ofAll(context.getSchemaProvider().getEdgeTypes()).toJavaSet();
        }

        return Stream.ofAll(types)
                .flatMap(type -> context.getSchemaProvider().getEdgeSchemas(type))
                .distinct()
                .toJavaList();
    }

    private Iterable<GraphEdgeSchema> getSingularEdgeSchemas(VertexControllerContext context) {
        return Stream.ofAll(getEdgeSchemas(context))
                .filter(edgeSchema -> !edgeSchema.getDirection().isPresent())
                .toJavaList();
    }

    private Iterable<GraphEdgeSchema> getRelevantSingularEdgeSchemas(VertexControllerContext context) {
        //currently assuming all bulk vertices of same type
        String vertexLabel = context.getBulkVertices().get(0).label();

        return Stream.ofAll(getSingularEdgeSchemas(context))
                .filter(edgeSchema -> (edgeSchema.getSource().get().getType().get().equals(vertexLabel) &&
                        (context.getDirection().equals(Direction.OUT) || context.getDirection().equals(Direction.BOTH))) ||
                        (edgeSchema.getDestination().get().getType().get().equals(vertexLabel) &&
                                (context.getDirection().equals(Direction.IN) || context.getDirection().equals(Direction.BOTH))))
                .toJavaList();
    }
    //endregion

}
