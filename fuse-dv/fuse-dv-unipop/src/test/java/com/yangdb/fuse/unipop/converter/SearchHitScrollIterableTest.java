package com.yangdb.fuse.unipop.converter;

import com.yangdb.fuse.dispatcher.provision.ScrollProvisioning;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import org.apache.lucene.search.TotalHits;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opensearch.action.ListenableActionFuture;
import org.opensearch.action.search.ClearScrollRequestBuilder;
import org.opensearch.action.search.ClearScrollResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollRequestBuilder;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.Client;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import com.yangdb.fuse.dispatcher.provision.PointInTimes;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Phase 7.3: a search is read with one plain request when its first page isn't full; a scroll is opened only after
 * a full page, never returns a hit twice, and is always cleared.
 */
public class SearchHitScrollIterableTest {
    private static final int PAGE = 3;

    private Client client;
    private SearchRequestBuilder search;
    private SearchScrollRequestBuilder scroll;
    private ClearScrollRequestBuilder clear;
    private ScrollProvisioning provisioning;
    private SearchOrderProvider order;

    @Before
    public void setUp() {
        client = mock(Client.class);
        search = mock(SearchRequestBuilder.class, RETURNS_SELF);
        scroll = mock(SearchScrollRequestBuilder.class, RETURNS_SELF);
        clear = mock(ClearScrollRequestBuilder.class, RETURNS_SELF);
        provisioning = mock(ScrollProvisioning.class);
        order = mock(SearchOrderProvider.class);
        when(order.getSort(any())).thenReturn(SearchOrderProvider.EMPTY);
        when(order.getSearchType(any())).thenReturn(SearchType.DEFAULT);
        when(search.request()).thenReturn(new SearchRequest());
        when(client.prepareSearchScroll(anyString())).thenReturn(scroll);
        when(client.prepareClearScroll()).thenReturn(clear);
        ClearScrollResponse clearResponse = mock(ClearScrollResponse.class);
        ListenableActionFuture<ClearScrollResponse> cleared = future(clearResponse);
        when(clear.execute()).thenReturn(cleared);
    }

    @Test
    public void smallResultIsOnePlainSearchWithoutScroll() {
        ListenableActionFuture<SearchResponse> page = page(null, hits(0, 2));
        when(search.execute()).thenReturn(page);

        assertEquals(ids(0, 2), read(Long.MAX_VALUE));

        verify(search, times(1)).execute();
        verify(search, never()).setScroll(any(TimeValue.class));
        verify(client, never()).prepareSearchScroll(anyString());
        verify(client, never()).prepareClearScroll();
        verify(search).setSize(PAGE);
    }

    @Test
    public void fullPageThatIsTheWholeResultOpensNoScroll() {
        ListenableActionFuture<SearchResponse> page = exactPage(hits(0, PAGE), PAGE);
        when(search.execute()).thenReturn(page);

        assertEquals(ids(0, PAGE), read(Long.MAX_VALUE));

        verify(search, times(1)).execute();
        verify(search, never()).setScroll(any(TimeValue.class));
        verify(client, never()).prepareClearScroll();
    }

    @Test
    public void limitBelowThePageSizeIsOnePlainSearch() {
        ListenableActionFuture<SearchResponse> page = page(null, hits(0, 2));
        when(search.execute()).thenReturn(page);

        assertEquals(ids(0, 2), read(2));

        verify(search).setSize(2);
        verify(search, times(1)).execute();
        verify(client, never()).prepareSearchScroll(anyString());
    }

    @Test
    public void fullPageFallsBackToAScrollThatSkipsHitsAlreadyReturned() {
        // plain page: 0,1,2 (full); the scroll then returns 2,0,3 / 1,4 / nothing
        ListenableActionFuture<SearchResponse> plain = page(null, hits(0, 3));
        ListenableActionFuture<SearchResponse> first = page("s1", new SearchHit[]{hit(2), hit(0), hit(3)});
        ListenableActionFuture<SearchResponse> second = page("s1", new SearchHit[]{hit(1), hit(4)});
        ListenableActionFuture<SearchResponse> empty = page("s1", new SearchHit[0]);
        when(search.execute()).thenReturn(plain, first);
        when(scroll.execute()).thenReturn(second, empty);

        assertEquals(ids(0, 5), read(Long.MAX_VALUE));

        verify(search).setScroll(any(TimeValue.class));
        // the scroll leaves out the first page's documents, per index
        ArgumentCaptor<QueryBuilder> query = ArgumentCaptor.forClass(QueryBuilder.class);
        verify(search).setQuery(query.capture());
        BoolQueryBuilder rest = (BoolQueryBuilder) query.getValue();
        assertEquals(1, rest.mustNot().size());
        assertTrue(rest.mustNot().get(0).toString().contains("\"_index\""));
        verify(client, times(2)).prepareSearchScroll("s1");
        verify(clear).addScrollId("s1");
        verify(provisioning).clearScroll("s1");
    }

