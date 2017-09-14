package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.controller.promise.context.PromiseVertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.utils.traversal.TraversalQueryTranslator;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * Created by Elad on 4/26/2017.
 */
public class StartVerticesSearchAppender implements SearchAppender<PromiseVertexControllerContext> {

    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseVertexControllerContext context) {

        Traversal traversal = buildStartVerticesConstraint(context.getBulkVertices());

        QueryBuilder queryBuilder = searchBuilder.getQueryBuilder().seekRoot().query().filtered().filter().bool().must();

        TraversalQueryTranslator traversalQueryTranslator = new TraversalQueryTranslator(queryBuilder, false);

        traversalQueryTranslator.visit(traversal);

        return true;

    }

    private Traversal buildStartVerticesConstraint(Iterable<Vertex> vertices) {
        return __.has(GlobalConstants.EdgeSchema.SOURCE_ID, P.within(Stream.ofAll(vertices).map(vertex -> vertex.id()).toJavaList()));
    }

}
