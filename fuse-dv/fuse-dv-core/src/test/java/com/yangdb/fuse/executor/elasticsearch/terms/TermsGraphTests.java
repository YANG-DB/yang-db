/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package com.yangdb.fuse.executor.elasticsearch.terms;

import com.yangdb.fuse.executor.elasticsearch.terms.model.Step;
import com.yangdb.fuse.executor.elasticsearch.terms.model.Vertex;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreRequest;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.GraphExploreResponse;
import com.yangdb.fuse.executor.elasticsearch.terms.transport.VertexRequest;
import org.apache.lucene.search.BooleanQuery;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeResponse;
import org.elasticsearch.action.admin.indices.segments.IndexShardSegments;
import org.elasticsearch.action.admin.indices.segments.ShardSegments;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.MockScriptPlugin;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.test.hamcrest.ElasticsearchAssertions.*;
import static org.hamcrest.Matchers.greaterThan;


public class TermsGraphTests extends ESSingleNodeTestCase {

    static class DocTemplate {
        int numDocs;
        String[] people;
        String description;
        String decade;

        DocTemplate(int numDocs, String decade, String description, String... people) {
            super();
            this.decade = decade;
            this.numDocs = numDocs;
            this.description = description;
            this.people = people;
        }
    }


    static final DocTemplate[] socialNetTemplate = {
            new DocTemplate(10, "60s", "beatles", "john", "paul", "george", "ringo"),
            new DocTemplate(2, "60s", "collaboration", "ravi", "george"),
            new DocTemplate(3, "80s", "travelling wilburys", "roy", "george", "jeff"),
            new DocTemplate(5, "80s", "travelling wilburys", "roy", "jeff", "bob"),
            new DocTemplate(1, "70s", "collaboration", "roy", "elvis"),
            new DocTemplate(10, "90s", "nirvana", "dave", "kurt"),
            new DocTemplate(2, "00s", "collaboration", "dave", "paul"),
            new DocTemplate(2, "80s", "collaboration", "stevie", "paul"),
            new DocTemplate(2, "70s", "collaboration", "john", "yoko"),
            new DocTemplate(100, "70s", "fillerDoc", "other", "irrelevant", "duplicated", "spammy", "background")
    };

    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertAcked(client().admin().indices().prepareCreate("test")
                .setSettings(Settings.builder().put("index.number_of_shards", 2).put("index.number_of_replicas", 0))
                .addMapping("_doc",
                        jsonBuilder()
                                .startObject()
                                    .startObject("properties")
                                        .startObject("decade")
                                            .field("type", "keyword")
                                        .endObject()
                                        .startObject("people")
                                            .field("type", "keyword")
                                        .endObject()
                                        .startObject("description")
                                            .field("type", "text")
                                            .field("fielddata", "true")
                                        .endObject()
                                    .endObject()
                                .endObject()));
                        createIndex("idx_unmapped");

        ensureGreen();

        int numDocs = 0;
        for (DocTemplate dt : socialNetTemplate) {
            for (int i = 0; i < dt.numDocs; i++) {
                // Supply a doc ID for deterministic routing of docs to shards
                client().prepareIndex("test", "_doc").setId("doc#" + numDocs)
                        .setSource("decade", dt.decade, "people", dt.people, "description", dt.description)
                        .get();
                numDocs++;
            }
        }
        client().admin().indices().prepareRefresh("test").get();
        // Ensure single segment with no deletes. Hopefully solves test instability in
        // issue https://github.com/elastic/x-pack-elasticsearch/issues/918
        ForceMergeResponse actionGet = client().admin().indices().prepareForceMerge("test").setFlush(true).setMaxNumSegments(1)
                .execute().actionGet();
        client().admin().indices().prepareRefresh("test").get();
        assertAllSuccessful(actionGet);
        for (IndexShardSegments seg : client().admin().indices().prepareSegments().get().getIndices().get("test")) {
            ShardSegments[] shards = seg.getShards();
            for (ShardSegments shardSegments : shards) {
                assertEquals(1, shardSegments.getSegments().size());
            }
        }

