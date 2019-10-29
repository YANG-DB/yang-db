package com.yangdb.fuse.client;

/*-
 * #%L
 * fuse-core
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



import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.resourceInfo.*;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.PlanTraceOptions;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static io.restassured.RestAssured.given;

public interface FuseClient {
    //region Protected Methods

    static String postRequest(String url, Object body) throws IOException {
      return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .asString();
    }

    static String getRequest(String url) {
        return getRequest(url, "application/json");
    }

    static String getRequest(String url, String contentType) {
        return given().contentType(contentType)
                .get(url)
                .thenReturn()
                .asString();
    }

    static <T> T unwrapDouble(String response) throws IOException {
        return ((ContentResponse<T>) JsonReader.jsonToJava((String) JsonReader.jsonToJava(response))).getData();
    }

    //region Public Methods
    FuseResourceInfo getFuseInfo() throws IOException;

    Object getId(String name, int numIds) throws IOException;

    /**
     * upsert data file (logical graph model) according to technical id
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     */
    ResultResourceInfo upsertData(String ontology, URL resource) throws IOException;

    /**
     * load data file (logical graph model) according to technical id
     * @param ontology
     * @param model
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadData(String ontology, LogicalGraphModel model) throws IOException;

    /**
     * load data file (logical graph model) according to technical id
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     */
    ResultResourceInfo loadData(String ontology, URL resource) throws IOException;

    /**
     *
     * upload data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo uploadFile(String ontology, URL resource) throws IOException, URISyntaxException;

    /**
     *
     * upsert data file (logical graph model) according to technical id
     *
     * @param ontology
     * @param resource
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    ResultResourceInfo upsertFile(String ontology, URL resource) throws IOException, URISyntaxException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, CreateQueryRequest request) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, String query, String ontology, PlanTraceOptions planTraceOptions) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException;

    QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name, CreateCursorRequest createCursorRequest) throws IOException;

    /**
     * call "fuse/load/ontology/{id}/init"
     * @param ontology
     * @return
     */
    String initIndices(String ontology);

    /**
     * call "fuse/load/ontology/{id}/drop"
     * @param ontology
     * @return
     */
    String dropIndices(String ontology);

    CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException;

    CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException;

    PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException;

    PageResourceInfo getPage(String pageUrl, String pageId) throws IOException;

    PageResourceInfo getPage(String pageUrl) throws IOException;

    QueryResourceInfo getQuery(String queryUrl, String queryId) throws IOException;

    CursorResourceInfo getCursor(String cursorUrl, String cursorId) throws IOException;

    Ontology getOntology(String ontologyUrl) throws IOException;

    Query getQuery(String queryUrl,Class<? extends Query> klass) throws IOException;

    QueryResultBase getPageData(String pageDataUrl, TypeReference typeReference) throws IOException ;

    QueryResultBase getPageData(String pageDataUrl) throws IOException;

    String getPageDataPlain(String pageDataUrl) throws IOException;

    String getPlan(String planUrl) throws IOException;

    Plan getPlanObject(String planUrl) throws IOException;

    Long getFuseSnowflakeId() throws IOException;

    String getFuseUrl();

    String deleteQuery(QueryResourceInfo queryResourceInfo);

    boolean shutdown();
}
