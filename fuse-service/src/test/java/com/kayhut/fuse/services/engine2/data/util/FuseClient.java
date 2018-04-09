package com.kayhut.fuse.services.engine2.data.util;

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
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.*;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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

    public Object getId(String name, int numIds) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(this.fuseUrl + "/idgen/" + name + "?numIds=" + numIds)), Map.class);
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query) throws IOException {
        return postQuery(queryStoreUrl,query, PlanTraceOptions.of(PlanTraceOptions.Level.none));
    }

    public QueryResourceInfo postQuery(String queryStoreUrl, Query query, PlanTraceOptions planTraceOptions) throws IOException {
        CreateQueryRequest request = new CreateQueryRequest();
        String id = UUID.randomUUID().toString();
        request.setId(id);
        request.setName(id);
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

    public QueryResultBase getPageData(String pageDataUrl) throws IOException {
        return new ObjectMapper().readValue(unwrap(getRequest(pageDataUrl)), QueryResultBase.class);
    }

    public String getPlan(String planUrl) throws IOException {
        return getRequest(planUrl);
    }

    public Plan getPlanObject(String planUrl) throws IOException {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = unwrapDouble(getRequest(planUrl));
        return planWithCost.getPlan();

    }

    public Query getQueryObject(String v1QueryUrl) throws IOException {
        return unwrapDouble(getRequest(v1QueryUrl));

    }
    //endregion

    //region Protected Methods
    public static String postRequest(String url, Object body) throws IOException {
        return given().contentType("application/json")
                .body(body)
                .post(url)
                .thenReturn()
                .print();
    }

    public static String getRequest(String url) {
        return given().contentType("application/json")
                .get(url)
                .thenReturn()
                .print();
    }

    public static String unwrap(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(response, new TypeReference<Map<String, Object>>(){});
        return mapper.writeValueAsString(responseMap.get("data"));
    }

    public  static <T> T unwrapDouble(String response) throws IOException {
        return ((ContentResponse<T>)JsonReader.jsonToJava((String)JsonReader.jsonToJava(response))).getData();
    }
    //endregion

    //region Fields
    private String fuseUrl;
    //endregion
}
