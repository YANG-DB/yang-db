package com.kayhut.fuse.services;

import com.kayhut.fuse.services.FuseApp;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class SearchTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp());

    @Test
    @Ignore
    /**
     * execute query with expected plan result
     */
    public void search() {
        given()
                .contentType("application/json")
                .body("{\"id\":1," +
                        "\"name\": \"hezi\"," +
                        "\"type\": \"search\"," +
                        "\"query\": \"plan me a graph!\" " +
                        "}")
                .post("/fuse/search")
                .then()
                .assertThat()
/*
                .body(sameJSONAs("{\"queryMetadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"plan\"},\"results\":1333}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
*/
                .statusCode(201)
                .contentType("application/json;charset=UTF-8");
    }

}
