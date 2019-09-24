package com.yangdb.fuse.client.elastic;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

//import com.yangdb.fuse.client.elastic.request_builders.IndexFuseRequestBuilder;
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
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequestBuilder;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexAction;
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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TransportFuseElasticClient implements BaseFuseElasticClient {

    private TransportClient transportClient;

    public List<TransportAddress> transportAddresses() {
        return transportClient.transportAddresses();
    }

    public List<DiscoveryNode> connectedNodes() {
        return transportClient.connectedNodes();
    }

    public List<DiscoveryNode> filteredNodes() {
        return transportClient.filteredNodes();
    }

    public List<DiscoveryNode> listedNodes() {
        return transportClient.listedNodes();
    }

    public TransportFuseElasticClient addTransportAddress(TransportAddress transportAddress) {
        transportClient = transportClient.addTransportAddress(transportAddress);
        return this;
    }

    public TransportFuseElasticClient addTransportAddresses(TransportAddress... transportAddress) {
        transportClient = transportClient.addTransportAddresses(transportAddress);
        return this;
    }

    public TransportFuseElasticClient removeTransportAddress(TransportAddress transportAddress) {
        transportClient = transportClient.removeTransportAddress(transportAddress);
        return this;
    }

    public TransportFuseElasticClient(Settings settings, Class<? extends Plugin>... plugins) {
        transportClient = new PreBuiltTransportClient(settings, plugins);
    }
    public TransportFuseElasticClient(Settings settings, Collection<Class<? extends Plugin>> plugins) {
        transportClient = new PreBuiltTransportClient(settings, plugins);
    }

    protected TransportFuseElasticClient(Settings settings, Settings defaultSettings, Collection<Class<? extends Plugin>> plugins, TransportClient.HostFailureListener hostFailureListener) {
        transportClient = new PreBuiltTransportClient(settings, plugins, hostFailureListener);
    }

    @Override
    public ActionFuture<IndexResponse> index(IndexRequest request) {
        return transportClient.index(request);
    }

    @Override
    public void index(IndexRequest request, ActionListener<IndexResponse> listener) {
        transportClient.index(request, listener);
    }

    @Override
    public IndexRequestBuilder prepareIndex() {
        return transportClient.prepareIndex();
    }

    @Override
    public IndexRequestBuilder prepareIndex(String index, String type) {
        return transportClient.prepareIndex(index, type);
    }

    @Override
    public IndexRequestBuilder prepareIndex(String index, String type, String id) {
        return transportClient.prepareIndex(index, type, id);
    }

    @Override
    public ActionFuture<UpdateResponse> update(UpdateRequest request) {
        return transportClient.update(request);
    }

    @Override
    public void update(UpdateRequest request, ActionListener<UpdateResponse> listener) {
        transportClient.update(request, listener);
    }

    @Override
    public UpdateRequestBuilder prepareUpdate() {
        return transportClient.prepareUpdate();
    }

    @Override
    public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {
        return transportClient.prepareUpdate(index, type, id);
    }

    @Override
    public ActionFuture<DeleteResponse> delete(DeleteRequest request) {
        return transportClient.delete(request);
    }

    @Override
    public void delete(DeleteRequest request, ActionListener<DeleteResponse> listener) {
        transportClient.delete(request, listener);
    }

    @Override
    public DeleteRequestBuilder prepareDelete() {
        return transportClient.prepareDelete();
    }

    @Override
    public DeleteRequestBuilder prepareDelete(String index, String type, String id) {
        return transportClient.prepareDelete(index, type, id);
    }

    @Override
    public ActionFuture<BulkResponse> bulk(BulkRequest request) {
        return transportClient.bulk(request);
    }

    @Override
    public void bulk(BulkRequest request, ActionListener<BulkResponse> listener) {
        transportClient.bulk(request, listener);
    }

    @Override
    public BulkRequestBuilder prepareBulk() {
        return transportClient.prepareBulk();
    }

    @Override
    public BulkRequestBuilder prepareBulk(String globalIndex, String globalType) {
        return transportClient.prepareBulk(globalIndex, globalType);
    }

    @Override
    public ActionFuture<GetResponse> get(GetRequest request) {
        return transportClient.get(request);
    }

    @Override
    public void get(GetRequest request, ActionListener<GetResponse> listener) {
        transportClient.get(request, listener);
    }

    @Override
    public GetRequestBuilder prepareGet() {
        return transportClient.prepareGet();
    }

    @Override
    public GetRequestBuilder prepareGet(String index, String type, String id) {
        return transportClient.prepareGet(index, type, id);
    }

    @Override
    public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest request) {
        return transportClient.multiGet(request);
    }

    @Override
    public void multiGet(MultiGetRequest request, ActionListener<MultiGetResponse> listener) {
        transportClient.multiGet(request, listener);
    }

    @Override
    public MultiGetRequestBuilder prepareMultiGet() {
        return transportClient.prepareMultiGet();
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest request) {
        return transportClient.search(request);
    }

    @Override
    public void search(SearchRequest request, ActionListener<SearchResponse> listener) {
        transportClient.search(request, listener);
    }

    @Override
    public SearchRequestBuilder prepareSearch(String... indices) {
        return transportClient.prepareSearch(indices);
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request) {
        return transportClient.searchScroll(request);
    }

    @Override
    public void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener) {
        transportClient.searchScroll(request, listener);
    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        return transportClient.prepareSearchScroll(scrollId);
    }

    @Override
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        return transportClient.multiSearch(request);
    }

    @Override
    public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
        transportClient.multiSearch(request, listener);
    }

    @Override
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return transportClient.prepareMultiSearch();
    }

    @Override
    public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest request) {
        return transportClient.termVectors(request);
    }

    @Override
    public void termVectors(TermVectorsRequest request, ActionListener<TermVectorsResponse> listener) {
        transportClient.termVectors(request, listener);
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors() {
        return transportClient.prepareTermVectors();
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors(String index, String type, String id) {
        return transportClient.prepareTermVectors(index, type, id);
    }

    @Override
    public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest request) {
        return transportClient.multiTermVectors(request);
    }

    @Override
    public void multiTermVectors(MultiTermVectorsRequest request, ActionListener<MultiTermVectorsResponse> listener) {
        transportClient.multiTermVectors(request, listener);
    }

    @Override
    public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {
        return transportClient.prepareMultiTermVectors();
    }

    @Override
    public ExplainRequestBuilder prepareExplain(String index, String type, String id) {
        return transportClient.prepareExplain(index, type, id);
    }

    @Override
    public ActionFuture<ExplainResponse> explain(ExplainRequest request) {
        return transportClient.explain(request);
    }

    @Override
    public void explain(ExplainRequest request, ActionListener<ExplainResponse> listener) {
        transportClient.explain(request, listener);
    }

    @Override
    public void clearScroll(ClearScrollRequest request, ActionListener<ClearScrollResponse> listener) {
        transportClient.clearScroll(request, listener);
    }

    @Override
    public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest request) {
        return transportClient.clearScroll(request);
    }

    @Override
    public ClearScrollRequestBuilder prepareClearScroll() {
        return transportClient.prepareClearScroll();
    }

    @Override
    public void fieldCaps(FieldCapabilitiesRequest request, ActionListener<FieldCapabilitiesResponse> listener) {
        transportClient.fieldCaps(request, listener);
    }

    @Override
    public ActionFuture<FieldCapabilitiesResponse> fieldCaps(FieldCapabilitiesRequest request) {
        return transportClient.fieldCaps(request);
    }

    @Override
    public FieldCapabilitiesRequestBuilder prepareFieldCaps(String... indices) {
        return transportClient.prepareFieldCaps(indices);
    }

    @Override
    public Client filterWithHeader(Map<String, String> headers) {
        return transportClient.filterWithHeader(headers);
    }

    @Override
    public AdminClient admin() {
        return transportClient.admin();
    }

    @Override
    public Settings settings() {
        return transportClient.settings();
    }

    @Override
    public Client getRemoteClusterClient(String clusterAlias) {
        return transportClient.getRemoteClusterClient(clusterAlias);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> ActionFuture<Response> execute(ActionType<Response> actionType, Request request) {
        return transportClient.execute(actionType, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void execute(ActionType<Response> actionType, Request request, ActionListener<Response> actionListener) {
        transportClient.execute(actionType, request, actionListener);
    }

    @Override
    public ThreadPool threadPool() {
        return transportClient.threadPool();
    }

    @Override
    public void close() {
        transportClient.close();
    }
}
