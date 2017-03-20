package com.kayhut.fuse.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.kayhut.fuse.services.TestUtils.loadQuery;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class PlanTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

    @Test
    /**
     * execute query with expected plan result
     */
    public void plan() throws IOException {
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


        //get query resource by id
        given()
                .contentType("application/json")
                .get("/fuse/query/1/plan")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        return o.toString().contains("ops");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");

    }

}