    @Test
    public void scrollStoppedByTheLimitIsCleared() {
        ListenableActionFuture<SearchResponse> plain = page(null, hits(0, 3));
        ListenableActionFuture<SearchResponse> first = page("s1", hits(0, 3));
        ListenableActionFuture<SearchResponse> next = page("s1", new SearchHit[]{hit(3), hit(4), hit(5)});
        when(search.execute()).thenReturn(plain, first);
        when(scroll.execute()).thenReturn(next);

        assertEquals(ids(0, 4), read(4));

        verify(clear).addScrollId("s1");
        verify(client, times(1)).prepareClearScroll();
    }

    @Test
    public void consistentCursorReadsItsPointInTimeWithSearchAfterAndNoScroll() {
        SearchRequest request = new SearchRequest("dragon", "idx_fire_*").source(new SearchSourceBuilder());
        when(search.request()).thenReturn(request);
        when(provisioning.pointInTime()).thenReturn(java.util.Optional.of(new PointInTimes.Handle("pit-1", 60000, () -> { })));
        ListenableActionFuture<SearchResponse> first = page(null, sorted(hits(0, PAGE)));
        ListenableActionFuture<SearchResponse> second = page(null, sorted(hits(3, 5)));
        when(search.execute()).thenReturn(first, second);

        assertEquals(ids(0, 5), read(Long.MAX_VALUE));

        // the PIT fixes the indices: the request names none, and filters on _index instead
        assertEquals(0, request.indices().length);
        ArgumentCaptor<QueryBuilder> query = ArgumentCaptor.forClass(QueryBuilder.class);
        verify(search).setQuery(query.capture());
        String filter = query.getValue().toString();
        assertTrue(filter.contains("\"_index\"") && filter.contains("idx_fire_*") && filter.contains("dragon"));
        assertEquals("pit-1", request.source().pointInTimeBuilder().getId());
        // the second page continues after the first page's last hit
        assertEquals(java.util.Arrays.asList(2), java.util.Arrays.asList(request.source().searchAfter()));
        verify(search).addSort("_doc", org.opensearch.search.sort.SortOrder.ASC);
        verify(search).addSort("_index", org.opensearch.search.sort.SortOrder.ASC);
        verify(search).addSort("_id", org.opensearch.search.sort.SortOrder.ASC);
        verify(search, never()).setScroll(any(TimeValue.class));
        verify(client, never()).prepareSearchScroll(anyString());
        verify(client, never()).prepareClearScroll();
    }

    /** hits with sort values, as a sorted (PIT) search returns them */
    private static SearchHit[] sorted(SearchHit[] hits) {
        for (SearchHit hit : hits) {
            hit.sortValues(new Object[]{Integer.parseInt(hit.getId())},
                    new org.opensearch.search.DocValueFormat[]{org.opensearch.search.DocValueFormat.RAW});
        }
        return hits;
    }

    private List<String> read(long limit) {
        List<String> out = new ArrayList<>();
        new SearchHitScrollIterable(client, provisioning, search, order, limit, PAGE, 60000)
                .forEach(h -> out.add(h.getId()));
        return out;
    }

    private static List<String> ids(int from, int to) {
        return IntStream.range(from, to).mapToObj(String::valueOf).collect(Collectors.toList());
    }

    private static SearchHit hit(int id) {
        return new SearchHit(id, String.valueOf(id), null, null);
    }

    private static SearchHit[] hits(int from, int to) {
        return IntStream.range(from, to).mapToObj(SearchHitScrollIterableTest::hit).toArray(SearchHit[]::new);
    }

    private static ListenableActionFuture<SearchResponse> page(String scrollId, SearchHit[] hits) {
        SearchResponse response = response(scrollId, hits, new TotalHits(hits.length, TotalHits.Relation.GREATER_THAN_OR_EQUAL_TO));
        return future(response);
    }

    private static ListenableActionFuture<SearchResponse> exactPage(SearchHit[] hits, long total) {
        SearchResponse response = response(null, hits, new TotalHits(total, TotalHits.Relation.EQUAL_TO));
        return future(response);
    }

    private static SearchResponse response(String scrollId, SearchHit[] hits, TotalHits total) {
        SearchResponse response = mock(SearchResponse.class);
        when(response.getScrollId()).thenReturn(scrollId);
        when(response.getHits()).thenReturn(
                new SearchHits(hits, total, 1f));
        return response;
    }

    @SuppressWarnings("unchecked")
    private static <T> ListenableActionFuture<T> future(T value) {
        ListenableActionFuture<T> future = mock(ListenableActionFuture.class);
        when(future.actionGet()).thenReturn(value);
        return future;
    }
}