        assertHitCount(client().prepareSearch().setQuery(matchAllQuery()).get(), numDocs);
    }


    public void testSignificanceQueryCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));
        hop1.addVertexRequest("people").size(10).minDocCount(1); // members of beatles
        grb.createNextStep(null).addVertexRequest("people").size(100).minDocCount(1); // friends of members of beatlesS

        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "john", "paul", "george", "ringo");
        checkVertexDepth(response, 1, "stevie", "yoko", "roy");
        checkVertexIsMoreImportant(response, "John's only collaboration is more relevant than one of Paul's many", "yoko", "stevie");
        checkVertexIsMoreImportant(response, "John's only collaboration is more relevant than George's with profligate Roy", "yoko", "roy");
        assertNull("Elvis is a 3rd tier connection so should not be returned here", response.getVertex(Vertex.createId("people", "elvis")));
    }


    @Override
    protected Settings nodeSettings() {
        // Disable security otherwise authentication failures happen creating indices.
        Builder newSettings = Settings.builder();
        newSettings.put(super.nodeSettings());
//        newSettings.put(XPackSettings.SECURITY_ENABLED.getKey(), false);
//        newSettings.put(XPackSettings.MONITORING_ENABLED.getKey(), false);
//        newSettings.put(XPackSettings.WATCHER_ENABLED.getKey(), false);
        return newSettings.build();
    }

    public void testTargetedQueryCrawl() {
        // Tests use of a client-provided query to steer exploration
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));
        hop1.addVertexRequest("people").size(10).minDocCount(1); // members of beatles
        //70s friends of beatles
        grb.createNextStep(QueryBuilders.termQuery("decade", "70s")).addVertexRequest("people").size(100).minDocCount(1);

        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "john", "paul", "george", "ringo");
        checkVertexDepth(response, 1, "yoko");
        assertNull("Roy collaborated with George in the 80s not the 70s", response.getVertex(Vertex.createId("people", "roy")));
        assertNull("Stevie collaborated with Paul in the 80s not the 70s", response.getVertex(Vertex.createId("people", "stevie")));

    }


    public void testLargeNumberTermsStartCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(null);
        VertexRequest peopleNames = hop1.addVertexRequest("people")
                .minDocCount(1);// members of beatles

        peopleNames.addInclude("john", 1);

        for (int i = 0; i < BooleanQuery.getMaxClauseCount() + 1; i++) {
            peopleNames.addInclude("unknown" + i, 1);
        }

        grb.createNextStep(null).addVertexRequest("people").size(100).minDocCount(1); // friends of members of beatles
        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "john");
        checkVertexDepth(response, 1, "yoko");
    }

    public void testTargetedQueryCrawlDepth2() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));

        hop1.addVertexRequest("people").size(10).minDocCount(1); // members of beatles
        //00s friends of beatles
        grb.createNextStep(QueryBuilders.termQuery("decade", "00s")).addVertexRequest("people").size(100).minDocCount(1);
        //90s friends of friends of beatles
        grb.createNextStep(QueryBuilders.termQuery("decade", "90s")).addVertexRequest("people").size(100).minDocCount(1);

        GraphExploreResponse response = driver.execute(grb);


        checkVertexDepth(response, 0, "john", "paul", "george", "ringo");
        checkVertexDepth(response, 1, "dave");
        checkVertexDepth(response, 2, "kurt");

    }

    public void testPopularityQueryCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));
        // Turning off the significance feature means we reward popularity
        grb.useSignificance(false);
        hop1.addVertexRequest("people").size(10).minDocCount(1); // members of beatles
        grb.createNextStep(null).addVertexRequest("people").size(100).minDocCount(1); // friends of members of beatles

        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "john", "paul", "george", "ringo");
        checkVertexDepth(response, 1, "stevie", "yoko", "roy");
        checkVertexIsMoreImportant(response, "Yoko has more collaborations than Stevie", "yoko", "stevie");
        checkVertexIsMoreImportant(response, "Roy has more collaborations than Stevie", "roy", "stevie");
        assertNull("Elvis is a 3rd tier connection so should not be returned here", response.getVertex(Vertex.createId("people", "elvis")));
    }


    public void testNonDiversifiedCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        Step hop1 = grb.createNextStep(QueryBuilders.termsQuery("people", "dave", "other"));

        hop1.addVertexRequest("people").size(10).minDocCount(1);

        GraphExploreResponse response = driver.execute(grb);


        checkVertexDepth(response, 0, "dave", "kurt", "other", "spammy");
        checkVertexIsMoreImportant(response, "Due to duplication and no diversification spammy content beats signal", "spammy", "kurt");
    }

    public void testDiversifiedCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        grb.sampleDiversityField("description");
        grb.maxDocsPerDiversityValue(1);

        Step hop1 = grb.createNextStep(QueryBuilders.termsQuery("people", "dave", "other"));
        hop1.addVertexRequest("people").size(10).minDocCount(1);

        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "dave", "kurt");
        assertNull("Duplicate spam should be removed from the results", response.getVertex(Vertex.createId("people", "spammy")));
    }

    public void testInvalidDiversifiedCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test");
        grb.sampleDiversityField("description");

        Step hop1 = grb.createNextStep(QueryBuilders.termsQuery("people", "roy", "other"));
        hop1.addVertexRequest("people").size(10).minDocCount(1);

        Throwable expectedError = null;
        try {
            GraphExploreResponse response = driver.execute(grb);
            if (response.getShardFailures().length > 0) {
                expectedError = response.getShardFailures()[0].getCause();
            }
        } catch (Exception rte) {
            expectedError = rte;
        }
        assertNotNull(expectedError);
        String message = expectedError.toString();
        assertTrue(message.contains("Sample diversifying key must be a single valued-field"));
    }

    public void testMappedAndUnmappedQueryCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("test", "idx_unmapped");
        grb.sampleDiversityField("description");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));
        hop1.addVertexRequest("people").size(10).minDocCount(1); // members of beatles
        grb.createNextStep(null).addVertexRequest("people").size(100).minDocCount(1); // friends of members of beatles

        GraphExploreResponse response = driver.execute(grb);

        checkVertexDepth(response, 0, "john", "paul", "george", "ringo");
        checkVertexDepth(response, 1, "stevie", "yoko", "roy");
        checkVertexIsMoreImportant(response, "John's only collaboration is more relevant than one of Paul's many", "yoko", "stevie");
        checkVertexIsMoreImportant(response, "John's only collaboration is more relevant than George's with profligate Roy", "yoko", "roy");
        assertNull("Elvis is a 3rd tier connection so should not be returned here", response.getVertex(Vertex.createId("people", "elvis")));
    }

    public void testUnmappedQueryCrawl() {
        TermGraphExplorationDriver driver = new TermGraphExplorationDriver(client());
        GraphExploreRequest grb = new GraphExploreRequest("idx_unmapped");
        grb.sampleDiversityField("description");
        Step hop1 = grb.createNextStep(QueryBuilders.termQuery("description", "beatles"));
        hop1.addVertexRequest("people").size(10).minDocCount(1);

        GraphExploreResponse response = driver.execute(grb);
        assertEquals(0, response.getEdgesMap().size());
        assertEquals(0, response.getVertices().size());

    }


    private static void checkVertexDepth(GraphExploreResponse response, int expectedDepth, String... ids) {
        for (String id : ids) {
            Vertex vertex = response.getVertex(Vertex.createId("people", id));
            assertNotNull("Expected to find " + id, vertex);
            assertEquals(id + " found at wrong hop depth", expectedDepth, vertex.getStepsDepth());
        }
    }

    private static void checkVertexIsMoreImportant(GraphExploreResponse response, String why, String strongerId, String weakerId) {
        // *Very* rarely I think the doc delete randomization and background merges conspire to
        // make this test fail. Scores vary slightly due to deletes I suspect.
        Vertex strongVertex = response.getVertex(Vertex.createId("people", strongerId));
        assertNotNull(strongVertex);
        Vertex weakVertex = response.getVertex(Vertex.createId("people", weakerId));
        assertNotNull(weakVertex);
        assertThat(why, strongVertex.getWeight(), greaterThan(weakVertex.getWeight()));
    }

    public static class ScriptedTimeoutPlugin extends MockScriptPlugin {
        @Override
        public Map<String, Function<Map<String, Object>, Object>> pluginScripts() {
            return Collections.singletonMap("graph_timeout", params -> {
                try {
                    Thread.sleep(750);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return true;
            });
        }
    }
}