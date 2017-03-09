package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

@Ignore
public class QueryTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

    @Test
    public void checkHealth() {
        get("/fuse/health")
                .then()
                .assertThat()
                .body(equalTo("\"Alive And Well...\""))
                .header("Access-Control-Allow-Origin", equalTo("*"))
                .header("Access-Control-Allow-Methods", equalTo("POST, GET, OPTIONS, DELETE, PATCH"))
                .header("Access-Control-Max-Age", equalTo("3600"))
                .header("Access-Control-Allow-Headers", "accept")
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    /**
     * execute query with expected path result
     */
    @Test
    @Ignore
    public void queryWithPathResults() {
        given()
                .contentType("application/json")
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"path\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query/path")
                .then()
                .assertThat()
/*
                .body(sameJSONAs("{\"queryMetadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"graph\",\"content\":{\"completed\":true,\"url\":\"http://localhost:8080/fuse/result/1\",\"id\":\"1\"}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
*/
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");

    }

    /**
     * execute query with expected graph result
     */
    @Test
    @Ignore
    public void queryWithGraphResults() {
        given()
                .contentType("application/json")
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"graph\"," +
                        "\"query\": \"build me a graph!\" " +
                        "}")
                .post("/fuse/query/graph")
                .then()
                .assertThat()
/*
                .body(sameJSONAs("{\"queryMetadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"graph\",\"content\":{\"completed\":true,\"url\":\"http://localhost:8080/fuse/result/1\",\"id\":\"1\"}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
*/
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }


}
