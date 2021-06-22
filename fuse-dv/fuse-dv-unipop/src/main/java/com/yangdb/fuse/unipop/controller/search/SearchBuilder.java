package com.yangdb.fuse.unipop.controller.search;

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

import javaslang.collection.Stream;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by lior.perry on 26/03/2017.
 */
public class SearchBuilder {
    //region Constructor
    public SearchBuilder() {
        this.includeSourceFields = new HashSet<>();
        this.excludeSourceFields = new HashSet<>();
        this.indices = new HashSet<>();
        this.types = new HashSet<>();
        this.routing = new HashSet<>();

        this.queryBuilder = new QueryBuilder();
        this.aggregationBuilder = new AggregationBuilder();
    }
    //endregion

    //region Properties
    public QueryBuilder getQueryBuilder() {
        return this.queryBuilder;
    }

    public void setQueryBuilder(QueryBuilder value) {
        this.queryBuilder = value;
    }

    public AggregationBuilder getAggregationBuilder() {
        return this.aggregationBuilder;
    }

    public void setAggregationBuilder(AggregationBuilder aggregationBuilder) {
        this.aggregationBuilder = aggregationBuilder;
    }

    public long getLimit() {
        return this.limit;
    }

    public void setLimit(long value) {
        this.limit = value;
    }

    public int getScrollSize() {
        return this.scrollSize;
    }

    public void setScrollSize(int value) {
        this.scrollSize = value;
    }

    public int getScrollTime() {
        return this.scrollTime;
    }

    public void setScrollTime(int value) {
        this.scrollTime = value;
    }

    public Collection<String> getIncludeSourceFields() {
        return this.includeSourceFields;
    }

    public Collection<String> getExcludeSourceFields() {
        return this.excludeSourceFields;
    }

    public Collection<String> getIndices() {
        return this.indices;
    }

    public Collection<String> getTypes() {
        return this.types;
    }

    public Collection<String> getRouting() {
        return this.routing;
    }
    //endregion

    //region API
    public SearchRequestBuilder build(
            Client client,
            boolean includeAggregations) {
        String[] indices = getIndices().stream().toArray(String[]::new);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch();
        searchRequestBuilder.setQuery(queryBuilder.getQuery());
        searchRequestBuilder.setSize((int) getLimit());
        searchRequestBuilder.setIndices(indices);

        if (!routing.isEmpty()) {
            searchRequestBuilder.setRouting(Stream.ofAll(this.routing).toJavaArray(String.class));
        }


        if (getIncludeSourceFields().size() == 0) {
            searchRequestBuilder.setFetchSource(false);
        } else {
            searchRequestBuilder.setFetchSource(
                    Stream.ofAll(getIncludeSourceFields()).toJavaArray(String.class),
                    Stream.ofAll(getExcludeSourceFields()).toJavaArray(String.class));
        }

        if (includeAggregations) {
            aggregationBuilder.getAggregations().forEach(searchRequestBuilder::addAggregation);
        }

        return searchRequestBuilder;
    }
    //endregion

    //region Fields
    private Collection<String> types;
    private Collection<String> includeSourceFields;
    private Collection<String> excludeSourceFields;
    private Collection<String> indices;
    private Collection<String> routing;
    private Set<String> labels;

    private QueryBuilder queryBuilder;
    private AggregationBuilder aggregationBuilder;

    private long limit;
    private int scrollSize;
    private int scrollTime;
    //endregion
}
