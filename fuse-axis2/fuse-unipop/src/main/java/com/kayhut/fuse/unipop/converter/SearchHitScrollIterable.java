package com.kayhut.fuse.unipop.converter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by r on 3/16/2015.
 */
public class SearchHitScrollIterable implements Iterable<SearchHit> {
    //region Constructor
    public SearchHitScrollIterable(
            MetricRegistry metricRegistry,
            Client client,
            SearchRequestBuilder searchRequestBuilder,
            long limit,
            int scrollSize,
            int scrollTime) {
        this.metricRegistry = metricRegistry;
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

    public int getScrollTime() {
        return this.scrollTime;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }
    //endregion


    private MetricRegistry metricRegistry;
    //region Fields
    private SearchRequestBuilder searchRequestBuilder;
    private long limit;
    private Client client;

    private int scrollTime;
    private int scrollSize;
    //endregion


    //region Iterator
    private class ScrollIterator implements Iterator<SearchHit> {
        //region Constructor
        private ScrollIterator(SearchHitScrollIterable iterable) {
            this.iterable = iterable;
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

            Timer.Context time = metricRegistry.timer(name(SearchHitScrollIterable.class.getSimpleName(), "Scroll")).time();
            Timer timeEs = metricRegistry.timer(name(SearchHitScrollIterable.class.getSimpleName(),"Scroll:elastic"));
            SearchResponse response;
            if (this.scrollId == null) {
                response = this.iterable.getSearchRequestBuilder()
                        .setSearchType(SearchType.SCAN)
                        .setScroll(new TimeValue(iterable.getScrollTime()))
                        .setSize(Math.min(iterable.getScrollSize(),
                                (int)Math.min((long)Integer.MAX_VALUE, iterable.getLimit())))
                        .execute()
                        .actionGet();
                this.scrollId = response.getScrollId();
                //update es execution time
                timeEs.update(response.getTookInMillis(), TimeUnit.MILLISECONDS);
                time.stop();
                Scroll();
            } else {
                response = this.iterable.getClient().prepareSearchScroll(this.scrollId)
                        .setScroll(new TimeValue(this.iterable.getScrollTime()))
                        .execute()
                        .actionGet();

                //update es execution time
                time.stop();
                timeEs.update(response.getTookInMillis(), TimeUnit.MILLISECONDS);

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
