package com.yangdb.fuse.unipop.controller.common.converter;

import org.junit.Assert;
import org.junit.Test;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.bucket.terms.Terms;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Over the REST bridge (OpenSearch 2.x) a terms aggregation arrives as ParsedStringTerms, not the internal StringTerms
 * an embedded node returns; the count filter must read its buckets either way.
 */
public class SearchAggDataItemTest {

    @Test
    public void readsBucketsOfAnyTermsImplementation() {
        Terms.Bucket a = Mockito.mock(Terms.Bucket.class);
        Mockito.when(a.getKey()).thenReturn("p1");
        Terms.Bucket b = Mockito.mock(Terms.Bucket.class);
        Mockito.when(b.getKey()).thenReturn("p2");
        Terms parsedTerms = Mockito.mock(Terms.class); // stands in for ParsedStringTerms: a Terms, not a StringTerms
        Mockito.<List<? extends Terms.Bucket>>when(parsedTerms.getBuckets()).thenReturn(Arrays.asList(a, b));

        List<SearchAggDataItem> items = SearchAggDataItem.build(Collections.<String, Aggregation>singletonMap("agg", parsedTerms));

        Assert.assertEquals(2, items.size());
        Assert.assertEquals("p1", items.get(0).id());
    }
}
