package com.kayhut.fuse.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.TestUtils.ContentMatcher;
import org.jooby.test.JoobyRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static com.kayhut.fuse.services.TestUtils.loadOntology;
import static io.restassured.RestAssured.given;

public class CatalogTest {

    @ClassRule
    public static JoobyRule app = new JoobyRule(new FuseApp(new DefaultAppUrlSupplier("/fuse")));

    @Test
    /**
     * execute query with expected plan result
     */
    public void catalog() throws IOException {
        Ontology ontology = loadOntology("dragons.json");
        given()
                .contentType("application/json")
                .get("/fuse/catalog/ontology/dragons")
                .then()
                .assertThat()
                .body(new ContentMatcher(o -> {
                    try {
                        String expected = new ObjectMapper().writeValueAsString(ontology);
                        ContentResponse contentResponse = new ObjectMapper().readValue(o.toString(), ContentResponse.class);
                        return new ObjectMapper().writeValueAsString(contentResponse.getData()).equals(expected);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

}

