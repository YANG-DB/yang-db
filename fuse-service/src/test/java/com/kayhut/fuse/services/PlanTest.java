package com.kayhut.fuse.services;

import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class PlanTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp());

    @Test
    /**
     * execute query with expected plan result
     */
    public void plan() {
        given()
                .contentType("application/json")
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"plan\"," +
                        "\"query\": \"plan me a graph!\" " +
                        "}")
                .post("/fuse/plan")
                .then()
                .assertThat()
                .body(sameJSONAs("{\"id\":\"1\",\"metadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"plan\"},\"content\":{\"data\":\"Simple Plan\",\"id\":\"1\",\"results\":11,\"completed\":true}}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

}
