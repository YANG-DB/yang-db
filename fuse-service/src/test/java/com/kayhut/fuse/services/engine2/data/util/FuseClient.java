package com.kayhut.fuse.services.engine2.data.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Created by Roman on 11/05/2017.
 */
public class FuseClient {
    //region Constructor
    public FuseClient(String fuseUrl) {
        this.fuseUrl = fuseUrl;
    }
    //endregion

    //region Public Methods
    public FuseResourceInfo getFuseInfo() throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(this.fuseUrl)), FuseResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl, request)), QueryResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl, request)), QueryResourceInfo.class);
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        CreateCursorRequest request = new CreateCursorRequest();
        request.setCursorType(CreateCursorRequest.CursorType.paths);

        return new ObjectMapper().readValue(unwrap(postRequest(cursorStoreUrl, request)), CursorResourceInfo.class);
    }

    public PageResourceInfo postPage(String pageStoreUrl, int pageSize) throws IOException {
        CreatePageRequest request = new CreatePageRequest();
        request.setPageSize(pageSize);

        return new ObjectMapper().readValue(unwrap(postRequest(pageStoreUrl, request)), PageResourceInfo.class);
    }

    public PageResourceInfo getPage(String pageUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageUrl)), PageResourceInfo.class);
    }

    public Ontology getOntology(String ontologyUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(ontologyUrl)), Ontology.class);
    }

    public QueryResult getPageData(String pageDataUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageDataUrl)), QueryResult.class);
    }
    //endregion

    //region Protected Methods
    protected String postRequest(String url, Object body) throws IOException {
        return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .print();
    }

    protected String getRequest(String url) {
        return given().contentType("application/json")
                .get(url)
                .thenReturn()
                .print();
    }

    protected String unwrap(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>(){});
        return mapper.writeValueAsString(responseMap.get("data"));
    }
    //endregion

    //region Fields
    private String fuseUrl;
    //endregion
}
