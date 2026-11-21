package com.yangdb.fuse.unipop.controller.search.translation;

import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collections;

import static com.yangdb.fuse.unipop.controller.search.QueryDslAssert.assertDsl;
import static com.yangdb.fuse.unipop.controller.search.translation.CountFilterQueryTranslatorTest.countFilterAggregation;

// NOTE: despite its name this class exercises CountFilterQueryTranslator, duplicating CountFilterQueryTranslatorTest;
// there is no distinct-filter translator.
public class DistinctFilterQueryTranslatorTest extends TestCase {

    public static final String ENTITY_A_ID = "entityA.id";
    public static final String ENTITY_B_ID = "entityB.id";


    @Test
    public void testGtEqTranslate() {
        AggregationBuilder builder = new AggregationBuilder();

        CountFilterQueryTranslator translator = new CountFilterQueryTranslator();
        Assert.assertTrue(translator.test(ENTITY_A_ID,CountFilterP.eq("120")));
        translator.translate(new QueryBuilder(), builder, ENTITY_A_ID, CountFilterP.eq("120"));

        Iterable<org.opensearch.search.aggregations.AggregationBuilder> aggregations = builder.getAggregations();
        Assert.assertTrue(aggregations.iterator().hasNext());
        // NOTE: pins current output; "a=120" is an assignment, not a comparison (see CountFilterQueryTranslatorTest)
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
        // NOTE: pins current output; gte renders as ">" (see CountFilterQueryTranslatorTest)
        assertDsl(countFilterAggregation("def a=params.entityA_id_Count;a>120"), aggregations.iterator().next().toString());
    }
}
