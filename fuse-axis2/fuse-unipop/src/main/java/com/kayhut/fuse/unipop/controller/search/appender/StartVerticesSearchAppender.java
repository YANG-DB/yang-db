package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.TraversalQueryTranslator;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elad on 4/26/2017.
 */
public class StartVerticesSearchAppender implements SearchAppender<PromiseVertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext context) {

        Traversal traversal = buildStartVerticesConstraint(context.getStartVertices());
        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter().bool().must();
        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);
        traversalQueryTranslator.visit(traversal);
        return true;

    }

    private Traversal buildStartVerticesConstraint(List<Vertex> vertices) {
        return __.has("entityA.id", P.within(vertices.stream().map(vertex -> vertex.id()).collect(Collectors.toList())));
    }
}
