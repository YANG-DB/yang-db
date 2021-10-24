package com.yangdb.fuse.executor.mock.opensearch;

/*-
 * #%L
 * fuse-dv-core
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

import org.opensearch.action.*;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteRequestBuilder;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.explain.ExplainRequest;
import org.opensearch.action.explain.ExplainRequestBuilder;
import org.opensearch.action.explain.ExplainResponse;
import org.opensearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.opensearch.action.fieldcaps.FieldCapabilitiesRequestBuilder;
import org.opensearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.opensearch.action.get.*;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexRequestBuilder;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.*;
import org.opensearch.action.termvectors.*;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateRequestBuilder;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.AdminClient;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;
import org.opensearch.threadpool.ThreadPool;

import java.util.Map;

/**
 * Created by roman.margolis on 08/11/2017.
 */
public class MockClient implements Client {

    @Override
    public AdminClient admin() {
        return null;
    }

    @Override
    public ActionFuture<IndexResponse> index(IndexRequest indexRequest) {
        return null;
    }

    @Override
    public void index(IndexRequest indexRequest, ActionListener<IndexResponse> actionListener) {

    }

    @Override
    public IndexRequestBuilder prepareIndex() {
        return null;
    }

    @Override
    public ActionFuture<UpdateResponse> update(UpdateRequest updateRequest) {
        return null;
    }

    @Override
    public void update(UpdateRequest updateRequest, ActionListener<UpdateResponse> actionListener) {

    }

    @Override
    public UpdateRequestBuilder prepareUpdate() {
        return null;
    }

    @Override
    public UpdateRequestBuilder prepareUpdate(String s, String s1, String s2) {
        return null;
    }

    @Override
    public IndexRequestBuilder prepareIndex(String s, String s1) {
        return null;
    }

    @Override
    public IndexRequestBuilder prepareIndex(String s, String s1, String s2) {
        return null;
    }

    @Override
    public ActionFuture<DeleteResponse> delete(DeleteRequest deleteRequest) {
        return null;
    }

    @Override
    public void delete(DeleteRequest deleteRequest, ActionListener<DeleteResponse> actionListener) {

    }

    @Override
    public DeleteRequestBuilder prepareDelete() {
        return null;
    }

    @Override
    public DeleteRequestBuilder prepareDelete(String s, String s1, String s2) {
        return null;
    }

    @Override
    public ActionFuture<BulkResponse> bulk(BulkRequest bulkRequest) {
        return null;
    }

    @Override
    public void bulk(BulkRequest bulkRequest, ActionListener<BulkResponse> actionListener) {

    }

    @Override
    public FieldCapabilitiesRequestBuilder prepareFieldCaps(String... indices) {
        return null;
    }

    @Override
    public ActionFuture<FieldCapabilitiesResponse> fieldCaps(FieldCapabilitiesRequest request) {
        return null;
    }

    @Override
    public void fieldCaps(FieldCapabilitiesRequest request, ActionListener<FieldCapabilitiesResponse> listener) {
    }

    @Override
    public BulkRequestBuilder prepareBulk() {
        return null;
    }

    @Override
    public BulkRequestBuilder prepareBulk(String globalIndex, String globalType) {
        return null;
    }

    @Override
    public ActionFuture<GetResponse> get(GetRequest getRequest) {
        return null;
    }

    @Override
    public void get(GetRequest getRequest, ActionListener<GetResponse> actionListener) {

    }

    @Override
    public GetRequestBuilder prepareGet() {
        return null;
    }

    @Override
    public GetRequestBuilder prepareGet(String s, String s1, String s2) {
        return null;
    }

    @Override
    public ActionFuture<MultiGetResponse> multiGet(MultiGetRequest multiGetRequest) {
        return null;
    }

    @Override
    public void multiGet(MultiGetRequest multiGetRequest, ActionListener<MultiGetResponse> actionListener) {

    }

    @Override
    public MultiGetRequestBuilder prepareMultiGet() {
        return null;
    }

    @Override
    public ActionFuture<SearchResponse> search(SearchRequest searchRequest) {
        return null;
    }

    @Override
    public void search(SearchRequest searchRequest, ActionListener<SearchResponse> actionListener) {

    }

    @Override
    public SearchRequestBuilder prepareSearch(String... strings) {
        return null;
    }

    @Override
    public ActionFuture<SearchResponse> searchScroll(SearchScrollRequest searchScrollRequest) {
        return null;
    }

    @Override
    public void searchScroll(SearchScrollRequest searchScrollRequest, ActionListener<SearchResponse> actionListener) {

    }

    @Override
    public SearchScrollRequestBuilder prepareSearchScroll(String s) {
        return null;
    }

    @Override
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest multiSearchRequest) {
        return null;
    }

    @Override
    public void multiSearch(MultiSearchRequest multiSearchRequest, ActionListener<MultiSearchResponse> actionListener) {

    }

    @Override
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return null;
    }

    @Override
    public ActionFuture<TermVectorsResponse> termVectors(TermVectorsRequest termVectorsRequest) {
        return null;
    }

    @Override
    public void termVectors(TermVectorsRequest termVectorsRequest, ActionListener<TermVectorsResponse> actionListener) {

    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors() {
        return null;
    }

    @Override
    public TermVectorsRequestBuilder prepareTermVectors(String s, String s1, String s2) {
        return null;
    }

    @Override
    public ActionFuture<MultiTermVectorsResponse> multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest) {
        return null;
    }

    @Override
    public void multiTermVectors(MultiTermVectorsRequest multiTermVectorsRequest, ActionListener<MultiTermVectorsResponse> actionListener) {

    }

    @Override
    public MultiTermVectorsRequestBuilder prepareMultiTermVectors() {
        return null;
    }

    @Override
    public ExplainRequestBuilder prepareExplain(String s, String s1, String s2) {
        return null;
    }

    @Override
    public ActionFuture<ExplainResponse> explain(ExplainRequest explainRequest) {
        return null;
    }

    @Override
    public void explain(ExplainRequest explainRequest, ActionListener<ExplainResponse> actionListener) {

    }

    @Override
    public ClearScrollRequestBuilder prepareClearScroll() {
        return null;
    }

    @Override
    public ActionFuture<ClearScrollResponse> clearScroll(ClearScrollRequest clearScrollRequest) {
        return null;
    }

    @Override
    public void clearScroll(ClearScrollRequest clearScrollRequest, ActionListener<ClearScrollResponse> actionListener) {

    }

    @Override
    public Settings settings() {
        return null;
    }

    @Override
    public Client filterWithHeader(Map<String, String> map) {
        return null;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> ActionFuture<Response> execute(ActionType<Response> action, Request request) {
        return null;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse> void execute(ActionType<Response> action, Request request, ActionListener<Response> listener) {

    }

    @Override
    public ThreadPool threadPool() {
        return null;
    }

    @Override
    public void close() {

    }
}
