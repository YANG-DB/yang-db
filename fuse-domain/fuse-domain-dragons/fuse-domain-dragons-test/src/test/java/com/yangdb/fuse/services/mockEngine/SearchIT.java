package com.yangdb.fuse.services.mockEngine;

import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.*;

import static io.restassured.RestAssured.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

@Ignore
public class SearchIT implements BaseITMarker {
    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    @Test
    @Ignore
    /**
     * execute query with expected plan result
     */
    public void search() {
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
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
