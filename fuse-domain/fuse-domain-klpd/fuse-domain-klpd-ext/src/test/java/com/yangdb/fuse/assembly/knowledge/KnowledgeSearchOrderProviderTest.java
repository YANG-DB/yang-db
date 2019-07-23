package com.yangdb.fuse.assembly.knowledge;

import com.yangdb.fuse.assembly.klpd.KnowledgeSearchOrderProvider;
import com.yangdb.fuse.unipop.controller.common.context.CompositeControllerContext;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;
import com.yangdb.fuse.unipop.promise.TraversalConstraint;
import com.yangdb.fuse.unipop.step.BoostingStepWrapper;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class KnowledgeSearchOrderProviderTest {

    @Test
    public void testNoBoosting(){
        CompositeControllerContext contextMock = Mockito.mock(CompositeControllerContext.class);
        when(contextMock.getConstraint()).thenAnswer(invocationOnMock -> {
           return Optional.of(new TraversalConstraint(__.start().has("abc", P.eq(123))));
        });

        KnowledgeSearchOrderProvider provider = new KnowledgeSearchOrderProvider();
        SearchOrderProvider build = provider.build(contextMock);
        Assert.assertEquals( SearchType.DEFAULT, build.getSearchType(null));
        Assert.assertEquals(new SearchOrderProvider.Sort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC), build.getSort(null));
    }

    @Test
    public void testBoostedConstraint(){
        CompositeControllerContext contextMock = Mockito.mock(CompositeControllerContext.class);
        when(contextMock.getConstraint()).thenAnswer(invocationOnMock -> {
            return Optional.of(new TraversalConstraint(__.start().asAdmin().addStep(new BoostingStepWrapper<>(__.start().has("abc", P.eq(123)).asAdmin().getEndStep(), 100))));
        });

        KnowledgeSearchOrderProvider provider = new KnowledgeSearchOrderProvider();
        SearchOrderProvider build = provider.build(contextMock);
        Assert.assertEquals( SearchType.DFS_QUERY_THEN_FETCH, build.getSearchType(null));
        Assert.assertEquals(SearchOrderProvider.EMPTY, build.getSort(null));
    }

    @Test
    public void testBoostedComplexConstraint(){
        CompositeControllerContext contextMock = Mockito.mock(CompositeControllerContext.class);
        when(contextMock.getConstraint()).thenAnswer(invocationOnMock -> {
            GraphTraversal.Admin<Object, Object> inner = __.start().asAdmin().addStep(new BoostingStepWrapper<>(__.start().has("abc", P.eq(123)).asAdmin().getEndStep(), 100));

            return Optional.of(new TraversalConstraint(__.start().and(__.start().has("bla", P.eq("123")), inner)));
        });

        KnowledgeSearchOrderProvider provider = new KnowledgeSearchOrderProvider();
        SearchOrderProvider build = provider.build(contextMock);
        Assert.assertEquals( SearchType.DFS_QUERY_THEN_FETCH, build.getSearchType(null));
        Assert.assertEquals(SearchOrderProvider.EMPTY, build.getSort(null));
    }

}
