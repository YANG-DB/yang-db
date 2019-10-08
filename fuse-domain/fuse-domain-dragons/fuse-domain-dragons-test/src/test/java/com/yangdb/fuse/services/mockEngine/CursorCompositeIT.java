package com.yangdb.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.client.BaseFuseClient;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.yangdb.fuse.services.mockEngine.CompositeQueryTestUtils.*;

public class CursorCompositeIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
//        TestSuiteAPISuite.setup();
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void cursor() throws IOException {
        BaseFuseClient fuseClient = new BaseFuseClient("http://localhost:8888/fuse");

        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(Q1());
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
                        final QueryResourceInfo queryResourceInfo = fuseClient.unwrap(o.toString(), QueryResourceInfo.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.get("resourceUrl").toString().endsWith("/fuse/query/1"));
                        assertTrue(data.get("cursorStoreUrl").toString().endsWith("/fuse/query/1/cursor"));
                        assertEquals(1,queryResourceInfo.getInnerUrlResourceInfos().size());
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getAsgUrl().endsWith("fuse/query/1->q2/asg"));
                        assertTrue(queryResourceInfo.getInnerUrlResourceInfos().get(0).getCursorStoreUrl().endsWith("fuse/query/1->q2/cursor"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cursor resource
        AtomicReference<String> outerCursorId = new AtomicReference<>();
        AtomicReference<String> innerCursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreatePathsCursorRequest();
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(cursorRequest)
                .post("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        final CursorResourceInfo cursorResourceInfo = fuseClient.unwrap(o.toString(), CursorResourceInfo.class);
                        outerCursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        assertTrue(cursorResourceInfo.getPageStoreUrl().endsWith("query/1/cursor/1/page"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor/"+outerCursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
        //get inner cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2/cursor/"+outerCursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {

                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/plan/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String data = fuseClient.unwrap(o.toString());
                        return data!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2/plan/print")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String data = fuseClient.unwrap(o.toString());
                        return data!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void deleteCursor() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(Q1());
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
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");


        //create cuyrsor resource
        AtomicReference<String> cursorId = new AtomicReference<>();
        CreateCursorRequest cursorRequest = new CreatePathsCursorRequest();
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .body(cursorRequest)
                .post("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorRequest"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");


        //delete cursor
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .delete("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");


        //get cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");

        //get inner cursor resource by id
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/query/1->q2/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
    }

}
