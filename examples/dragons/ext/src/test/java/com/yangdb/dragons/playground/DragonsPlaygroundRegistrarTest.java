package com.yangdb.dragons.playground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

/** Guards what the playground needs at runtime, since none of it is checked by the compiler. */
public class DragonsPlaygroundRegistrarTest {

    @Test
    public void swaggerUiWebjarMatchesTheVersionThePageServes() {
        // the version in SWAGGER_UI must match the swagger-ui webjar dependency in this module's pom
        Assert.assertNotNull("swagger-ui-bundle.js not on the classpath at " + DragonsPlaygroundRegistrar.SWAGGER_UI,
                getClass().getResource(DragonsPlaygroundRegistrar.SWAGGER_UI + "swagger-ui-bundle.js"));
        Assert.assertNotNull(getClass().getResource(DragonsPlaygroundRegistrar.SWAGGER_UI + "swagger-ui.css"));
        Assert.assertTrue(DragonsPlaygroundRegistrar.page().contains("/dragons/swagger/openapi.json"));
    }

    @Test
    public void everyTutorialFileAndTheOntologyAreBundled() throws Exception {
        for (String[] file : DragonsPlaygroundRegistrar.FILES) {
            String csv = DragonsPlaygroundRegistrar.resource(DragonsPlaygroundRegistrar.TUTORIAL + "csv/" + file[0]);
            Assert.assertTrue(file[0] + " has no data rows", csv.split("\r?\n").length > 1);
        }
        Ontology ontology = new ObjectMapper().readValue(
                DragonsPlaygroundRegistrar.resource(DragonsPlaygroundRegistrar.TUTORIAL + "Dragons.json"), Ontology.class);
        Assert.assertEquals(DragonsPlaygroundRegistrar.ONTOLOGY, ontology.getOnt());
    }

    @Test
    public void specOffersEveryTutorialQueryAsANamedExample() throws Exception {
        JsonNode spec = new ObjectMapper().readTree(DragonsPlaygroundRegistrar.resource(DragonsPlaygroundRegistrar.SPEC));
        Assert.assertTrue(spec.path("paths").has("/dragons/tutorial/load"));
        JsonNode cypher = null;
        for (JsonNode p : spec.path("paths").path("/query/cypher/run").path("get").path("parameters")) {
            if ("cypher".equals(p.path("name").asText())) cypher = p;
        }
        Assert.assertNotNull(cypher);
        int count = 0;
        for (Iterator<Map.Entry<String, JsonNode>> it = cypher.path("examples").fields(); it.hasNext(); count++) {
            Map.Entry<String, JsonNode> e = it.next();
            Assert.assertFalse(e.getKey() + " has no query", e.getValue().path("value").asText().isEmpty());
            Assert.assertTrue(e.getKey() + " has no x-doc (the e2e suite needs it)", e.getValue().has("x-doc"));
            Assert.assertTrue(e.getKey() + " summary should start with its id", e.getValue().path("summary").asText().startsWith(e.getKey()));
        }
        Assert.assertTrue("expected every tutorial query, got " + count, count >= 17);

        // the GraphQL examples (the GraphQL extension's run route), checked by the e2e suite the same way
        JsonNode graphQL = null;
        for (JsonNode p : spec.path("paths").path("/query/graphQL/run").path("get").path("parameters")) {
            if ("graphQL".equals(p.path("name").asText())) graphQL = p;
        }
        Assert.assertNotNull(graphQL);
        int graphQLCount = 0;
        for (Iterator<Map.Entry<String, JsonNode>> it = graphQL.path("examples").fields(); it.hasNext(); graphQLCount++) {
            Map.Entry<String, JsonNode> e = it.next();
            Assert.assertTrue(e.getKey() + " is not a GraphQL query", e.getValue().path("value").asText().startsWith("{"));
            Assert.assertTrue(e.getKey() + " has no x-doc (the e2e suite needs it)", e.getValue().has("x-doc"));
            Assert.assertTrue(e.getKey() + " summary should start with its id", e.getValue().path("summary").asText().startsWith(e.getKey()));
        }
        Assert.assertTrue("expected the GraphQL examples, got " + graphQLCount, graphQLCount >= 3);
    }

    @Test
    public void aThrowingSetupStepIsReportedNotPropagated() {
        // idgen init throws "index already exists" on every run after the first; the load must carry on
        Map<String, Object> expected = DragonsPlaygroundRegistrar.step("idgen/init", () -> {
            throw new IllegalStateException("index [.idgenerator] already exists");
        }, true);
        Assert.assertFalse(expected.containsKey("error"));
        Assert.assertTrue(String.valueOf(expected.get("note")).contains("already exists"));

        Map<String, Object> real = DragonsPlaygroundRegistrar.step("create indices", () -> {
            throw new IllegalStateException("boom");
        }, false);
        Assert.assertTrue(String.valueOf(real.get("error")).contains("boom"));
    }
}
