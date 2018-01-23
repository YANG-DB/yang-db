package com.kayhut.fuse.services.engine2.data.util;

import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.*;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.*;
import io.restassured.response.ResponseBody;

import java.io.IOException;
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
        return postQuery(queryStoreUrl,query, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        request.setPlanTraceOptions(planTraceOptions);
        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl, request)), QueryResourceInfo.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, String id, String name) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId(id);
        request.setName(name);
        request.setQuery(query);
        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl, request)), QueryResourceInfo.class);
    }

    public QueryCursorPageResourceInfo postQueryAndFetch(
            String queryStoreUrl,
            Query query,
            String id,
            String name,
            CreateCursorRequest.CursorType cursorType,
            int pageSize) throws IOException {

        CreateQueryAndFetchRequest request = new CreateQueryAndFetchRequest(
                id,
                name,
                query,
                new CreateCursorRequest(cursorType),
                new CreatePageRequest(pageSize)
        );

        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl + "?fetch=true", request)), QueryCursorPageResourceInfo.class);
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        return this.postCursor(cursorStoreUrl, CreateCursorRequest.CursorType.paths);
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest.CursorType cursorType) throws IOException {
        CreateCursorRequest request = new CreateCursorRequest();
        request.setCursorType(cursorType);

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

    public String getPlan(String planUrl) throws IOException {
        return getRequest(planUrl);
    }

    public Plan getPlanObject(String planUrl) throws IOException {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = unwrapDouble(getRequest(planUrl));
        return planWithCost.getPlan();

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

    protected <T> T unwrapDouble(String response) throws IOException {
        return ((ContentResponse<T>)JsonReader.jsonToJava((String)JsonReader.jsonToJava(response))).getData();
    }
    //endregion

    //region Fields
    private String fuseUrl;
    //endregion
}
