package com.kayhut.fuse.unipop.converter;

import com.kayhut.fuse.dispatcher.logging.ElapsedFrom;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogType;
import com.kayhut.fuse.dispatcher.logging.MethodName;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.log;

/**
 * Created by r on 3/16/2015.
 */
public class SearchHitScrollIterable implements Iterable<SearchHit> {
    private final static Logger logger = LoggerFactory.getLogger(SearchHitScrollIterable.class);
    private static MethodName.MDCWriter searchHitScrollIterable = MethodName.of("SearchHitScrollIterable()");

    //region Constructor
    public SearchHitScrollIterable(
            Client client,
            SearchRequestBuilder searchRequestBuilder,
            long limit,
            int scrollSize,
            int scrollTime) {
        this.searchRequestBuilder = searchRequestBuilder;
        this.limit = limit;
        this.scrollSize = scrollSize;
        this.scrollTime = scrollTime;
        this.client = client;
        //log elastic query
        new LogMessage.Impl(logger, trace, searchRequestBuilder.toString(), LogType.of(log), searchHitScrollIterable, ElapsedFrom.now()).log();
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

            SearchResponse response = this.scrollId == null ?
                    this.iterable.getSearchRequestBuilder()
                            .addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
                            .setScroll(new TimeValue(iterable.getScrollTime()))
                            .setSize(Math.min(iterable.getScrollSize(),
                                    (int) Math.min((long) Integer.MAX_VALUE, iterable.getLimit())))
                            .execute()
                            .actionGet() :
                    this.iterable.getClient().prepareSearchScroll(this.scrollId)
                            .setScroll(new TimeValue(this.iterable.getScrollTime()))
                            .execute()
                            .actionGet();

            this.scrollId = response.getScrollId();
            for (SearchHit hit : response.getHits().getHits()) {
                if (counter < this.iterable.getLimit()) {
                    this.searchHits.add(hit);
                    counter++;
                }
            }

            if (response.getHits().getHits().length == 0) {
                this.iterable.getClient().prepareClearScroll().addScrollId(this.scrollId).execute().actionGet();
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
