package com.kayhut.fuse.test.util;

import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.resourceInfo.*;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Created by Roman on 11/05/2017.
 */
public class FuseClient {
    //region Constructor
    public FuseClient(String fuseUrl) {
        this.fuseUrl = fuseUrl;
        RestAssured.config = RestAssured.config().logConfig(new LogConfig(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {

            }
        }), true));
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
            CreateCursorRequest cursorRequest,
            int pageSize) throws IOException {

        CreateQueryAndFetchRequest request = new CreateQueryAndFetchRequest(
                id,
                name,
                query,
                cursorRequest,
                new CreatePageRequest(pageSize)
        );

        return new ObjectMapper().readValue(unwrap(postRequest(queryStoreUrl + "?fetch=true", request)), QueryCursorPageResourceInfo.class);
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl) throws IOException {
        return this.postCursor(cursorStoreUrl, new CreatePathsCursorRequest());
    }

    public CursorResourceInfo postCursor(String cursorStoreUrl, CreateCursorRequest cursorRequest) throws IOException {
        return new ObjectMapper().readValue(unwrap(postRequest(cursorStoreUrl, cursorRequest)), CursorResourceInfo.class);
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

    public AssignmentsQueryResult getPageData(String pageDataUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageDataUrl)), AssignmentsQueryResult.class);
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
                .thenReturn().asString();
                //.print();
    }

    protected String getRequest(String url) {
        return given().contentType("application/json")
                .get(url)
                .thenReturn().asString();
                //.print();
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
