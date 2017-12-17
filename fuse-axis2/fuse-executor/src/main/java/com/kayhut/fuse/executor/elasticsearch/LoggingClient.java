package com.kayhut.fuse.executor.elasticsearch;

import com.kayhut.fuse.dispatcher.logging.LogMessage;
import org.elasticsearch.action.*;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.exists.ExistsRequest;
import org.elasticsearch.action.exists.ExistsRequestBuilder;
import org.elasticsearch.action.exists.ExistsResponse;
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
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.delete.DeleteIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.get.GetIndexedScriptResponse;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequest;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptRequestBuilder;
import org.elasticsearch.action.indexedscripts.put.PutIndexedScriptResponse;
import org.elasticsearch.action.percolate.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.suggest.SuggestRequest;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.action.termvectors.*;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.support.Headers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 13/12/2017.
 */
public class LoggingClient implements Client {
    //region Constructors
    public LoggingClient(Client client) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.client = client;

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
    public PutIndexedScriptRequestBuilder preparePutIndexedScript() {
        return client.preparePutIndexedScript();
    }

    @Override
    public PutIndexedScriptRequestBuilder preparePutIndexedScript(String s, String s1, String s2) {
        return client.preparePutIndexedScript(s, s1, s2);
    }

    @Override
    public void deleteIndexedScript(DeleteIndexedScriptRequest deleteIndexedScriptRequest, ActionListener<DeleteIndexedScriptResponse> actionListener) {
        client.deleteIndexedScript(deleteIndexedScriptRequest, actionListener);
    }

    @Override
    public ActionFuture<DeleteIndexedScriptResponse> deleteIndexedScript(DeleteIndexedScriptRequest deleteIndexedScriptRequest) {
        return client.deleteIndexedScript(deleteIndexedScriptRequest);
    }

    @Override
    public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript() {
        return client.prepareDeleteIndexedScript();
    }

    @Override
    public DeleteIndexedScriptRequestBuilder prepareDeleteIndexedScript(String s, String s1) {
        return client.prepareDeleteIndexedScript(s, s1);
    }

    @Override
    public void putIndexedScript(PutIndexedScriptRequest putIndexedScriptRequest, ActionListener<PutIndexedScriptResponse> actionListener) {
        client.putIndexedScript(putIndexedScriptRequest, actionListener);
    }

    @Override
    public ActionFuture<PutIndexedScriptResponse> putIndexedScript(PutIndexedScriptRequest putIndexedScriptRequest) {
        return client.putIndexedScript(putIndexedScriptRequest);
    }

    @Override
    public GetIndexedScriptRequestBuilder prepareGetIndexedScript() {
        return client.prepareGetIndexedScript();
    }

    @Override
    public GetIndexedScriptRequestBuilder prepareGetIndexedScript(String s, String s1) {
        return client.prepareGetIndexedScript(s, s1);
    }

    @Override
    public void getIndexedScript(GetIndexedScriptRequest getIndexedScriptRequest, ActionListener<GetIndexedScriptResponse> actionListener) {
        client.getIndexedScript(getIndexedScriptRequest, actionListener);
    }

