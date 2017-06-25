package com.kayhut.fuse.services.mockEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.services.TestsConfiguration;
import org.jooby.test.JoobyRule;
import org.junit.*;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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

