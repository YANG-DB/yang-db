package com.kayhut.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.client.BaseFuseClient;
import com.kayhut.fuse.model.asgQuery.AsgCompositeQuery;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryAssert;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.PlanTraceOptions;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.kayhut.fuse.services.TestsConfiguration;
import com.kayhut.test.data.DragonsOntology;
import io.restassured.http.Header;
import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryTest {
    @Before
    public void before() {
//        TestSuite.setup();
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    /**
     * execute query with expected path result
     */
    @Test
    public void queryCreate() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void queryCreateVerbose() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        request.setPlanTraceOptions(PlanTraceOptions.of(PlanTraceOptions.Level.verbose));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void createQueryAndFetch() throws IOException {
        CreatePageRequest createPageRequest = new CreatePageRequest();
        createPageRequest.setPageSize(10);

        CreateCursorRequest createCursorRequest = new CreatePathsCursorRequest();
        createCursorRequest.setCreatePageRequest(createPageRequest);

        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        request.setCreateCursorRequest(createCursorRequest);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().contains("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("cursorRequest"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("pageStoreUrl"));
                        assertTrue(((Map)(((List) data.get("cursorResourceInfos")).get(0))).containsKey("pageResourceInfos"));
                        assertTrue(((Map)((List)((Map)(((List) data.get("cursorResourceInfos")).get(0))).get("pageResourceInfos")).get(0)).containsKey("dataUrl"));
                        assertTrue(((Map)((List)((Map)(((List) data.get("cursorResourceInfos")).get(0))).get("pageResourceInfos")).get(0)).containsKey("actualPageSize"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void queryFaultyCreate() throws IOException {
        Query query = new Query();
        ETyped typed_1 = new ETyped(1, "tag", DragonsOntology.PERSON.type, 2, 0);
        ETyped typed_2 = new ETyped(2, "tag", DragonsOntology.PERSON.type, 0, 0);
        Start start = new Start(0, 1);
        query.setElements(Arrays.asList(start, typed_1, typed_2));
        query.setOnt("Dragons");
        query.setName("Q1");


        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl") == null);
                        assertTrue(data.get("cursorStoreUrl") == null);
                        Map errorContent = (Map) data.get("error");
                        assertTrue(errorContent.get("errorCode").toString().endsWith(Query.class.getSimpleName()));
                        assertTrue(errorContent.get("errorDescription").toString().contains("Ontology Contains two adjacent Entities without relation inside"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(500)
                .contentType("application/json;charset=UTF-8");

    }

    @Test
    public void queryCreateAndFetchResource() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndFetchV1QueryResource() throws IOException {
        BaseFuseClient fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(TestUtils.loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/v1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        Query data = fuseClient.unwrap(o.toString(), Query.class);
                        QueryAssert.assertEquals(data, request.getQuery());
                        return data != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndFetchAsgQueryResource() throws IOException {
        BaseFuseClient fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        //query request
        Query query = TestUtils.loadQuery("Q001.json");
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertTrue(data.get("v1QueryUrl").toString().endsWith("/fuse/query/1/v1"));
                        assertTrue(data.get("asgUrl").toString().endsWith("/fuse/query/1/asg"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        final AsgQuery[] asgQuery = {null};
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/asg")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        asgQuery[0] = fuseClient.unwrap(o.toString(), AsgCompositeQuery.class);
                        assertTrue(asgQuery[0].getName() != null);
                        assertTrue(asgQuery[0].getOnt() != null);
                        assertTrue(AsgQueryUtil.elements(asgQuery[0]).size() >= request.getQuery().getElements().size());
                        return asgQuery[0] != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/asg/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher((Object o) -> {
                    try {
                        String data = StringUtils.strip(fuseClient.unwrap(o.toString()), "\"");
                        assertEquals(AsgQueryDescriptor.print(asgQuery[0]).replace("\n", "\\n"), data);
                        return data != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void queryCreateAndDeleteResource() throws IOException {
        BaseFuseClient fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        //query request
        Query query = TestUtils.loadQuery("Q001.json");
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(query);
        //submit query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(request)
                .post("/fuse/query")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //get query resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/v1/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        assertEquals(QueryDescriptor.print(query).replace("\n", "\\n"), StringUtils.strip(fuseClient.unwrap(o.toString()), "\""));
                        return fuseClient.unwrap(o.toString()) != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

        //delete query
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .delete("/fuse/query/1")
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");
    }

}