    @Override
    public ActionFuture<GetIndexedScriptResponse> getIndexedScript(GetIndexedScriptRequest getIndexedScriptRequest) {
        return client.getIndexedScript(getIndexedScriptRequest);
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
    @Deprecated
    public ActionFuture<CountResponse> count(CountRequest countRequest) {
        return client.count(countRequest);
    }

    @Override
    @Deprecated
    public void count(CountRequest countRequest, ActionListener<CountResponse> actionListener) {
        client.count(countRequest, actionListener);
    }

    @Override
    @Deprecated
    public CountRequestBuilder prepareCount(String... strings) {
        return client.prepareCount(strings);
    }

    @Override
    @Deprecated
    public ActionFuture<ExistsResponse> exists(ExistsRequest existsRequest) {
        return client.exists(existsRequest);
    }

    @Override
    @Deprecated
    public void exists(ExistsRequest existsRequest, ActionListener<ExistsResponse> actionListener) {
        client.exists(existsRequest, actionListener);
    }

    @Override
    @Deprecated
    public ExistsRequestBuilder prepareExists(String... strings) {
        return client.prepareExists(strings);
    }

    @Override
    public ActionFuture<SuggestResponse> suggest(SuggestRequest suggestRequest) {
        return client.suggest(suggestRequest);
    }

    @Override
    public void suggest(SuggestRequest suggestRequest, ActionListener<SuggestResponse> actionListener) {
        client.suggest(suggestRequest, actionListener);
    }

    @Override
    public SuggestRequestBuilder prepareSuggest(String... strings) {
        return client.prepareSuggest(strings);
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest searchRequest) {
        int operationId = this.operationId++;

        try {
            new LogMessage(this.logger, LogMessage.Level.trace, "#{} start search", operationId).log();
            ActionFuture<SearchResponse> future = client.search(searchRequest);
            if (ListenableActionFuture.class.isAssignableFrom(future.getClass())) {
                ListenableActionFuture<SearchResponse> listenableActionFuture = (ListenableActionFuture<SearchResponse>)future;
                listenableActionFuture.addListener(new LoggingActionListener<>(
                        new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish search", operationId),
                        new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search: {}", operationId)));
            }
            return future;
        } catch (Exception ex) {
            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search: {}", operationId, ex).log();
            return null;
        }
    }

    @Override
    public void search(SearchRequest searchRequest, ActionListener<SearchResponse> actionListener) {
        int operationId = this.operationId++;

        try {
            new LogMessage(this.logger, LogMessage.Level.trace, "#{} start search", operationId).log();
            client.search(
                    searchRequest,
                    new LoggingActionListener<>(
                            new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish search", operationId),
                            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search: {}", operationId),
                            actionListener,
                            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search actionListener: {}", operationId)));
        } catch (Exception ex) {
            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search: {}", operationId, ex).log();
        }
    }

    @Override
    public SearchRequestBuilder prepareSearch(String... strings) {
        int operationId = this.operationId++;

        return new LoggingSearchRequestBuilder(
                this,
                SearchAction.INSTANCE,
                new LogMessage(this.logger, LogMessage.Level.trace, "#{} start search", operationId),
                new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish search", operationId),
                new LogMessage(this.logger, LogMessage.Level.error, "#{} failed search: {}", operationId))
                .setIndices(strings);
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest searchScrollRequest) {
        int operationId = this.operationId++;

        try {
            new LogMessage(this.logger, LogMessage.Level.trace, "#{} start searchScroll", operationId).log();
            ActionFuture<SearchResponse> future = client.searchScroll(searchScrollRequest);
            if (ListenableActionFuture.class.isAssignableFrom(future.getClass())) {
                ListenableActionFuture<SearchResponse> listenableActionFuture = (ListenableActionFuture<SearchResponse>)future;
                listenableActionFuture.addListener(new LoggingActionListener<>(
                        new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish searchScroll", operationId),
                        new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll: {}", operationId)));
            }
            return future;
        } catch (Exception ex) {
            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll: {}", operationId, ex).log();
            return null;
        }
    }

    @Override
    public void searchScroll(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> actionListener) {
        int operationId = this.operationId++;

        try {
            new LogMessage(this.logger, LogMessage.Level.trace, "#{} start searchScroll", operationId).log();
            client.searchScroll(
                    searchScrollRequest,
                    new LoggingActionListener<>(
                            new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish searchScroll", operationId),
                            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll: {}", operationId),
                            actionListener,
                            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll actionListener: {}", operationId)));
        } catch (Exception ex) {
            new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll: {}", operationId, ex).log();
        }
    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        int operationId = this.operationId++;

        return new LoggingSearchScrollRequestBuilder(
                this,
                SearchScrollAction.INSTANCE,
                scrollId,
                new LogMessage(this.logger, LogMessage.Level.trace, "#{} start searchScroll", operationId),
                new LogMessage(this.logger, LogMessage.Level.trace, "#{} finish searchScroll", operationId),
                new LogMessage(this.logger, LogMessage.Level.error, "#{} failed searchScroll: {}", operationId));
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
    public ActionFuture<PercolateResponse> percolate(PercolateRequest percolateRequest) {
        return client.percolate(percolateRequest);
    }

    @Override
    public void percolate(PercolateRequest percolateRequest, ActionListener<PercolateResponse> actionListener) {
        client.percolate(percolateRequest, actionListener);
    }

    @Override
    public PercolateRequestBuilder preparePercolate() {
        return client.preparePercolate();
    }

    @Override
    public ActionFuture<MultiPercolateResponse> multiPercolate(MultiPercolateRequest multiPercolateRequest) {
        return client.multiPercolate(multiPercolateRequest);
    }

    @Override
    public void multiPercolate(MultiPercolateRequest multiPercolateRequest, ActionListener<MultiPercolateResponse> actionListener) {
        client.multiPercolate(multiPercolateRequest, actionListener);
    }

    @Override
    public MultiPercolateRequestBuilder prepareMultiPercolate() {
        return client.prepareMultiPercolate();
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
    public Headers headers() {
        return client.headers();
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
    private Client client;
    private Logger logger;

    private int operationId;
    //endregion
}
