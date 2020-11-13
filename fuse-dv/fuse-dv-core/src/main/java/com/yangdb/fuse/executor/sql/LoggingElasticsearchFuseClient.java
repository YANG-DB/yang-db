package com.yangdb.fuse.executor.sql;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.logging.LoggingSyncMethodDecorator;
import com.yangdb.fuse.dispatcher.logging.MethodName;
import org.slf4j.Logger;

import java.util.Map;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;

public class LoggingElasticsearchFuseClient implements ElasticsearchClient {
    public static final String clientParam = "LoggingElasticsearchFuseClient.@client";
    public static final String loggerParameter = "LoggingPlanTraversalTranslator.@logger";

    //region Constructors
    @Inject
    public LoggingElasticsearchFuseClient(
            @Named(clientParam) ElasticsearchClient elasticsearchClient,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.elasticsearchClient = elasticsearchClient;
        this.logger = logger;
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Map<String, IndexMapping> getIndexMappings(String indexExpression) {
        return new LoggingSyncMethodDecorator<Map<String, IndexMapping>>(this.logger, this.metricRegistry, getMapping, trace)
                .decorate(() -> this.elasticsearchClient.getIndexMappings(indexExpression));
    }

    @Override
    public ElasticsearchResponse search(ElasticsearchRequest request) {
        return new LoggingSyncMethodDecorator<ElasticsearchResponse>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> this.elasticsearchClient.search(request));
    }

    @Override
    public void cleanup(ElasticsearchRequest request) {
        new LoggingSyncMethodDecorator<>(this.logger, this.metricRegistry, cleanup, trace)
                .decorate(() -> {
                    this.elasticsearchClient.cleanup(request);
                    return null;
                });

    }

    @Override
    public void schedule(Runnable task) {
        new LoggingSyncMethodDecorator<>(this.logger, this.metricRegistry, schedule, trace)
                .decorate(() -> {
                    this.elasticsearchClient.schedule(task);
                    return null;
                });
    }


    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private ElasticsearchClient elasticsearchClient;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static MethodName.MDCWriter getMapping = MethodName.of("getMapping");
    private static MethodName.MDCWriter cleanup = MethodName.of("cleanup");
    private static MethodName.MDCWriter schedule = MethodName.of("schedule");
    //endregion

}
