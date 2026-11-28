package com.yangdb.fuse.executor.opensearch;

import com.codahale.metrics.MetricRegistry;
import com.yangdb.fuse.dispatcher.provision.PointInTimes;
import com.yangdb.fuse.dispatcher.provision.ScrollProvisioning;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import com.yangdb.test.BaseITMarker;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.search.CreatePitAction;
import org.opensearch.action.search.CreatePitRequest;
import org.opensearch.action.search.DeletePitAction;
import org.opensearch.action.search.DeletePitRequest;
import org.opensearch.action.search.SearchType;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.yangdb.fuse.executor.TestSuiteIndexProviderSuite.client;

/**
 * ROADMAP 7.8: the searches of a consistent cursor all read its Point in Time, so a write between two of them is not
 * seen; without one, the later search sees it. Runs against the suite's OpenSearch through the REST bridge (which
 * forwards the PIT actions) and the engine's own search iterator, paging with search_after.
 */
public class PointInTimeConsistencyIT implements BaseITMarker {
    private static final String INDEX = "pit_consistency_it";
    private static final String CONSISTENT = PointInTimes.cursorContext("pitQuery", "1");
    private static final String NOT_CONSISTENT = PointInTimes.cursorContext("scrollQuery", "1");

    @Before
    public void setUp() {
        dropIndex();
        write(0, 30);
    }

    @After
    public void tearDown() {
        PointInTimes.releaseQuery("pitQuery");
        dropIndex();
    }

    @Test
    public void aConsistentCursorDoesNotSeeWritesMadeBetweenItsSearches() {
        String pit = client.execute(CreatePitAction.INSTANCE,
                new CreatePitRequest(TimeValue.timeValueMinutes(1), false, INDEX)).actionGet().getId();
        PointInTimes.register(CONSISTENT, new PointInTimes.Handle(pit, 60_000,
                () -> client.execute(DeletePitAction.INSTANCE, new DeletePitRequest(pit)).actionGet()));

        // page size 7: several search_after pages per search
        Assert.assertEquals("first search of the cursor", 30, read(CONSISTENT).size());

        write(30, 35); // five more documents, visible to new searches at once (refreshed)

        Set<String> again = read(CONSISTENT);
        Assert.assertEquals("a later search of the same consistent cursor sees its snapshot", 30, again.size());
        Assert.assertFalse(again.contains("30"));
        Assert.assertEquals("a cursor without a Point in Time sees the write", 35, read(NOT_CONSISTENT).size());

        PointInTimes.release(CONSISTENT);
        Assert.assertFalse(PointInTimes.get(CONSISTENT).isPresent());
    }

    private Set<String> read(String cursorContext) {
        Set<String> ids = new HashSet<>();
        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                client,
                new ScrollProvisioning.MetricRegistryScrollProvisioning(new MetricRegistry(), cursorContext),
                client.prepareSearch(INDEX).setQuery(QueryBuilders.matchAllQuery()),
                SearchOrderProvider.of(SearchOrderProvider.EMPTY, SearchType.DEFAULT),
                Long.MAX_VALUE, 7, 60_000);
        for (SearchHit hit : hits) {
            Assert.assertTrue("no hit twice: " + hit.getId(), ids.add(hit.getId()));
        }
        return ids;
    }

    private static void write(int from, int to) {
        BulkRequestBuilder bulk = client.prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (int i = from; i < to; i++) {
            bulk.add(client.prepareIndex(INDEX).setId(String.valueOf(i))
                    .setSource(Collections.singletonMap("type", "Dragon")));
        }
        Assert.assertFalse(bulk.get().hasFailures());
    }

    private static void dropIndex() {
        if (client.admin().indices().exists(new IndicesExistsRequest(INDEX)).actionGet().isExists()) {
            client.admin().indices().delete(new DeleteIndexRequest(INDEX)).actionGet();
        }
    }
}
