package com.yangdb.fuse.unipop.controller.search.translation;

import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import org.mockito.stubbing.Answer;
import org.opensearch.action.search.SearchAction;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.client.Client;
import org.opensearch.script.Script;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.unipop.process.predicate.CountFilterP;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.yangdb.fuse.unipop.controller.search.QueryDslAssert.assertDsl;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        assertDsl(countFilterAggregation("def a=params.entityA_id_Count;a==120"), aggregations.iterator().next().toString());

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
        assertDsl(countFilterAggregation("def a=params.entityA_id_Count;a>=120"), aggregations.iterator().next().toString());
    }

    /**
     * DiscreteVertexController.filterByAggregation sets the search size to the bulk size (the number of start
     * vertices), and SearchBuilder.build pushes it into the terms aggregation. The aggregation is therefore not
     * capped at the default of 10 buckets: there is a bucket slot for every start vertex in the bulk.
     */
    @Test
    public void testCountFilterTermsSizeIsTheBulkSizeNotTheDefault() {
        SearchBuilder searchBuilder = new SearchBuilder();
        new CountFilterQueryTranslator().translate(searchBuilder.getQueryBuilder(), searchBuilder.getAggregationBuilder(),
                ENTITY_A_ID, CountFilterP.gt("3"));
        searchBuilder.setLimit(0);
        searchBuilder.setSize(36);

        Client client = mock(Client.class);
        when(client.prepareSearch()).then((Answer<SearchRequestBuilder>) invocation ->
                new SearchRequestBuilder(client, mock(SearchAction.class)));
        SearchRequestBuilder request = searchBuilder.build(client, true);

        Collection<org.opensearch.search.aggregations.AggregationBuilder> aggs =
                request.request().source().aggregations().getAggregatorFactories();
        Assert.assertEquals(1, aggs.size());
        Assert.assertEquals(36, ((TermsAggregationBuilder) aggs.iterator().next()).size());
    }

    @Test
    public void testEveryCompareRendersAPainlessComparison() {
        Map<CountFilterP.CountFilterCompare, String> expected = new EnumMap<>(CountFilterP.CountFilterCompare.class);
        expected.put(CountFilterP.CountFilterCompare.eq, "==");
        expected.put(CountFilterP.CountFilterCompare.neq, "!=");
        expected.put(CountFilterP.CountFilterCompare.gt, ">");
        expected.put(CountFilterP.CountFilterCompare.gte, ">=");
        expected.put(CountFilterP.CountFilterCompare.lt, "<");
        expected.put(CountFilterP.CountFilterCompare.lte, "<=");
        // fails here if a new compare value is added without a rendering
        Assert.assertEquals(EnumSet.allOf(CountFilterP.CountFilterCompare.class), expected.keySet());

        for (Map.Entry<CountFilterP.CountFilterCompare, String> entry : expected.entrySet()) {
            Script script = AggregationBuilder.BuildFilterScript.script(
                    "entityA_id_Count", entry.getKey(), Collections.singletonList("120"));
            Assert.assertEquals(entry.getKey().name(),
                    "def a=params.entityA_id_Count;a" + entry.getValue() + "120", script.getIdOrCode());
        }
    }
}
