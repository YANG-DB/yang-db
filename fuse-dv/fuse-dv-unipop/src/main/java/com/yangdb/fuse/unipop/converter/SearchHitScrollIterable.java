package com.yangdb.fuse.unipop.converter;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.dispatcher.provision.ScrollProvisioning;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import org.apache.lucene.search.TotalHits;
import org.opensearch.action.search.SearchRequestBuilder;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchType;
import org.opensearch.client.Client;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.PointInTimeBuilder;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;
import com.yangdb.fuse.dispatcher.provision.PointInTimes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Created by roman on 3/16/2015.
 */
public class SearchHitScrollIterable implements Iterable<SearchHit> {
    //region Constructor
    public SearchHitScrollIterable(
            Client client,
            ScrollProvisioning scrollProvision,
            SearchRequestBuilder searchRequestBuilder,
            SearchOrderProvider orderProvider,
            long limit,
            int scrollSize,
            int scrollTime) {
        this.scrollProvision = scrollProvision;
        this.searchRequestBuilder = searchRequestBuilder;
        this.orderProvider = orderProvider;
        this.limit = limit;
        this.scrollSize = scrollSize;
        this.scrollTime = scrollTime;
        this.client = client;
    }
    //endregion

    //region Iterable Implementation
    @Override
    public Iterator<SearchHit> iterator() {
        return new ScrollIterator(this);
    }
    //endregion

    //region Properties
    protected SearchRequestBuilder getSearchRequestBuilder() {
        return this.searchRequestBuilder;
    }

    public SearchOrderProvider getOrderProvider() {
        return orderProvider;
    }

    protected Client getClient() {
        return this.client;
    }

    protected long getLimit() {
        return this.limit;
    }

    public int getScrollTime() {
        return this.scrollTime;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }
    //endregion

    private ScrollProvisioning scrollProvision;
    //region Fields
    private SearchRequestBuilder searchRequestBuilder;
    private SearchOrderProvider orderProvider;
    private long limit;
    private Client client;

    private int scrollTime;
    private int scrollSize;
    //endregion


    //region Iterator
    /**
     * Reads the hits of one search, cheapest way first (Phase 7.3, docs/benchmark.md):
     * <ol>
     *   <li>a plain search (no scroll) for the first page, sized {@code min(scrollSize, limit)};</li>
     *   <li>if that page isn't full, or the limit is reached, that's every hit: no scroll is opened, nothing to clear;</li>
     *   <li>OpenSearch's exact hit count also tells when a full page is the whole result;</li>
     *   <li>only after a full page, while more hits may exist, the same search runs again as a scroll, with the hits
     *   already returned excluded by index and id, so their documents aren't loaded again;</li>
     *   <li>an opened scroll is cleared as soon as it's exhausted or the limit is reached.</li>
     * </ol>
     * Before this, every search opened a scroll, needed one more (empty) scroll request to see the end, and then a
     * clear; a search stopped by its limit left its scroll open until it timed out.
     */
    private class ScrollIterator implements Iterator<SearchHit> {
        //region Constructor
        private ScrollIterator(SearchHitScrollIterable iterable) {
            this.iterable = iterable;
            this.scrollId = null;
            this.searchHits = new ArrayList<>();
            this.returned = new HashSet<>();
        }
        //endregion

        //region Iterator Implementation
        @Override
        public boolean hasNext() {
            while (this.searchHits.isEmpty() && !this.done) {
                fetch();
            }
            return !this.searchHits.isEmpty();
        }

        @Override
        public SearchHit next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return this.searchHits.remove(0);
        }
        //endregion

