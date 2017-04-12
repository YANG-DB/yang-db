package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by r on 3/16/2015.
 */
public class SearchHitScrollIterable implements Iterable<SearchHit> {
    //region Constructor
    public SearchHitScrollIterable(ElasticGraphConfiguration configuration,
                                   SearchRequestBuilder searchRequestBuilder,
                                   long limit,
                                   Client client) {
        this(client,
             searchRequestBuilder,
             limit,
             configuration.getElasticGraphScrollSize(),
             configuration.getElasticGraphScrollTime());
    }

    public SearchHitScrollIterable(
            Client client,
            SearchRequestBuilder searchRequestBuilder,
            long limit,
            int scrollSize,
            long scrollTime) {
        this.searchRequestBuilder = searchRequestBuilder;
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

    protected Client getClient() {
        return this.client;
    }

    protected long getLimit() {
        return this.limit;
    }

    public long getScrollTime() {
        return this.scrollTime;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }
    //endregion


    //region Fields
    private SearchRequestBuilder searchRequestBuilder;
    private long limit;
    private Client client;

    private long scrollTime;
    private int scrollSize;
    //endregion


    //region Iterator
    private class ScrollIterator implements Iterator<SearchHit> {
        //region Constructor
        private ScrollIterator(SearchHitScrollIterable iterable) {
            this.iterable = iterable;
            iterable.getSearchRequestBuilder().setSearchType(SearchType.SCAN)
                    .setScroll(new TimeValue(iterable.getScrollTime()))
                    .setSize(Math.min(iterable.getScrollSize(),
                            (int)Math.min((long)Integer.MAX_VALUE, iterable.getLimit())));

            this.scrollId = null;
            this.searchHits = new ArrayList<>(iterable.getScrollSize());
        }
        //endregion

        //region Iterator Implementation
        @Override
        public boolean hasNext() {
            if (this.searchHits.size() > 0) {
                return true;
            }

            Scroll();

            return this.searchHits.size() > 0;

        }

        @Override
        public SearchHit next() {
            if (this.searchHits.size() > 0) {
                SearchHit searchHit = this.searchHits.get(0);
                this.searchHits.remove(0);

                return searchHit;
            }

            Scroll();

            if (this.searchHits.size() > 0) {
                SearchHit searchHit = this.searchHits.get(0);
                this.searchHits.remove(0);

                return searchHit;
            }

            throw new NoSuchElementException();
        }
        //endregion

        //region Private Methods
        private void Scroll() {
            if (counter >= this.iterable.getLimit()) {
                return;
            }

            SearchResponse response;
            if (this.scrollId == null) {
                response = this.iterable.getSearchRequestBuilder()
                        .execute()
                        .actionGet();

                this.scrollId = response.getScrollId();
                Scroll();
            } else {
                response = this.iterable.getClient().prepareSearchScroll(this.scrollId)
                        .setScroll(new TimeValue(this.iterable.getScrollTime()))
                        .execute()
                        .actionGet();

                for(SearchHit hit : response.getHits().getHits()) {
                    if (counter < this.iterable.getLimit()) {
                        this.searchHits.add(hit);
                        counter++;
                    }
                }

                this.scrollId = response.getScrollId();
            }
        }
        //endregion

        //region Fields
        private SearchHitScrollIterable iterable;
        private ArrayList<SearchHit> searchHits;
        private String scrollId;

        private long counter;
        //endregion
    }
    //endregion
}
