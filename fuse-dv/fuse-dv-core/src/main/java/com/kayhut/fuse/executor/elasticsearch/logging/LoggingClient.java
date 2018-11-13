package com.kayhut.fuse.executor.elasticsearch.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.logging.*;
import org.elasticsearch.action.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.termvectors.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.*;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

/**
 * Created by roman.margolis on 28/12/2017.
 */
public class LoggingClient implements Client {
    public static final String clientParameter = "LoggingClient.@client";
    public static final String loggerParameter = "LoggingClient.@logger";

    //region Constructors
    @Inject
    public LoggingClient(
            @Named(clientParameter) Client client,
            @Named(loggerParameter) Logger logger,
            MetricRegistry metricRegistry) {
        this.client = client;
        this.logger = logger;
        this.verboseLogger = LoggerFactory.getLogger(logger.getName() + ".Verbose");
        this.metricRegistry = metricRegistry;

        this.operationId = 0;
    }
    //endregion

    //region Client Implementation
    @Override
    public AdminClient admin() {
        return client.admin();
    }

    @Override
    public ActionFuture<IndexResponse> index(IndexRequest indexRequest) {
        return client.index(indexRequest);
    }

    @Override
    public void index(IndexRequest indexRequest, ActionListener<IndexResponse> actionListener) {
        client.index(indexRequest, actionListener);
    }

    @Override
    public IndexRequestBuilder prepareIndex() {
        return client.prepareIndex();
    }

    @Override
    public ActionFuture<UpdateResponse> update(UpdateRequest updateRequest) {
        return client.update(updateRequest);
    }

    @Override
    public void update(UpdateRequest updateRequest, ActionListener<UpdateResponse> actionListener) {
        client.update(updateRequest, actionListener);
    }

    @Override
    public UpdateRequestBuilder prepareUpdate() {
        return client.prepareUpdate();
    }

    @Override
    public UpdateRequestBuilder prepareUpdate(String s, String s1, String s2) {
        return client.prepareUpdate(s, s1, s2);
    }

    @Override
    public IndexRequestBuilder prepareIndex(String s, String s1) {
        return client.prepareIndex(s, s1);
    }

    @Override
    public IndexRequestBuilder prepareIndex(String s, String s1, String s2) {
        return client.prepareIndex(s, s1, s2);
    }

