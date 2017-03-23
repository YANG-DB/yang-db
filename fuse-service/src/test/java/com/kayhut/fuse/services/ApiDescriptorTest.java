package com.kayhut.fuse.services;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ApiDescriptorTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

    @Test
    /**
     * execute query with expected plan result
     */
    public void api() {
        given()
                .contentType("application/json")
                .get("/fuse")
                .then()
                .assertThat()
                .body(sameJSONAs("{\"resourceUrl\":\"/fuse\",\"healthUrl\":\"/fuse/health\",\"queryStoreUrl\":\"/fuse/query\",\"searchStoreUrl\":\"/fuse/search\",\"catalogStoreUrl\":\"/fuse/catalog\"}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

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

}

