package com.yangdb.fuse.epb.plan.statistics.provider;

/*-
 * #%L
 * fuse-dv-epb
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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.yangdb.fuse.epb.plan.statistics.configuration.StatConfig;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProvider;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.Map;

/**
 * Created by Roman on 21/06/2017.
 */
public class ElasticStatDocumentProvider implements StatDataProvider {
    //region Constructors
    @Inject
    public ElasticStatDocumentProvider(MetricRegistry metricRegistry, Provider<Client> client, StatConfig config) {
        this.metricRegistry = metricRegistry;
        this.client = client;
        this.config = config;
    }
    //endregion

    //region StatDataProvider Implementation
    @Override
    public Iterable<Map<String, Object>> getStatDataItems(
            Iterable<String> indices,
            Iterable<String> types,
            Iterable<String> fields,
            Map<String, Object> constraints) {
        SearchBuilder searchBuilder = new SearchBuilder();
        searchBuilder.getIndices().add(config.getStatIndexName());
        searchBuilder.getIncludeSourceFields().add("*");
        searchBuilder.getQueryBuilder().query().filtered().filter().bool().push()
                .must().terms("index", indices).pop().push()
                .must().terms("sourceType", types).pop().push()
                .must().terms("field", fields).pop().push();

        constraints.forEach((key, value) -> searchBuilder.getQueryBuilder().must().term(key, value).pop().push());

        searchBuilder.setLimit(Integer.MAX_VALUE);
        searchBuilder.setScrollSize(1000);
        searchBuilder.setScrollTime(60000);

        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                this.client.get(),
                searchBuilder.build(this.client.get(), GlobalConstants.INCLUDE_AGGREGATION), SearchOrderProvider.of(SearchOrderProvider.EMPTY,SearchType.DEFAULT) ,
                searchBuilder.getLimit(),
                searchBuilder.getScrollSize(), searchBuilder.getScrollTime());

        return Stream.ofAll(hits)
                .map(SearchHit::getSourceAsMap)
                .toJavaList();
    }
    //endregion

    private MetricRegistry metricRegistry;
    //region Fields
    private Provider<Client> client;
    private StatConfig config;
    //endregion
}
