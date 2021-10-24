package com.yangdb.fuse.unipop.controller.search.translation;

import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

public class CountFilterQueryTranslatorTest extends TestCase {

    public static final String ENTITY_A_ID = "entityA.id";


    @Test
    public void testGtEqTranslate() {
        AggregationBuilder builder = new AggregationBuilder();

        CountFilterQueryTranslator translator = new CountFilterQueryTranslator();
        Assert.assertTrue(translator.test(ENTITY_A_ID,CountFilterP.eq("120")));
        translator.translate(new QueryBuilder(), builder, ENTITY_A_ID, CountFilterP.eq("120"));

        Iterable<org.opensearch.search.aggregations.AggregationBuilder> aggregations = builder.getAggregations();
        Assert.assertTrue(aggregations.iterator().hasNext());
        Assert.assertEquals("{\"entityA.id\":{\"terms\":{\"field\":\"entityA.id\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"entityA.id\":{\"bucket_selector\":{\"buckets_path\":{\"entityA_id_Count\":\"_count\"},\"script\":{\"source\":\"def a=params.entityA_id_Count;a=120\",\"lang\":\"painless\"},\"gap_policy\":\"skip\"}}}}}",aggregations.iterator().next().toString());

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
        Assert.assertEquals("{\"entityA.id\":{\"terms\":{\"field\":\"entityA.id\",\"size\":10,\"min_doc_count\":1,\"shard_min_doc_count\":0,\"show_term_doc_count_error\":false,\"order\":[{\"_count\":\"desc\"},{\"_key\":\"asc\"}]},\"aggregations\":{\"entityA.id\":{\"bucket_selector\":{\"buckets_path\":{\"entityA_id_Count\":\"_count\"},\"script\":{\"source\":\"def a=params.entityA_id_Count;a>120\",\"lang\":\"painless\"},\"gap_policy\":\"skip\"}}}}}",aggregations.iterator().next().toString());
    }
}