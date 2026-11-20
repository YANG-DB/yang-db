package com.yangdb.fuse.unipop.controller.search.translation;

import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

import static com.yangdb.fuse.unipop.controller.search.QueryDslAssert.assertDsl;

public class CountFilterQueryTranslatorTest extends TestCase {

    public static final String ENTITY_A_ID = "entityA.id";

    /**
     * Expected shape of a count filter: a terms aggregation on the field with a bucket_selector sub-aggregation
     * that runs {@code scriptSource} over the bucket's doc count.
     */
    static String countFilterAggregation(String scriptSource) {
        return "{'entityA.id':{" +
                "terms:{field:'entityA.id'}," +
                "aggregations:{'entityA.id':{bucket_selector:{" +
                    "buckets_path:{entityA_id_Count:'_count'}," +
                    "script:{source:'" + scriptSource + "',lang:'painless'}," +
                    "gap_policy:'skip'}}}}}";
    }

    @Test
    public void testGtEqTranslate() {
        AggregationBuilder builder = new AggregationBuilder();

        CountFilterQueryTranslator translator = new CountFilterQueryTranslator();
        Assert.assertTrue(translator.test(ENTITY_A_ID,CountFilterP.eq("120")));
        translator.translate(new QueryBuilder(), builder, ENTITY_A_ID, CountFilterP.eq("120"));

        Iterable<org.opensearch.search.aggregations.AggregationBuilder> aggregations = builder.getAggregations();
        Assert.assertTrue(aggregations.iterator().hasNext());
        // NOTE: pins current output. "a=120" is a painless assignment, not a comparison (should be "a==120");
        // see AggregationBuilder.BuildFilterScript.
        assertDsl(countFilterAggregation("def a=params.entityA_id_Count;a=120"), aggregations.iterator().next().toString());

    }

    @Test
    public void testGtEqTranslateBuilder() {
        final AggregationBuilder builder = new AggregationBuilder();
        AggregationBuilder edge = builder.countFilter(ENTITY_A_ID);
        edge.param("field", ENTITY_A_ID);
        edge.param("operator", CountFilterP.CountFilterCompare.gte);
        edge.param("operands", Collections.singletonList("120"));

        Iterable<org.opensearch.search.aggregations.AggregationBuilder> aggregations = builder.getAggregations();
        Assert.assertTrue(aggregations.iterator().hasNext());
        // NOTE: pins current output. gte renders as ">" (should be ">="); see AggregationBuilder.BuildFilterScript.
        assertDsl(countFilterAggregation("def a=params.entityA_id_Count;a>120"), aggregations.iterator().next().toString());
    }
}
