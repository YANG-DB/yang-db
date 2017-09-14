package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.common.appender.FilterSourceSearchAppender;
import com.kayhut.fuse.unipop.controller.common.context.SelectContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roman on 24/05/2017.
 */
public class FilterSourceSearchAppenderTest {
    @Test
    public void testSingleSelectPredicate() {
        SearchBuilder searchBuilder = new SearchBuilder();

        SelectContext selectContext = () -> Collections.singleton(new HasContainer("name", SelectP.raw("name")));
        FilterSourceSearchAppender appender = new FilterSourceSearchAppender();
        appender.append(searchBuilder, selectContext);

        Assert.assertEquals(1, searchBuilder.getIncludeSourceFields().size());
        Assert.assertEquals("name", searchBuilder.getIncludeSourceFields().iterator().next());
    }

    @Test
    public void testMultipleSelectPredicate() {
        SearchBuilder searchBuilder = new SearchBuilder();

        SelectContext selectContext = () -> Arrays.asList(
                new HasContainer("name", SelectP.raw("name")),
                new HasContainer("age", SelectP.raw("age")),
                new HasContainer("timestamp", SelectP.raw("timestamp")));
        FilterSourceSearchAppender appender = new FilterSourceSearchAppender();
        appender.append(searchBuilder, selectContext);

        Assert.assertEquals(3, searchBuilder.getIncludeSourceFields().size());

        List<String> sourceFields = Stream.ofAll(searchBuilder.getIncludeSourceFields()).toJavaList();
        Assert.assertEquals("name", sourceFields.get(0));
        Assert.assertEquals("age", sourceFields.get(1));
        Assert.assertEquals("timestamp", sourceFields.get(2));
    }

    @Test
    public void testZeroSelectPredicates() {
        SearchBuilder searchBuilder = new SearchBuilder();

        FilterSourceSearchAppender appender = new FilterSourceSearchAppender();
        appender.append(searchBuilder, Collections::emptyList);

        Assert.assertEquals(0, searchBuilder.getIncludeSourceFields().size());
    }
}
