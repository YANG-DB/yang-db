package com.kayhut.fuse.unipop.controller.search;

import org.junit.Assert;
import org.junit.Test;

/**
 * test query builder composit expressions
 */
public class QueryBuilderTest {

    @Test
    public void wildcardScriptTest() {
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.query();
        queryBuilder.bool();
        queryBuilder.should();
        queryBuilder.wildcardScript("StringValue.keyword","*blabla*");


        Assert.assertTrue(queryBuilder.getCurrent() instanceof QueryBuilder.ScriptComposite);
        final QueryBuilder.ScriptComposite current = (QueryBuilder.ScriptComposite) queryBuilder.getCurrent();
        Assert.assertEquals("wildcard", current.getName());
        Assert.assertEquals("*blabla*", current.getValue());
        Assert.assertEquals("StringValue.keyword", current.getFieldName());
        Assert.assertEquals(QueryBuilder.Op.script, current.getOp());
    }

    @Test
    public void wildcardTest() {
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.query();
        queryBuilder.bool();
        queryBuilder.should();
        queryBuilder.wildcard("StringValue.keyword","*blabla*");


        Assert.assertTrue(queryBuilder.getCurrent() instanceof QueryBuilder.WildcardComposite);
        final QueryBuilder.WildcardComposite current = (QueryBuilder.WildcardComposite) queryBuilder.getCurrent();
        Assert.assertEquals("*blabla*", current.getValue());
        Assert.assertEquals("StringValue.keyword", current.getFieldName());
        Assert.assertEquals(QueryBuilder.Op.wildcard, current.getOp());
    }
}
