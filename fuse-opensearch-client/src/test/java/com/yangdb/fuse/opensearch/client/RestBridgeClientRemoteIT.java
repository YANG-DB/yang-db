package com.yangdb.fuse.opensearch.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.action.DocWriteResponse;
import org.opensearch.action.admin.cluster.health.ClusterHealthAction;
import org.opensearch.action.admin.cluster.state.ClusterStateResponse;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.opensearch.action.bulk.BulkRequestBuilder;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.explain.ExplainResponse;
import org.opensearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.get.MultiGetResponse;
import org.opensearch.action.search.ClearScrollResponse;
import org.opensearch.action.search.MultiSearchResponse;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.cluster.metadata.IndexMetadata;
import org.opensearch.cluster.metadata.MappingMetadata;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.index.engine.VersionConflictEngineException;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.node.InternalSettingsPreparer;
import org.opensearch.node.Node;
import org.opensearch.plugins.Plugin;
import org.opensearch.search.SearchHit;
import org.opensearch.transport.Netty4Plugin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * End-to-end check of the bridge against a <b>remote</b> cluster: an OpenSearch node is started on the agent's test
 * ports (-Dfuse.test.http.port / -Dfuse.test.transport.port), and every call goes through
 * {@link ClientFactory#remote} over HTTP. The node's in-JVM {@code node.client()} is never used.
 * <p>
 * To run it against an external cluster instead (e.g. the Docker OpenSearch on a spare port), pass
 * {@code -Dfuse.bridge.it.remote=host:port}; then no node is started.
 */
public class RestBridgeClientRemoteIT {
    private static final int HTTP_PORT = Integer.getInteger("fuse.test.http.port", 9200);
    private static final int TRANSPORT_PORT = Integer.getInteger("fuse.test.transport.port", 9300);
    private static final String DATA_DIR = System.getProperty("fuse.test.data.dir", "target/es") + "-bridge-it";
    private static final String EXTERNAL = System.getProperty("fuse.bridge.it.remote");
    private static final String CLUSTER = "yangdb-bridge-it";

    private static final String INDEX = "bridge_it_person";
    private static final String TEMPLATE = "bridge_it_template";

    private static Node node;
    private static Client client;

    private static class PluginConfigurableNode extends Node {
        PluginConfigurableNode(Settings settings, Collection<Class<? extends Plugin>> plugins, Path path) {
            super(InternalSettingsPreparer.prepareEnvironment(settings, new HashMap<>(), path, () -> CLUSTER), plugins, false);
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        if (EXTERNAL == null) {
            deleteFolder(new File(DATA_DIR));
            Settings settings = Settings.builder()
                    .put("cluster.name", CLUSTER)
                    .put("node.name", CLUSTER)
                    .put("path.home", DATA_DIR)
                    .put("path.data", DATA_DIR)
                    .put("path.logs", DATA_DIR)
                    .put("http.port", HTTP_PORT)
                    .put("transport.port", TRANSPORT_PORT)
                    .put("transport.type", "netty4")
                    .put("http.type", "netty4")
                    .put("cluster.routing.allocation.disk.threshold_enabled", false)
                    .build();
            node = new PluginConfigurableNode(settings, Collections.singletonList(Netty4Plugin.class), Paths.get(DATA_DIR));
            node.start();
            client = ClientFactory.remote(new String[]{"localhost"}, HTTP_PORT, Settings.builder().put("cluster.name", CLUSTER).build());
        } else {
            client = ClientFactory.remote(new String[]{EXTERNAL}, 9200, Settings.EMPTY);
        }
        assertTrue("talks to the cluster through the bridge only", client instanceof RestBridgeClient);

        if (client.admin().indices().exists(new IndicesExistsRequest(INDEX)).actionGet().isExists()) {
            client.admin().indices().prepareDelete(INDEX).get();
        }
        client.admin().indices().prepareCreate(INDEX)
                .setSettings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0))
                .setMapping("{\"properties\":{"
                        + "\"name\":{\"type\":\"keyword\"},\"age\":{\"type\":\"integer\"}}}")
                .get();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (client != null) {
            try {
                client.admin().indices().prepareDelete(INDEX).get();
            } catch (Exception ignored) {
            }
            client.close();
        }
        if (node != null) {
            node.close();
            deleteFolder(new File(DATA_DIR));
        }
    }

    @Test
    public void indexGetUpdateDelete() {
        DocWriteResponse indexed = client.prepareIndex(INDEX).setId("doc-1")
                .setSource("{\"name\":\"Rhaegar\",\"age\":24}", XContentType.JSON).get();
        assertEquals(DocWriteResponse.Result.CREATED, indexed.getResult());

        GetResponse get = client.prepareGet(INDEX, "doc-1").get();
        assertTrue(get.isExists());
        assertEquals("Rhaegar", get.getSourceAsMap().get("name"));

        DocWriteResponse updated = client.prepareUpdate(INDEX, "doc-1").setDoc("{\"age\":25}", XContentType.JSON).get();
        assertEquals(DocWriteResponse.Result.UPDATED, updated.getResult());
        assertEquals(25, client.prepareGet(INDEX, "doc-1").get().getSourceAsMap().get("age"));

        DocWriteResponse deleted = client.prepareDelete(INDEX, "doc-1").get();
        assertEquals(DocWriteResponse.Result.DELETED, deleted.getResult());
        assertFalse(client.prepareGet(INDEX, "doc-1").get().isExists());
    }

    @Test
    public void bulkRefreshSearchScroll() {
        BulkRequestBuilder bulk = client.prepareBulk();
        for (int i = 0; i < 25; i++) {
            bulk.add(client.prepareIndex(INDEX).setId("bulk-" + i)
                    .setSource("{\"name\":\"dragon-" + i + "\",\"age\":" + i + "}", XContentType.JSON));
        }
        BulkResponse bulkResponse = bulk.get();
        assertFalse(bulkResponse.buildFailureMessage(), bulkResponse.hasFailures());
        assertEquals(25, bulkResponse.getItems().length);

        assertEquals(0, client.admin().indices().prepareRefresh(INDEX).get().getFailedShards());

        SearchResponse first = client.prepareSearch(INDEX)
                .setQuery(QueryBuilders.prefixQuery("name", "dragon-"))
                .setSize(10).setScroll(TimeValue.timeValueMinutes(1)).get();
        assertEquals(25, first.getHits().getTotalHits().value);

        Set<String> ids = new HashSet<>();
        SearchResponse page = first;
        while (page.getHits().getHits().length > 0) {
            for (SearchHit hit : page.getHits().getHits()) {
                ids.add(hit.getId());
            }
            page = client.prepareSearchScroll(page.getScrollId()).setScroll(TimeValue.timeValueMinutes(1)).get();
        }
        assertEquals(25, ids.size());

        ClearScrollResponse cleared = client.prepareClearScroll().addScrollId(page.getScrollId()).get();
        assertTrue(cleared.isSucceeded());
    }

    @Test
    public void multiGetMultiSearchExplainFieldCaps() {
        client.prepareIndex(INDEX).setId("m-1").setSource("{\"name\":\"Viserion\",\"age\":3}", XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).get();
        client.prepareIndex(INDEX).setId("m-2").setSource("{\"name\":\"Rhaegal\",\"age\":3}", XContentType.JSON)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE).get();

        MultiGetResponse mget = client.prepareMultiGet().add(INDEX, "m-1", "m-2", "m-missing").get();
        assertEquals(3, mget.getResponses().length);
        assertTrue(mget.getResponses()[0].getResponse().isExists());
        assertFalse(mget.getResponses()[2].getResponse().isExists());

        MultiSearchResponse msearch = client.prepareMultiSearch()
                .add(client.prepareSearch(INDEX).setQuery(QueryBuilders.termQuery("name", "Viserion")))
                .add(client.prepareSearch(INDEX).setQuery(QueryBuilders.termQuery("name", "Rhaegal")))
                .get();
        assertEquals(2, msearch.getResponses().length);
        assertEquals(1, msearch.getResponses()[0].getResponse().getHits().getTotalHits().value);

        ExplainResponse explain = client.prepareExplain(INDEX, "m-1").setQuery(QueryBuilders.termQuery("name", "Viserion")).get();
        assertTrue(explain.isMatch());

        FieldCapabilitiesResponse caps = client.prepareFieldCaps(INDEX).setFields("name", "age").get();
        assertEquals("keyword", caps.getField("name").get("keyword").getType());
    }

    @Test
    public void mappingsAreUntypedAndUpdatable() {
        assertTrue(client.admin().indices().preparePutMapping(INDEX)
                .setSource("{\"properties\":{\"title\":{\"type\":\"text\"}}}", XContentType.JSON).get().isAcknowledged());

        GetMappingsResponse mappings = client.admin().indices().prepareGetMappings(INDEX).get();
        MappingMetadata mapping = mappings.mappings().get(INDEX);
        assertNotNull("the index has its (single, untyped) mapping", mapping);
        Map<?, ?> properties = (Map<?, ?>) mapping.sourceAsMap().get("properties");
        assertTrue(properties.containsKey("name"));
        assertTrue(properties.containsKey("title"));
    }

    @Test
    public void templatesPutGetDelete() {
        assertTrue(client.admin().indices().preparePutTemplate(TEMPLATE)
                .setPatterns(Arrays.asList("bridge_it_tpl_*"))
                .setMapping("{\"properties\":{\"name\":{\"type\":\"keyword\"}}}", XContentType.JSON)
                .get().isAcknowledged());

        GetIndexTemplatesResponse templates = client.admin().indices().prepareGetTemplates(TEMPLATE).get();
        assertEquals(1, templates.getIndexTemplates().size());
        assertEquals(Arrays.asList("bridge_it_tpl_*"), templates.getIndexTemplates().get(0).patterns());
        // as from the transport action: the untyped mapping under OpenSearch's single internal mapping name
        Map<?, ?> templateMapping = new MappingMetadata(templates.getIndexTemplates().get(0).mappings()).sourceAsMap();
        assertEquals("keyword", ((Map<?, ?>) ((Map<?, ?>) templateMapping.get("properties")).get("name")).get("type"));

        assertTrue(client.admin().indices().prepareDeleteTemplate(TEMPLATE).get().isAcknowledged());
        assertTrue("unknown template -> empty list, as from the transport action",
                client.admin().indices().prepareGetTemplates(TEMPLATE).get().getIndexTemplates().isEmpty());
    }

    @Test
    public void indexAdminLifecycleAndClusterState() {
        String index = "bridge_it_lifecycle";
        assertFalse(client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists());
        assertTrue(client.admin().indices().prepareCreate(index)
                .setSettings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 0))
                .get().isAcknowledged());
        assertTrue(client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists());
        assertEquals("1", client.admin().indices().prepareGetSettings(index).get().getSetting(index, "index.number_of_shards"));

        ClusterStateResponse state = client.admin().cluster().prepareState().get();
        IndexMetadata personMetadata = state.getState().getMetadata().index(INDEX);
        assertNotNull(personMetadata);
        assertNotNull(state.getState().getMetadata().index(index));
        assertNotNull("mappings come through the cluster state", personMetadata.mapping());

        try {
            client.admin().indices().prepareCreate(index).get();
            fail("creating an existing index must fail");
        } catch (ResourceAlreadyExistsException expected) {
            // as with the TransportClient
        }

        assertTrue(client.admin().indices().prepareDelete(index).get().isAcknowledged());
        assertFalse(client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists());
    }

    @Test
    public void serverErrorsKeepTheirTransportTypes() {
        try {
            client.prepareSearch("bridge_it_no_such_index").get();
            fail();
        } catch (IndexNotFoundException e) {
            assertEquals("bridge_it_no_such_index", e.getIndex().getName());
        }

        client.prepareIndex(INDEX).setId("dup").setSource("{\"name\":\"a\"}", XContentType.JSON).get();
        try {
            client.prepareIndex(INDEX).setId("dup").setCreate(true).setSource("{\"name\":\"b\"}", XContentType.JSON).get();
            fail();
        } catch (VersionConflictEngineException expected) {
            // as with the TransportClient
        }
    }

    @Test
    public void unsupportedActionFailsLoudly() {
        try {
            client.admin().cluster().prepareHealth().get();
            fail();
        } catch (UnsupportedOperationException e) {
            assertEquals(ClusterHealthAction.NAME, e.getMessage());
        }
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