    @Override
    public ActionFuture<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return client.delete(deleteRequest);
    }

    @Override
    public void delete(DeleteRequest deleteRequest, ActionListener<DeleteResponse> actionListener) {
        client.delete(deleteRequest, actionListener);
    }

    @Override
    public DeleteRequestBuilder prepareDelete() {
        return client.prepareDelete();
    }

    @Override
    public DeleteRequestBuilder prepareDelete(String s, String s1, String s2) {
        return client.prepareDelete(s, s1, s2);
    }

    @Override
    public ActionFuture<BulkResponse> bulk(BulkRequest bulkRequest) {
        return client.bulk(bulkRequest);
    }

    @Override
    public void bulk(BulkRequest bulkRequest, ActionListener<BulkResponse> actionListener) {
        client.bulk(bulkRequest, actionListener);
    }

    @Override
    public BulkRequestBuilder prepareBulk() {
        return client.prepareBulk();
    }

    @Override
    public ActionFuture<GetResponse> get(GetRequest getRequest) {
        return client.get(getRequest);
    }

    @Override
    public void get(GetRequest getRequest, ActionListener<GetResponse> actionListener) {
        client.get(getRequest, actionListener);
    }

    @Override
    public GetRequestBuilder prepareGet() {
        return client.prepareGet();
    }

    @Override
    public GetRequestBuilder prepareGet(String s, String s1, String s2) {
        return client.prepareGet(s, s1, s2);
    }

    @Override
    public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest multiGetRequest) {
        return client.multiGet(multiGetRequest);
    }

    @Override
    public void multiGet(MultiGetRequest multiGetRequest, ActionListener<MultiGetResponse> actionListener) {
        client.multiGet(multiGetRequest, actionListener);
    }

    @Override
    public MultiGetRequestBuilder prepareMultiGet() {
        return client.prepareMultiGet();
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest searchRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();

        int operationId = this.operationId++;

        try {
            new LogMessage.Impl(this.logger, trace, "#{} start search", sequence, LogType.of(start), search, ElapsedFrom.now(), NetworkElasticElapsed.start())
                    .with(operationId).log();
            new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), search, ElapsedFrom.now())
                    .with(operationId, searchRequest.toString()).log();

            ActionFuture<SearchResponse> future = client.search(searchRequest);
            return new LoggingActionFuture<>(future,
                            (response -> new LogMessage.Impl(this.logger, trace, "#{} finish search", sequence, LogType.of(success), search,
                                    ElapsedFrom.now(),
                                    ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                                    NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                                    .with(operationId)),
                            (ex -> new LogMessage.Impl(this.logger, error, "#{} failed search", sequence, LogType.of(failure), search,
                                    ElapsedFrom.now(), NetworkElasticElapsed.stop())
                                    .with(operationId, ex)),
                            timerContext,
                            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")),
                            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")));
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "#{} failed search", sequence, LogType.of(failure), search, ElapsedFrom.now())
                    .with(operationId, ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")).mark();
            timerContext.stop();
            throw ex;
        }
    }

    @Override
    public void search(SearchRequest searchRequest, ActionListener<SearchResponse> actionListener) {
        Timer.Context timerContext =  this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();

        int operationId = this.operationId++;

        try {
            new LogMessage.Impl(this.logger, trace, "#{} start search", sequence, LogType.of(start), search, ElapsedFrom.now(), NetworkElasticElapsed.start())
                    .with(operationId).log();
            new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), search, ElapsedFrom.now())
                    .with(operationId, searchRequest.toString()).log();

            this.client.search(
                    searchRequest,
                    new LoggingActionListener<>(
                            actionListener,
                            (response -> new LogMessage.Impl(this.logger, trace, "#{} finish search", sequence, LogType.of(success), search,
                                    ElapsedFrom.now(),
                                    ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                                    NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                                    .with(operationId)),
                            (ex -> new LogMessage.Impl(this.logger, error, "#{} failed search", sequence, LogType.of(failure), search,
                                    ElapsedFrom.now(), NetworkElasticElapsed.stop())
                                    .with(operationId, ex)),
                            (ex -> new LogMessage.Impl(this.logger, error, "#{} failed search actionListener", sequence, LogType.of(failure), search,
                                    ElapsedFrom.now())
                                    .with(operationId, ex)),
                            timerContext,
                            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")),
                            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure"))));
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "#{} failed search", sequence, LogType.of(failure), search, ElapsedFrom.now())
                    .with(operationId, ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")).mark();
            timerContext.stop();
            throw ex;
        }
    }

    @Override
    public SearchRequestBuilder prepareSearch(String... strings) {
        int operationId = this.operationId++;

        return new LoggingSearchRequestBuilder(
                this,
                SearchAction.INSTANCE,
                (searchRequestBuilder -> new LogMessage.Impl(this.logger, trace, "#{} start search", sequence, LogType.of(start), search,
                        ElapsedFrom.now(), NetworkElasticElapsed.start())
                        .with(operationId)),
                (searchRequestBuilder -> new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), search,
                        ElapsedFrom.deferredNow()).with(operationId, searchRequestBuilder.toString())),
                (response -> new LogMessage.Impl(this.logger, trace, "#{} finish search", sequence, LogType.of(success), search,
                        ElapsedFrom.now(),
                        ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                        NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                        .with(operationId)),
                (ex -> new LogMessage.Impl(this.logger, error, "#{} failed search", sequence, LogType.of(failure), search,
                        ElapsedFrom.now(), NetworkElasticElapsed.stop())
                        .with(operationId, ex)),
                this.metricRegistry.timer(name(this.logger.getName(), search.toString())),
                this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")),
                this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")))
                .setIndices(strings);
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest searchScrollRequest) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), searchScroll.toString())).time();

        int operationId = this.operationId++;

        try {
            new LogMessage.Impl(this.logger, trace, "#{} start searchScroll", sequence, LogType.of(start), searchScroll, ElapsedFrom.now(), NetworkElasticElapsed.start())
                    .with(operationId).log();
            new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), searchScroll, ElapsedFrom.now())
                    .with(operationId, searchScrollRequest.toString()).log();

            ActionFuture<SearchResponse> future = client.searchScroll(searchScrollRequest);
            return new LoggingActionFuture<>(future,
                    (response -> new LogMessage.Impl(this.logger, trace, "#{} finish searchScroll", sequence, LogType.of(success), searchScroll,
                            ElapsedFrom.now(),
                            ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                            NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                            .with(operationId)),
                    (ex -> new LogMessage.Impl(this.logger, error, "#{} failed searchScroll", sequence, LogType.of(failure), searchScroll,
                            ElapsedFrom.now(), NetworkElasticElapsed.stop())
                            .with(operationId, ex)),
                    timerContext,
                    this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "success")),
                    this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "failure")));
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "#{} failed searchScroll", sequence, LogType.of(failure), searchScroll, ElapsedFrom.now())
                    .with(operationId, ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "failure")).mark();
            timerContext.stop();
            throw ex;
        }
    }

    @Override
    public void searchScroll(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> actionListener) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), searchScroll.toString())).time();

        int operationId = this.operationId++;

        try {
            new LogMessage.Impl(this.logger, trace, "#{} start searchScroll", sequence, LogType.of(start), searchScroll, ElapsedFrom.now(), NetworkElasticElapsed.start()).with(operationId).log();
            new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), searchScroll, ElapsedFrom.now())
                    .with(operationId, searchScrollRequest.toString()).log();

            client.searchScroll(
                    searchScrollRequest,
                    new LoggingActionListener<>(
                            actionListener,
                            (response -> new LogMessage.Impl(this.logger, trace, "#{} finish searchScroll", sequence, LogType.of(success), searchScroll,
                                    ElapsedFrom.now(),
                                    ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                                    NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                                    .with(operationId)),
                            (ex -> new LogMessage.Impl(this.logger, error, "#{} failed searchScroll", sequence, LogType.of(failure), searchScroll,
                                    ElapsedFrom.deferredNow(), NetworkElasticElapsed.stop())
                                    .with(operationId, ex)),
                            (ex -> new LogMessage.Impl(this.logger, error, "#{} failed searchScroll actionListener", sequence, LogType.of(failure), searchScroll,
                                    ElapsedFrom.now())
                                    .with(operationId, ex)),
                            timerContext,
                            this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "success")),
                            this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "failure"))));
        } catch (Exception ex) {
            new LogMessage.Impl(this.logger, error, "#{} failed searchScroll", sequence, LogType.of(failure), searchScroll, ElapsedFrom.now())
                    .with(operationId, ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "failure")).mark();
            timerContext.stop();
            throw ex;
        }
    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        int operationId = this.operationId++;

        return new LoggingSearchScrollRequestBuilder(
                this,
                SearchScrollAction.INSTANCE,
                scrollId,
                (searchScrollRequestBuilder -> new LogMessage.Impl(this.logger, trace, "#{} start searchScroll", sequence, LogType.of(start), searchScroll,
                        ElapsedFrom.now(), NetworkElasticElapsed.start())
                        .with(operationId)),
                (searchScrollRequestBuilder -> new LogMessage.Impl(this.verboseLogger, trace, "#{} {}", sequence, LogType.of(log), searchScroll,
                        ElapsedFrom.now())
                        .with(operationId, searchScrollRequestBuilder.toString())),
                (response -> new LogMessage.Impl(this.logger, trace, "#{} finish searchScroll", sequence, LogType.of(success), searchScroll,
                        ElapsedFrom.now(),
                        ElasticElapsed.of(response.getTookInMillis()), ElasticElapsed.add(response.getTookInMillis()),
                        NetworkElasticElapsed.stop(), NetworkElasticElapsed.stopTotal())
                        .with(operationId)),
                (ex -> new LogMessage.Impl(this.logger, error, "#{} failed searchScroll", sequence, LogType.of(failure), searchScroll,
                        ElapsedFrom.now(), NetworkElasticElapsed.stop())
                        .with(operationId, ex)),
                this.metricRegistry.timer(name(this.logger.getName(), searchScroll.toString())),
                this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "success")),
                this.metricRegistry.meter(name(this.logger.getName(), searchScroll.toString(), "failure")));
    }

    @Override
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest multiSearchRequest) {
        return client.multiSearch(multiSearchRequest);
    }

    @Override
    public void multiSearch(MultiSearchRequest multiSearchRequest, ActionListener<MultiSearchResponse> actionListener) {
        client.multiSearch(multiSearchRequest, actionListener);
    }

    @Override
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return client.prepareMultiSearch();
    }

    @Override
    public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest termVectorsRequest) {
        return client.termVectors(termVectorsRequest);
    }

    @Override
    public void termVectors(TermVectorsRequest termVectorsRequest, ActionListener<TermVectorsResponse> actionListener) {
        client.termVectors(termVectorsRequest, actionListener);
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors() {
        return client.prepareTermVectors();
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors(String s, String s1, String s2) {
        return client.prepareTermVectors(s, s1, s2);
    }

    @Override
    @Deprecated
    public ActionFuture<TermVectorsResponse> termVector(TermVectorsRequest termVectorsRequest) {
        return client.termVector(termVectorsRequest);
    }

    @Override
    @Deprecated
    public void termVector(TermVectorsRequest termVectorsRequest, ActionListener<TermVectorsResponse> actionListener) {
        client.termVector(termVectorsRequest, actionListener);
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector() {
        return client.prepareTermVector();
    }

    @Override
    @Deprecated
    public TermVectorsRequestBuilder prepareTermVector(String s, String s1, String s2) {
        return client.prepareTermVector(s, s1, s2);
    }

    @Override
    public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest) {
        return client.multiTermVectors(multiTermVectorsRequest);
    }

    @Override
    public void multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest, ActionListener<MultiTermVectorsResponse> actionListener) {
        client.multiTermVectors(multiTermVectorsRequest, actionListener);
    }

    @Override
    public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {
        return client.prepareMultiTermVectors();
    }

    @Override
    public ExplainRequestBuilder prepareExplain(String s, String s1, String s2) {
        return client.prepareExplain(s, s1, s2);
    }

    @Override
    public ActionFuture<ExplainResponse> explain(ExplainRequest explainRequest) {
        return client.explain(explainRequest);
    }

    @Override
    public void explain(ExplainRequest explainRequest, ActionListener<ExplainResponse> actionListener) {
        client.explain(explainRequest, actionListener);
    }

    @Override
    public ClearScrollRequestBuilder prepareClearScroll() {
        return client.prepareClearScroll();
    }

    @Override
    public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest clearScrollRequest) {
        return client.clearScroll(clearScrollRequest);
    }

    @Override
    public void clearScroll(ClearScrollRequest clearScrollRequest, ActionListener<ClearScrollResponse> actionListener) {
        client.clearScroll(clearScrollRequest, actionListener);
    }

    @Override
    public FieldStatsRequestBuilder prepareFieldStats() {
        return client.prepareFieldStats();
    }

    @Override
    public ActionFuture<FieldStatsResponse> fieldStats(FieldStatsRequest fieldStatsRequest) {
        return client.fieldStats(fieldStatsRequest);
    }

    @Override
    public void fieldStats(FieldStatsRequest fieldStatsRequest, ActionListener<FieldStatsResponse> actionListener) {
        client.fieldStats(fieldStatsRequest, actionListener);
    }

    @Override
    public Settings settings() {
        return client.settings();
    }

    @Override
    public Client filterWithHeader(Map<String, String> map) {
        return client.filterWithHeader(map);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(Action<Request, Response, RequestBuilder> action, Request request) {
        return client.execute(action, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(Action<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> actionListener) {
        client.execute(action, request, actionListener);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(Action<Request, Response, RequestBuilder> action) {
        return client.prepareExecute(action);
    }

    @Override
    public ThreadPool threadPool() {
        return client.threadPool();
    }

    @Override
    public void close() {
        client.close();
    }
    //endregion

    //region Fields
    private Logger logger;
    private Logger verboseLogger;

    private MetricRegistry metricRegistry;
    private Client client;

    private int operationId;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static MethodName.MDCWriter searchScroll = MethodName.of("searchScroll");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
