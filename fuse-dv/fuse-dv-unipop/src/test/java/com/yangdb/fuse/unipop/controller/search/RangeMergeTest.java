package com.yangdb.fuse.unipop.controller.search;

import org.junit.Assert;
import org.junit.Test;

/**
 * Q16: conditions on one field share a range clause only where they are ANDed. The translator names each range after
 * its field (CompareQueryTranslator with shouldAggregateRange), so the builder decides whether a second condition on
 * the field joins the first one.
 */
public class RangeMergeTest {

    private static String build(java.util.function.Consumer<QueryBuilder> context) {
        QueryBuilder q = new QueryBuilder().query().bool();
        context.accept(q);
        q.push().range("height", "height").to(165).includeUpper(false).pop();
        q.push().range("height", "height").from(182).includeLower(false).pop();
        return q.getQuery().toString().replaceAll("\\s", "");
    }

    private static int ranges(String json) {
        return json.split("\"range\"", -1).length - 1;
    }

    @Test
    public void inShouldTheyStaySeparate() {
        String json = build(QueryBuilder::should);
        Assert.assertEquals(json, 2, ranges(json));
    }

    @Test
    public void inMustNotTheyStaySeparate() {
        String json = build(QueryBuilder::mustNot);
        Assert.assertEquals(json, 2, ranges(json));
    }

    @Test
    public void inFilterTheyShareOneRange() {
        String json = build(QueryBuilder::filter);
        Assert.assertEquals(json, 1, ranges(json));
        Assert.assertTrue(json, json.contains("\"from\":182") && json.contains("\"to\":165"));
    }
}
