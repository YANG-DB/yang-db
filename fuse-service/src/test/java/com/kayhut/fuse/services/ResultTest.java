package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Ignore
public class ResultTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

     @Test
     @Ignore
    public void getResultById() {
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
                 .body(sameJSONAs("{\"queryMetadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"plan\"},\"results\":1333}")
                         .allowingExtraUnexpectedFields()
                         .allowingAnyArrayOrdering())
*/
                 .statusCode(201)
                 .contentType("application/json;charset=UTF-8");

         get("/fuse/result/1")
                .then()
                .assertThat()
/*
                .body(sameJSONAs("{\"queryMetadata\":{\"id\":\"1\",\"name\":\"hezi\",\"type\":\"path\"},\"results\":1333}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
*/
                .header("Access-Control-Allow-Origin", equalTo("*"))
                .header("Access-Control-Allow-Methods", equalTo("POST, GET, OPTIONS, DELETE, PATCH"))
                .header("Access-Control-Max-Age", equalTo("3600"))
                .header("Access-Control-Allow-Headers", "accept")
                .statusCode(302)
                .contentType("application/json;charset=UTF-8");

    }

}
