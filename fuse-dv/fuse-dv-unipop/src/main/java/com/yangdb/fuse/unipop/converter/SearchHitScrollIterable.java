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

            SearchResponse response;
            if( this.scrollId != null) {
                response = this.iterable.getClient().prepareSearchScroll(this.scrollId)
                        .setScroll(new TimeValue(this.iterable.getScrollTime()))
                        .execute()
                        .actionGet();
            } else {
                response = getSearchResponse();
            }

            this.scrollId = response.getScrollId();
            //register scroll in the provisioning
            this.iterable.scrollProvision.addScroll(scrollId,iterable.getScrollTime());

            for (SearchHit hit : response.getHits().getHits()) {
                if (counter < this.iterable.getLimit()) {
                    this.searchHits.add(hit);
                    counter++;
                }
            }

            if (response.getHits().getHits().length == 0) {
                //unregister scroll in the provisioning
                this.iterable.scrollProvision.clearScroll(scrollId);
                this.iterable.getClient().prepareClearScroll()
                        .addScrollId(this.scrollId)
                        .execute()
                        .actionGet();
            }
        }

        private SearchResponse getSearchResponse() {
            SearchOrderProvider.Sort sort = getOrderProvider().getSort(this.iterable.getSearchRequestBuilder());
            SearchType searchType = getOrderProvider().getSearchType(this.iterable.getSearchRequestBuilder());
            return sort !=SearchOrderProvider.EMPTY  ?
                this.iterable.getSearchRequestBuilder()
                        .addSort(sort.getSortField(), sort.getSortOrder())
                        .setScroll(new TimeValue(iterable.getScrollTime()))
                        .setSearchType(searchType)
                        .setSize(Math.min(iterable.getScrollSize(),
                                (int) Math.min((long) Integer.MAX_VALUE, iterable.getLimit())))
                        .execute()
                        .actionGet() :
            this.iterable.getSearchRequestBuilder()
                    .setScroll(new TimeValue(iterable.getScrollTime()))
                    .setSearchType(searchType)
                    .setSize(Math.min(iterable.getScrollSize(),
                            (int) Math.min((long) Integer.MAX_VALUE, iterable.getLimit())))
                    .execute()
                    .actionGet();
        }
        //endregion

        //region Fields
        private SearchHitScrollIterable iterable;
        private ArrayList<SearchHit> searchHits;
        private String scrollId;
        private long counter;
        private long totalHits;
        //endregion
    }
    //endregion
}
