package com.kayhut.fuse.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.kayhut.fuse.services.TestUtils.loadQuery;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class CursorTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

    @Test
    /**
     * execute query with expected plan result
     */
    public void cursor() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
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
        CreateCursorRequest cursorRequest = new CreateCursorRequest();
        cursorRequest.setCursorType(CreateCursorRequest.CursorType.graph);
        given()
                .contentType("application/json")
                .body(cursorRequest)
                .post("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorType"));
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
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorType"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(302)
                .contentType("application/json;charset=UTF-8");


    }

    @Test
    public void deleteCursor() throws IOException {
        //query request
        CreateQueryRequest request = new CreateQueryRequest();
        request.setId("1");
        request.setName("test");
        request.setQuery(loadQuery("Q001.json"));
        //submit query
        given()
                .contentType("application/json")
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
        CreateCursorRequest cursorRequest = new CreateCursorRequest();
        cursorRequest.setCursorType(CreateCursorRequest.CursorType.graph);
        given()
                .contentType("application/json")
                .body(cursorRequest)
                .post("/fuse/query/1/cursor")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        cursorId.set(data.get("resourceId").toString());
                        assertTrue(data.containsKey("cursorType"));
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
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        Map data = (Map) contentResponse.getData();
                        assertTrue(data.containsKey("cursorType"));
                        assertTrue(data.containsKey("pageStoreUrl"));
                        return contentResponse.getData()!=null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(302)
                .contentType("application/json;charset=UTF-8");


        //delete cursor
        given()
                .contentType("application/json")
                .delete("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .statusCode(202)
                .contentType("application/json;charset=UTF-8");


        //get cursor resource by id
        given()
                .contentType("application/json")
                .get("/fuse/query/1/cursor/"+cursorId.get())
                .then()
                .assertThat()
                .statusCode(404)
                .contentType("application/json;charset=UTF-8");
    }

}
