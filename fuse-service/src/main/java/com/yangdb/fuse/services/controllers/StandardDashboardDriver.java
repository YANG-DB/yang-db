package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.DashboardDriver;
import com.yangdb.fuse.executor.resource.PersistantNodeStatusResource;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValueType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.GlobalConstants.DEFAULT_DATE_FORMAT;
import static com.yangdb.fuse.model.GlobalConstants.TYPE;
import static com.yangdb.fuse.model.ontology.Ontology.Accessor.NodeType.ENTITY;
import static com.yangdb.fuse.model.ontology.Ontology.Accessor.NodeType.RELATION;
import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardDashboardDriver implements DashboardDriver {

    public static final String CREATION_TIME = "creationTime";
    private Client client;

    //region Constructors
    @Inject
    public StandardDashboardDriver(Client client) {
        this.client = client;
    }

    @Override
    //todo - fix this to be Ontology depended
    public Map graphElementCount() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        final TermsAggregationBuilder aggregation = new TermsAggregationBuilder("graphElementCount",ValueType.STRING);
        aggregation.field(TYPE);
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((StringTerms) response.getAggregations().get("graphElementCount")).getBuckets().stream()
                .collect(Collectors.toMap(StringTerms.Bucket::getKey, StringTerms.Bucket::getDocCount));
        return elementCount;
    }

    @Override
    //todo - fix this to be Ontology depended
    public Map graphElementCreated() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setSize(0);
        builder.setQuery(boolQuery()
                .should(termQuery(TYPE, ENTITY.toString().toLowerCase()))
                .should(termQuery(TYPE, RELATION.toString().toLowerCase())));
        final DateHistogramAggregationBuilder aggregation = new DateHistogramAggregationBuilder("graphElementCreatedOverTime");
        aggregation.field(CREATION_TIME);
        aggregation.interval(1000*60*60*24);
        aggregation.format(DEFAULT_DATE_FORMAT);
        final SearchResponse response = builder.addAggregation(aggregation).get();
        final Map<Object, Long> elementCount = ((InternalDateHistogram) response.getAggregations().get("graphElementCreatedOverTime")).getBuckets().stream()
                .collect(Collectors.toMap(InternalDateHistogram.Bucket::getKey, InternalDateHistogram.Bucket::getDocCount));

        return elementCount;
    }

    @Override
    public Map cursorCount() {
        final SearchRequestBuilder builder = client.prepareSearch();
        builder.setIndices(PersistantNodeStatusResource.SYSTEM);
        builder.setQuery(matchAllQuery());
        SearchResponse response = builder.get();
        return Arrays.stream(response.getHits().getHits()).collect(Collectors.toMap(SearchHit::getId, SearchHit::getSourceAsMap));
    }

    //enStridregion
}
