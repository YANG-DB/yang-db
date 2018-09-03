package com.kayhut.fuse.services.mockEngine;

import com.kayhut.fuse.services.TestsConfiguration;
import io.restassured.http.Header;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class ApiDescriptorTest {
    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    @Ignore
    /**
     * execute query with expected plan result
     */
    public void api() {
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse")
                .then()
                .assertThat()
                .body(sameJSONAs("{ \"data\":{\"resourceUrl\":\"/fuse\",\"healthUrl\":\"/fuse/health\",\"queryStoreUrl\":\"/fuse/query\",\"searchStoreUrl\":\"/fuse/search\",\"catalogStoreUrl\":\"/fuse/catalog/ontology\"}")
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering())
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void checkHealth() {
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/health")
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