        //region Private Methods
        private void fetch() {
            if (this.counter >= this.iterable.getLimit()) {
                finish();
                return;
            }
            java.util.Optional<PointInTimes.Handle> pit = this.iterable.scrollProvision.pointInTime();
            if (pit.isPresent()) {
                fetchFromPointInTime(pit.get());
                return;
            }

            if (!this.started) {
                this.started = true;
                prepare();
                SearchResponse response = this.iterable.getSearchRequestBuilder().execute().actionGet();
                SearchHit[] hits = response.getHits().getHits();
                for (SearchHit hit : hits) {
                    take(hit, true);
                }
                // the whole result: a page that isn't full, or OpenSearch's exact hit count fits in this page
                TotalHits total = response.getHits().getTotalHits();
                boolean complete = hits.length < this.pageSize
                        || (total != null && total.relation == TotalHits.Relation.EQUAL_TO && total.value <= hits.length);
                if (complete || this.counter >= this.iterable.getLimit()) {
                    this.done = true;
                }
                return;
            }

            SearchResponse response;
            if (this.scrollId == null) {
                // more hits than one page: the same search again, as a scroll, leaving out what's already returned
                excludeReturned();
                response = this.iterable.getSearchRequestBuilder()
                        .setScroll(new TimeValue(this.iterable.getScrollTime()))
                        .execute()
                        .actionGet();
            } else {
                response = this.iterable.getClient().prepareSearchScroll(this.scrollId)
                        .setScroll(new TimeValue(this.iterable.getScrollTime()))
                        .execute()
                        .actionGet();
            }
            if (response.getScrollId() != null && !response.getScrollId().equals(this.scrollId)) {
                if (this.scrollId != null) {
                    this.iterable.scrollProvision.clearScroll(this.scrollId);
                }
                this.scrollId = response.getScrollId();
                this.iterable.scrollProvision.addScroll(this.scrollId, this.iterable.getScrollTime());
            }

            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits) {
                if (!this.returned.isEmpty() && this.returned.remove(key(hit))) {
                    continue; // already returned by the first, plain page
                }
                take(hit, false);
            }
            if (hits.length == 0 || this.counter >= this.iterable.getLimit()) {
                finish();
            }
        }

        /**
         * Adds to the search a {@code must_not} for every hit the first page returned, per index ({@code _id}s repeat
         * across indices), so the scroll doesn't load those documents again. The id check in {@link #fetch()} stays as
         * a safety net.
         */
        private void excludeReturned() {
            SearchRequestBuilder builder = this.iterable.getSearchRequestBuilder();
            Map<String, List<String>> byIndex = new HashMap<>();
            for (String key : this.returned) {
                int slash = key.indexOf('/');
                byIndex.computeIfAbsent(key.substring(0, slash), k -> new ArrayList<>()).add(key.substring(slash + 1));
            }
            SearchSourceBuilder source = builder.request().source();
            QueryBuilder query = source != null && source.query() != null ? source.query() : QueryBuilders.matchAllQuery();
            BoolQueryBuilder rest = QueryBuilders.boolQuery().filter(query);
            byIndex.forEach((index, ids) -> rest.mustNot(QueryBuilders.boolQuery()
                    .filter(QueryBuilders.termQuery("_index", index))
                    .filter(QueryBuilders.idsQuery().addIds(ids.toArray(new String[0])))));
            builder.setQuery(rest);
        }

        /**
         * A consistent cursor (ROADMAP 7.8): every request reads the cursor's Point in Time, so this search, the cursor's
         * other searches and its later pages all see one snapshot. A PIT request names no indices (the PIT fixes them),
         * so the indices this search meant become an {@code _index} filter; pages follow with {@code search_after} on a
         * sort that is unique per hit: {@code _doc} (cheap), then {@code _index} and {@code _id}. No scroll is used.
         */
        private void fetchFromPointInTime(PointInTimes.Handle pit) {
            SearchRequestBuilder builder = this.iterable.getSearchRequestBuilder();
            if (!this.started) {
                this.started = true;
                prepare();
                String[] indices = builder.request().indices();
                builder.request().indices(new String[0]);
                builder.request().routing((String) null);
                if (indices != null && indices.length > 0) {
                    BoolQueryBuilder inIndices = QueryBuilders.boolQuery().minimumShouldMatch(1);
                    for (String index : indices) {
                        inIndices.should(index.contains("*") || index.contains("?")
                                ? QueryBuilders.wildcardQuery("_index", index)
                                : QueryBuilders.termQuery("_index", index));
                    }
                    SearchSourceBuilder source = builder.request().source();
                    QueryBuilder query = source != null && source.query() != null ? source.query() : QueryBuilders.matchAllQuery();
                    builder.setQuery(QueryBuilders.boolQuery().filter(query).filter(inIndices));
                }
                builder.addSort("_doc", SortOrder.ASC).addSort("_index", SortOrder.ASC).addSort("_id", SortOrder.ASC);
                builder.request().source().pointInTimeBuilder(
                        new PointInTimeBuilder(pit.getId()).setKeepAlive(TimeValue.timeValueMillis(pit.getKeepAliveMillis())));
            } else {
                builder.request().source().searchAfter(this.searchAfter);
            }
            SearchHit[] hits = builder.execute().actionGet().getHits().getHits();
            for (SearchHit hit : hits) {
                take(hit, false);
            }
            if (hits.length < this.pageSize || this.counter >= this.iterable.getLimit()) {
                this.done = true;
                return;
            }
            this.searchAfter = hits[hits.length - 1].getSortValues();
        }

        /** sort, search type and page size, set once on the request (the scroll fallback reuses it) */
        private void prepare() {
            SearchRequestBuilder builder = this.iterable.getSearchRequestBuilder();
            SearchOrderProvider.Sort sort = getOrderProvider().getSort(builder);
            SearchType searchType = getOrderProvider().getSearchType(builder);
            if (sort != SearchOrderProvider.EMPTY) {
                builder.addSort(sort.getSortField(), sort.getSortOrder());
            }
            this.pageSize = Math.min(this.iterable.getScrollSize(),
                    (int) Math.min((long) Integer.MAX_VALUE, this.iterable.getLimit()));
            builder.setSearchType(searchType).setSize(this.pageSize);
        }

        private void take(SearchHit hit, boolean firstPage) {
            if (this.counter < this.iterable.getLimit()) {
                this.searchHits.add(hit);
                this.counter++;
                if (firstPage) {
                    this.returned.add(key(hit));
                }
            }
        }

        private String key(SearchHit hit) {
            return hit.getIndex() + "/" + hit.getId();
        }

        private void finish() {
            this.done = true;
            this.returned.clear();
            if (this.scrollId != null) {
                String id = this.scrollId;
                this.scrollId = null;
                this.iterable.scrollProvision.clearScroll(id);
                this.iterable.getClient().prepareClearScroll()
                        .addScrollId(id)
                        .execute()
                        .actionGet();
            }
        }
        //endregion

        //region Fields
        private SearchHitScrollIterable iterable;
        private ArrayList<SearchHit> searchHits;
        private Set<String> returned;
        private String scrollId;
        private long counter;
        private int pageSize;
        private boolean started;
        private boolean done;
        private Object[] searchAfter;
        //endregion
    }
    //endregion
}
