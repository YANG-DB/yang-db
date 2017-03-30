package com.kayhut.fuse.unipop.search.appender;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.CompositeSearchAppender;
import com.kayhut.fuse.unipop.controller.search.appender.SearchAppender;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import static org.mockito.Matchers.booleanThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by User on 29/03/2017.
 */
public class CompositeSearchAppenderTest {
    public class Context {
    }

    @Test
    public void testSimpleCompositeSearchAppenderThatDoesNothing() {
        SearchBuilder searchBuilder = new SearchBuilder();
        Context context = new Context();

        SearchAppender<Context> searchAppender1 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(false);

        CompositeSearchAppender<Context> compositeSearchAppender = new CompositeSearchAppender<>(searchAppender1);
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        Assert.assertFalse(appendResult);
    }

    @Test
    public void testSimpleCompositeSearchAppenderThatDoesSomething() {
        SearchBuilder searchBuilder = new SearchBuilder();
        Context context = new Context();

        SearchAppender<Context> searchAppender1 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(true);

        CompositeSearchAppender<Context> compositeSearchAppender = new CompositeSearchAppender<>(searchAppender1);
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        Assert.assertTrue(appendResult);
    }

    @Test
    public void testComplexCompositeSearchAppenderThatDoesNothing() {
        SearchBuilder searchBuilder = new SearchBuilder();
        Context context = new Context();

        SearchAppender<Context> searchAppender1 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(true);

        SearchAppender<Context> searchAppender2 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(false);

        CompositeSearchAppender<Context> compositeSearchAppender = new CompositeSearchAppender<>(searchAppender1, searchAppender2);
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        Assert.assertFalse(appendResult);
    }

    @Test
    public void testComplexCompositeSearchAppenderThatDoesSomething() {
        SearchBuilder searchBuilder = new SearchBuilder();
        Context context = new Context();

        SearchAppender<Context> searchAppender1 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(true);

        SearchAppender<Context> searchAppender2 = (SearchAppender<Context>)mock(SearchAppender.class);
        when(searchAppender1.append(eq(searchBuilder), eq(context))).thenReturn(true);

        CompositeSearchAppender<Context> compositeSearchAppender = new CompositeSearchAppender<>(searchAppender1, searchAppender2);
        boolean appendResult = compositeSearchAppender.append(searchBuilder, context);

        Assert.assertTrue(appendResult);
    }
}
