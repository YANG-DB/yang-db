package com.yangdb.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyFinalizer;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.services.TestsConfiguration;
import com.yangdb.test.BaseITMarker;
import io.restassured.http.Header;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static io.restassured.RestAssured.given;

public class CatalogIT implements BaseITMarker {
    @Before
    public void before() throws Exception {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
//        TestSuite.setup();
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalog() throws IOException {
        Ontology ontology = TestUtils.loadOntology("Dragons.json");
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/ontology/Dragons")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals(expected,result);
                        return result.equals(expected);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogs() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/ontology")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals(expected,result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }
    @Test
    /**
     * execute query with expected plan result
     */
    public void catalogSchemas() throws IOException {
        Ontology ontology = OntologyFinalizer.finalize(TestUtils.loadOntology("Dragons.json"));
        given()
                .contentType("application/json")
                .header(new Header("fuse-external-id", "test"))
                .with().port(8888)
                .get("/fuse/catalog/schema")
                .then()
                .assertThat()
                .body(new TestUtils.ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        String result = new ObjectMapper().writeValueAsString(contentResponse.getData());
                        Assert.assertEquals(expected,result);
                        return contentResponse.getData() != null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

}

