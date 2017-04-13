package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.context.PromiseElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.schemaProviders.EmptyGraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * Created by User on 29/03/2017.
 */
public class ElementConstraintSearchAppenderTest {
    @Test
    public void testNoConstraint() {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                Collections.emptyList(),
                Optional.empty(),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                mock(SearchQuery.class)));

        Assert.assertTrue(!appendResult);
        Assert.assertTrue(searchBuilder.getQueryBuilder().getQuery() == null);
    }

    @Test
    public void testSimpleConstraint() {

        Traversal dragonTraversal = __.has("label", P.eq("dragon")).limit(100);

        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                Collections.emptyList(),
                Optional.of(Constraint.by(__.has(T.label, "dragon"))),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                mock(SearchQuery.class)));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"term\":{\"_type\":\"dragon\"}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }

    @Test
    public void testComplexConstraint() {
        SearchBuilder searchBuilder = new SearchBuilder();

        ElementConstraintSearchAppender appender = new ElementConstraintSearchAppender();
        boolean appendResult = appender.append(searchBuilder, new PromiseElementControllerContext(
                Collections.emptyList(),
                Optional.of(Constraint.by(__.and(__.has(T.label, "dragon"), __.has("name", "Drogar")))),
                EmptyGraphElementSchemaProvider.instance,
                ElementType.vertex,
                mock(SearchQuery.class)));

        Assert.assertTrue(appendResult);
        JSONAssert.assertEquals(
                "{\"filtered\":{\"query\":{\"match_all\":{}},\"filter\":{\"bool\":{\"must\":{\"bool\":{\"must\":[{\"term\":{\"_type\":\"dragon\"}},{\"term\":{\"name\":\"Drogar\"}}]}}}}}}",
                searchBuilder.getQueryBuilder().getQuery().toString(),
                JSONCompareMode.LENIENT);
    }
}
