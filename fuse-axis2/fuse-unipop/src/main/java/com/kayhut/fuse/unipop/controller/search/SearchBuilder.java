package com.kayhut.fuse.unipop.controller.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by User on 26/03/2017.
 */
public class SearchBuilder {
    //region Constructor
    public SearchBuilder() {
        this.includeSourceFields = new HashSet<>();
        this.excludeSourceFields = new HashSet<>();
        this.indices = new HashSet<>();
        this.types = new HashSet<>();

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
    //endregion

    //region API
    public SearchRequestBuilder compose(
            Client client,
            boolean includeAggregations) {
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch()
                .setIndices(getIndices().stream().toArray(String[]::new))
                .setQuery(queryBuilder.getQuery())
                .setSize((int) getLimit());

        if (getIncludeSourceFields().size() == 0) {
            searchRequestBuilder.setFetchSource(false);
        } else {
            searchRequestBuilder.setFetchSource(
                    getIncludeSourceFields().stream().toArray(String[]::new),
                    getExcludeSourceFields().stream().toArray(String[]::new));
        }

        if (includeAggregations) {
            for (org.elasticsearch.search.aggregations.AbstractAggregationBuilder aggregationBuilder : aggregationBuilder.getAggregations()) {
                searchRequestBuilder.addAggregation(aggregationBuilder);
            }
        }

        return searchRequestBuilder;
    }
    //endregion

    //region Fields
    private Collection<String> types;
    private Collection<String> includeSourceFields;
    private Collection<String> excludeSourceFields;
    private Collection<String> indices;

    private QueryBuilder queryBuilder;
    private AggregationBuilder aggregationBuilder;

    private long limit;
    //endregion
}
