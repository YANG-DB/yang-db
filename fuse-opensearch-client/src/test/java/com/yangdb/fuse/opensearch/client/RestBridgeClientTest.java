package com.yangdb.fuse.opensearch.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.action.ActionListener;
import org.opensearch.action.ActionRequest;
import org.opensearch.action.ActionResponse;
import org.opensearch.action.ActionType;
import org.opensearch.action.admin.cluster.health.ClusterHealthAction;
import org.opensearch.action.admin.cluster.state.ClusterStateAction;
import org.opensearch.action.admin.cluster.state.ClusterStateResponse;
import org.opensearch.action.admin.indices.create.CreateIndexAction;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.create.CreateIndexResponse;
import org.opensearch.action.admin.indices.delete.DeleteIndexAction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsAction;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.opensearch.action.admin.indices.mapping.put.PutMappingAction;
import org.opensearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.opensearch.action.admin.indices.refresh.RefreshAction;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.admin.indices.refresh.RefreshResponse;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesAction;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.bulk.BulkAction;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.delete.DeleteAction;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.explain.ExplainAction;
import org.opensearch.action.explain.ExplainRequest;
import org.opensearch.action.explain.ExplainResponse;
import org.opensearch.action.fieldcaps.FieldCapabilitiesAction;
import org.opensearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.opensearch.action.fieldcaps.FieldCapabilitiesResponse;
import org.opensearch.action.get.GetAction;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.get.MultiGetAction;
import org.opensearch.action.get.MultiGetRequest;
import org.opensearch.action.get.MultiGetResponse;
import org.opensearch.action.index.IndexAction;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.ClearScrollAction;
import org.opensearch.action.search.ClearScrollRequest;
import org.opensearch.action.search.ClearScrollResponse;
import org.opensearch.action.search.MultiSearchAction;
import org.opensearch.action.search.MultiSearchRequest;
import org.opensearch.action.search.MultiSearchResponse;
import org.opensearch.action.search.SearchAction;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.search.SearchScrollAction;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.action.termvectors.MultiTermVectorsAction;
import org.opensearch.action.termvectors.TermVectorsAction;
import org.opensearch.action.update.UpdateAction;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.action.update.UpdateResponse;
import org.opensearch.client.Client;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.index.engine.VersionConflictEngineException;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.rest.RestStatus;
import org.opensearch.threadpool.ThreadPool;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * One test per covered action: the {@link Client} call must reach the matching {@link RestHighLevelClient} method with
 * the same (or an equivalently converted) request, and the RHLC's response must come back to the caller unchanged.
 * The RHLC and its {@link IndicesClient} are mocks (inline mock maker: both are final); every async call they get is
 * recorded and answered with {@link #nextResponse} or {@link #nextFailure}.
 */
public class RestBridgeClientTest {
    private ThreadPool threadPool;
    private Client client;

    private String lastMethod;
    private Object lastRequest;
    private Object nextResponse;
    private Exception nextFailure;

    @Before
    public void setUp() {
        IndicesClient indices = mock(IndicesClient.class, invocation -> record(invocation.getMethod().getName(), invocation.getArguments()));
        RestHighLevelClient rest = mock(RestHighLevelClient.class, invocation -> {
            if (invocation.getMethod().getName().equals("indices")) {
                return indices;
            }
            return record(invocation.getMethod().getName(), invocation.getArguments());
        });
        threadPool = new ThreadPool(Settings.builder().put("node.name", "bridge-unit").build());
        client = new RestBridgeClient(rest, Settings.EMPTY, threadPool, false, RequestOptions.DEFAULT);
    }

    @After
    public void tearDown() {
        ThreadPool.terminate(threadPool, 10, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private Object record(String method, Object[] args) {
        if (args.length == 3 && args[2] instanceof ActionListener) {
            lastMethod = method;
            lastRequest = args[0];
            ActionListener<Object> listener = (ActionListener<Object>) args[2];
            if (nextFailure != null) {
                listener.onFailure(nextFailure);
            } else {
                listener.onResponse(nextResponse);
            }
        }
        return null;
    }

    /** Runs {@code request} through the bridge with {@code response} as the RHLC's answer; checks the RHLC method. */
    private <Resp extends ActionResponse> Resp roundTrip(ActionType<Resp> action, ActionRequest request, Object response,
                                                         String expectedRhlcMethod) {
        nextResponse = response;
        Resp result = client.execute(action, request).actionGet();
        assertEquals(expectedRhlcMethod, lastMethod);
        return result;
    }

    private void assertForwarded(ActionType<? extends ActionResponse> action, ActionRequest request,
                                 Class<? extends ActionResponse> responseType, String expectedRhlcMethod) {
        ActionResponse response = mock(responseType);
        ActionResponse result = roundTrip(action, request, response, expectedRhlcMethod);
        assertSame("the request reaches the RHLC unchanged", request, lastRequest);
        assertSame("the RHLC's response reaches the caller unchanged", response, result);
    }

    //region reads
    @Test
    public void search() {
        SearchRequest request = client.prepareSearch("idx").setQuery(QueryBuilders.matchAllQuery()).request();
        assertForwarded(SearchAction.INSTANCE, request, SearchResponse.class, "searchAsync");
    }

    @Test
    public void searchViaBuilderGet() {
        SearchResponse response = mock(SearchResponse.class);
        nextResponse = response;
        assertSame(response, client.prepareSearch("idx").setSize(5).get());
        assertEquals("searchAsync", lastMethod);
        assertEquals(5, ((SearchRequest) lastRequest).source().size());
    }

    @Test
    public void scroll() {
        SearchScrollRequest request = client.prepareSearchScroll("scroll-id").setScroll("1m").request();
        assertForwarded(SearchScrollAction.INSTANCE, request, SearchResponse.class, "scrollAsync");
    }

    @Test
    public void clearScroll() {
        ClearScrollRequest request = client.prepareClearScroll().addScrollId("scroll-id").request();
        assertForwarded(ClearScrollAction.INSTANCE, request, ClearScrollResponse.class, "clearScrollAsync");
    }

    @Test
    public void multiSearch() {
        MultiSearchRequest request = client.prepareMultiSearch().add(client.prepareSearch("idx")).request();
        assertForwarded(MultiSearchAction.INSTANCE, request, MultiSearchResponse.class, "msearchAsync");
    }

    @Test
    public void get() {
        GetRequest request = client.prepareGet("idx", "pge", "1").request();
        assertForwarded(GetAction.INSTANCE, request, GetResponse.class, "getAsync");
    }

    @Test
    public void multiGet() {
        MultiGetRequest request = client.prepareMultiGet().add("idx", "1").request();
        assertForwarded(MultiGetAction.INSTANCE, request, MultiGetResponse.class, "mgetAsync");
    }

    @Test
    public void explain() {
        ExplainRequest request = client.prepareExplain("idx", "pge", "1").setQuery(QueryBuilders.matchAllQuery()).request();
        assertForwarded(ExplainAction.INSTANCE, request, ExplainResponse.class, "explainAsync");
    }

    @Test
    public void fieldCaps() {
        FieldCapabilitiesRequest request = client.prepareFieldCaps("idx").setFields("name").request();
        assertForwarded(FieldCapabilitiesAction.INSTANCE, request, FieldCapabilitiesResponse.class, "fieldCapsAsync");
    }
    //endregion

    //region writes
    @Test
    public void index() {
        IndexRequest request = client.prepareIndex("idx", "pge", "1").setSource("{\"a\":1}", XContentType.JSON).request();
        assertForwarded(IndexAction.INSTANCE, request, IndexResponse.class, "indexAsync");
    }

    @Test
    public void bulk() {
        BulkRequest request = client.prepareBulk()
                .add(client.prepareIndex("idx", "pge", "1").setSource("{\"a\":1}", XContentType.JSON))
                .add(client.prepareDelete("idx", "pge", "2"))
                .request();
        assertForwarded(BulkAction.INSTANCE, request, BulkResponse.class, "bulkAsync");
    }

    @Test
    public void delete() {
        DeleteRequest request = client.prepareDelete("idx", "pge", "1").request();
        assertForwarded(DeleteAction.INSTANCE, request, DeleteResponse.class, "deleteAsync");
    }

    @Test
    public void update() {
        UpdateRequest request = client.prepareUpdate("idx", "pge", "1").setDoc("{\"a\":2}", XContentType.JSON).request();
        assertForwarded(UpdateAction.INSTANCE, request, UpdateResponse.class, "updateAsync");
    }
    //endregion

    //region admin
    @Test
    public void createIndex() {
        CreateIndexRequest request = client.admin().indices().prepareCreate("idx")
                .addMapping("pge", "{\"pge\":{\"properties\":{\"name\":{\"type\":\"keyword\"}}}}", XContentType.JSON)
                .request();
        assertForwarded(CreateIndexAction.INSTANCE, request, CreateIndexResponse.class, "createAsync");
    }

    @Test
    public void deleteIndex() {
        DeleteIndexRequest request = client.admin().indices().prepareDelete("idx").request();
        assertForwarded(DeleteIndexAction.INSTANCE, request, AcknowledgedResponse.class, "deleteAsync");
    }

    @Test
    public void refresh() {
        RefreshRequest request = client.admin().indices().prepareRefresh("idx").request();
        assertForwarded(RefreshAction.INSTANCE, request, RefreshResponse.class, "refreshAsync");
    }

    @Test
    public void putMapping() {
        PutMappingRequest request = client.admin().indices().preparePutMapping("idx").setType("pge")
                .setSource("{\"properties\":{\"name\":{\"type\":\"keyword\"}}}", XContentType.JSON).request();
        assertForwarded(PutMappingAction.INSTANCE, request, AcknowledgedResponse.class, "putMappingAsync");
    }

    @Test
    public void getMappings() {
        GetMappingsRequest request = client.admin().indices().prepareGetMappings("idx").request();
        assertForwarded(GetMappingsAction.INSTANCE, request, GetMappingsResponse.class, "getMappingAsync");
    }

    @Test
    public void putTemplate() {
        PutIndexTemplateRequest request = client.admin().indices().preparePutTemplate("tpl")
                .setPatterns(Arrays.asList("idx_*")).request();
        assertForwarded(PutIndexTemplateAction.INSTANCE, request, AcknowledgedResponse.class, "putTemplateAsync");
    }

    @Test
    public void deleteTemplate() {
        DeleteIndexTemplateRequest request = client.admin().indices().prepareDeleteTemplate("tpl").request();
        assertForwarded(DeleteIndexTemplateAction.INSTANCE, request, AcknowledgedResponse.class, "deleteTemplateAsync");
    }

    @Test
    public void getTemplatesConvertsTheRequest() {
        GetIndexTemplatesResponse response = new GetIndexTemplatesResponse(java.util.Collections.emptyList());
        GetIndexTemplatesResponse result = roundTrip(GetIndexTemplatesAction.INSTANCE,
                new GetIndexTemplatesRequest("tpl1", "tpl2"), response, "getTemplateAsync");
        assertSame(response, result);
        assertEquals(Arrays.asList("tpl1", "tpl2"),
                ((org.opensearch.client.indices.GetIndexTemplatesRequest) lastRequest).names());
    }

    @Test
    public void getTemplatesAnswersAnUnknownNameWithAnEmptyList() {
        // the transport action returns an empty list for an unknown name; REST returns 404
        nextFailure = new OpenSearchStatusException("no template", RestStatus.NOT_FOUND);
        GetIndexTemplatesResponse result = client.admin().indices().prepareGetTemplates("missing").get();
        assertTrue(result.getIndexTemplates().isEmpty());
    }

    @Test
    public void indicesExistsConvertsTheRequestAndTheBooleanAnswer() {
        IndicesExistsResponse result = roundTrip(IndicesExistsAction.INSTANCE, new IndicesExistsRequest("a", "b"),
                Boolean.TRUE, "existsAsync");
        assertTrue(result.isExists());
        assertTrue(lastRequest instanceof org.opensearch.client.indices.GetIndexRequest);
        assertEquals(Arrays.asList("a", "b"), Arrays.asList(((org.opensearch.client.indices.GetIndexRequest) lastRequest).indices()));

        assertFalse(roundTrip(IndicesExistsAction.INSTANCE, new IndicesExistsRequest("a"), Boolean.FALSE, "existsAsync").isExists());
    }

    @Test
    public void clusterStateIsParsedFromTheRestMetadata() throws Exception {
        String json = "{\"cluster_name\":\"Dragons\",\"cluster_uuid\":\"u1\",\"metadata\":{\"cluster_uuid\":\"u1\","
                + "\"templates\":{},\"indices\":{\"person\":{\"version\":4,\"mapping_version\":1,\"settings_version\":1,"
                + "\"aliases_version\":1,\"routing_num_shards\":1,\"state\":\"open\","
                + "\"settings\":{\"index\":{\"number_of_shards\":\"1\",\"number_of_replicas\":\"0\",\"uuid\":\"x1\","
                + "\"version\":{\"created\":\"136227827\"},\"provided_name\":\"person\",\"creation_date\":\"1\"}},"
                + "\"mappings\":{\"pge\":{\"properties\":{\"name\":{\"type\":\"keyword\"}}}},"
                + "\"aliases\":[],\"primary_terms\":{\"0\":1},\"in_sync_allocations\":{\"0\":[]},"
                + "\"rollover_info\":{},\"system\":false}}}}";
        ClusterStateResponse response = RestBridgeClient.parseClusterState(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Dragons", response.getClusterName().value());
        assertNotNull(response.getState().getMetadata().index("person"));
        assertEquals("keyword", ((java.util.Map<?, ?>) ((java.util.Map<?, ?>) response.getState().getMetadata()
                .index("person").mapping().sourceAsMap().get("properties")).get("name")).get("type"));
        assertTrue(RestBridgeClient.supportedActions().contains(ClusterStateAction.INSTANCE));
    }
    //endregion

    //region unsupported actions and errors
    @Test
    public void unsupportedActionsFailLoudlyWithTheActionName() {
        for (ActionType<?> action : Arrays.asList(ClusterHealthAction.INSTANCE, TermVectorsAction.INSTANCE, MultiTermVectorsAction.INSTANCE)) {
            assertFalse(RestBridgeClient.supportedActions().contains(action));
        }
        try {
            client.admin().cluster().prepareHealth().get();
            fail("cluster health is not bridged");
        } catch (UnsupportedOperationException e) {
            assertEquals(ClusterHealthAction.NAME, e.getMessage());
        }
        try {
            client.prepareTermVectors("idx", "pge", "1").get();
            fail("term vectors are not bridged");
        } catch (UnsupportedOperationException e) {
            assertEquals(TermVectorsAction.NAME, e.getMessage());
        }
        assertEquals("no RHLC call for an unsupported action", null, lastMethod);
    }

    @Test
    public void supportedActionsAreTheDocumentedSet() {
        assertEquals(new HashSet<>(Arrays.asList(
                SearchAction.INSTANCE, SearchScrollAction.INSTANCE, ClearScrollAction.INSTANCE, MultiSearchAction.INSTANCE,
                GetAction.INSTANCE, MultiGetAction.INSTANCE, ExplainAction.INSTANCE, FieldCapabilitiesAction.INSTANCE,
                IndexAction.INSTANCE, BulkAction.INSTANCE, DeleteAction.INSTANCE, UpdateAction.INSTANCE,
                CreateIndexAction.INSTANCE, DeleteIndexAction.INSTANCE, IndicesExistsAction.INSTANCE, RefreshAction.INSTANCE,
                PutMappingAction.INSTANCE, GetMappingsAction.INSTANCE,
                PutIndexTemplateAction.INSTANCE, GetIndexTemplatesAction.INSTANCE, DeleteIndexTemplateAction.INSTANCE,
                ClusterStateAction.INSTANCE)), RestBridgeClient.supportedActions());
    }

    @Test
    public void indexNotFoundIsTranslated() {
        nextFailure = new OpenSearchStatusException(
                "OpenSearch exception [type=index_not_found_exception, reason=no such index [missing]]", RestStatus.NOT_FOUND);
        try {
            client.prepareSearch("missing").get();
            fail();
        } catch (IndexNotFoundException e) {
            assertEquals("missing", e.getIndex().getName());
        }
    }

    @Test
    public void resourceAlreadyExistsIsTranslated() {
        nextFailure = new OpenSearchStatusException(
                "OpenSearch exception [type=resource_already_exists_exception, reason=index [idx/abc] already exists]",
                RestStatus.BAD_REQUEST);
        try {
            client.admin().indices().prepareCreate("idx").get();
            fail();
        } catch (ResourceAlreadyExistsException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("already exists"));
        }
    }

    @Test
    public void versionConflictIsTranslated() {
        nextFailure = new OpenSearchStatusException(
                "OpenSearch exception [type=version_conflict_engine_exception, reason=[1]: version conflict, document already exists]",
                RestStatus.CONFLICT);
        try {
            client.prepareIndex("idx", "pge", "1").setCreate(true).setSource("{}", XContentType.JSON).get();
            fail();
        } catch (VersionConflictEngineException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("version conflict"));
        }
    }

    @Test
    public void otherServerErrorsPassThrough() {
        OpenSearchStatusException error = new OpenSearchStatusException(
                "OpenSearch exception [type=search_phase_execution_exception, reason=all shards failed]", RestStatus.BAD_REQUEST);
        nextFailure = error;
        try {
            client.prepareSearch("idx").get();
            fail();
        } catch (OpenSearchStatusException e) {
            assertSame(error, e);
        }
    }
    //endregion
}
