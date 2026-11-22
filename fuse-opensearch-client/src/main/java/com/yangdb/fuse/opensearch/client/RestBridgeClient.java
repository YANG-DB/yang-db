package com.yangdb.fuse.opensearch.client;

/*-
 * #%L
 * fuse-opensearch-client
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.opensearch.OpenSearchException;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.core.action.ActionListener;
import org.opensearch.action.ActionRequest;
import org.opensearch.core.action.ActionResponse;
import org.opensearch.action.ActionType;
import org.opensearch.action.admin.cluster.state.ClusterStateAction;
import org.opensearch.action.admin.cluster.state.ClusterStateRequest;
import org.opensearch.action.admin.cluster.state.ClusterStateResponse;
import org.opensearch.action.admin.indices.create.CreateIndexAction;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.delete.DeleteIndexAction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.opensearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsAction;
import org.opensearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.opensearch.action.admin.indices.mapping.put.PutMappingAction;
import org.opensearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.opensearch.action.admin.indices.refresh.RefreshAction;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.action.admin.indices.settings.get.GetSettingsAction;
import org.opensearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.opensearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesAction;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.opensearch.action.admin.indices.template.get.GetIndexTemplatesResponse;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.opensearch.action.bulk.BulkAction;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.delete.DeleteAction;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.explain.ExplainAction;
import org.opensearch.action.explain.ExplainRequest;
import org.opensearch.action.fieldcaps.FieldCapabilitiesAction;
import org.opensearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.opensearch.action.get.GetAction;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.MultiGetAction;
import org.opensearch.action.get.MultiGetRequest;
import org.opensearch.action.index.IndexAction;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.ClearScrollAction;
import org.opensearch.action.search.ClearScrollRequest;
import org.opensearch.action.search.MultiSearchAction;
import org.opensearch.action.search.MultiSearchRequest;
import org.opensearch.action.search.SearchAction;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchScrollAction;
import org.opensearch.action.search.SearchScrollRequest;
import org.opensearch.action.update.UpdateAction;
import org.opensearch.action.update.UpdateRequest;
import org.opensearch.client.Request;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.Response;
import org.opensearch.client.ResponseListener;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.support.AbstractClient;
import org.opensearch.cluster.ClusterName;
import org.opensearch.cluster.ClusterState;
import org.opensearch.cluster.metadata.IndexMetadata;
import org.opensearch.cluster.metadata.Metadata;
import org.opensearch.core.common.bytes.BytesReference;
import org.opensearch.common.settings.Settings;
import org.opensearch.core.xcontent.DeprecationHandler;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.common.xcontent.XContentHelper;
import org.opensearch.core.xcontent.XContentParser;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.index.engine.VersionConflictEngineException;
import org.opensearch.core.index.shard.ShardId;
import org.opensearch.core.rest.RestStatus;
import org.opensearch.threadpool.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link org.opensearch.client.Client} for a <b>remote</b> cluster, implemented on top of the
 * {@link RestHighLevelClient}. It replaces the {@code TransportClient}, which OpenSearch 2.x no longer ships, so the
 * code that only knows the {@code Client} interface (request builders, {@code admin().indices()}, ...) keeps working.
 * <p>
 * Every {@code client.prepareX(...).get()} ends up in {@link #doExecute}; it dispatches on the action type to the
 * matching RHLC call. The covered actions are listed in {@link #supportedActions()} and in the module README. Any
 * other action fails loudly with {@code UnsupportedOperationException(action.name())}; nothing is silently ignored.
 * <p>
 * Server errors come back from the RHLC as {@link OpenSearchStatusException}s. The few exception types this codebase
 * catches ({@link IndexNotFoundException}, {@link ResourceAlreadyExistsException},
 * {@link VersionConflictEngineException}) are re-created from them, so existing {@code catch} blocks keep working.
 */
public class RestBridgeClient extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(RestBridgeClient.class);

    /** One dispatch entry: forwards a (server-side) request to the RHLC and completes the listener. */
    @FunctionalInterface
    interface Handler {
        void execute(RestBridgeClient client, ActionRequest request, ActionListener<Object> listener) throws Exception;
    }

    private static final Map<ActionType<?>, Handler> HANDLERS = handlers();

    private final RestHighLevelClient rest;
    private final RequestOptions options;
    private final boolean ownsThreadPool;

    //region Constructors
    /**
     * @param rest     the REST client; this bridge owns it and closes it on {@link #close()}
     * @param settings client settings (kept for {@link #settings()}; {@code cluster.name} is informational only)
     */
    public RestBridgeClient(RestHighLevelClient rest, Settings settings) {
        this(rest, settings, newThreadPool(), true, RequestOptions.DEFAULT);
    }

    RestBridgeClient(RestHighLevelClient rest, Settings settings, ThreadPool threadPool, boolean ownsThreadPool,
                     RequestOptions options) {
        super(settings, threadPool);
        this.rest = rest;
        this.options = options;
        this.ownsThreadPool = ownsThreadPool;
    }
    //endregion

    //region Public
    /** The action types this bridge forwards; everything else fails with {@link UnsupportedOperationException}. */
    public static Set<ActionType<?>> supportedActions() {
        return Collections.unmodifiableSet(HANDLERS.keySet());
    }

    public RestHighLevelClient restClient() {
        return rest;
    }

    @Override
    public void close() {
        try {
            rest.close();
        } catch (IOException e) {
            logger.warn("closing the REST client failed", e);
        }
        if (ownsThreadPool) {
            ThreadPool.terminate(threadPool(), 10, TimeUnit.SECONDS);
        }
    }
    //endregion

    //region AbstractClient
    @Override
    @SuppressWarnings("unchecked")
    protected <Req extends ActionRequest, Resp extends ActionResponse> void doExecute(
            ActionType<Resp> action, Req request, ActionListener<Resp> listener) {
        Handler handler = HANDLERS.get(action);
        if (handler == null) {
            listener.onFailure(new UnsupportedOperationException(action.name()));
            return;
        }
        ActionListener<Object> translating = translating((ActionListener<Object>) (ActionListener<?>) listener);
        try {
            handler.execute(this, request, translating);
        } catch (Exception e) {
            translating.onFailure(e);
        }
    }
    //endregion

    //region Dispatch table
    private static Map<ActionType<?>, Handler> handlers() {
        Map<ActionType<?>, Handler> h = new LinkedHashMap<>();
        // reads
        h.put(SearchAction.INSTANCE, (c, r, l) -> c.rest.searchAsync((SearchRequest) r, c.options, l(l)));
        h.put(SearchScrollAction.INSTANCE, (c, r, l) -> c.rest.scrollAsync((SearchScrollRequest) r, c.options, l(l)));
        h.put(ClearScrollAction.INSTANCE, (c, r, l) -> c.rest.clearScrollAsync((ClearScrollRequest) r, c.options, l(l)));
        h.put(MultiSearchAction.INSTANCE, (c, r, l) -> c.rest.msearchAsync((MultiSearchRequest) r, c.options, l(l)));
        h.put(GetAction.INSTANCE, (c, r, l) -> c.rest.getAsync((GetRequest) r, c.options, l(l)));
        h.put(MultiGetAction.INSTANCE, (c, r, l) -> c.rest.mgetAsync((MultiGetRequest) r, c.options, l(l)));
        h.put(ExplainAction.INSTANCE, (c, r, l) -> c.rest.explainAsync((ExplainRequest) r, c.options, l(l)));
        h.put(FieldCapabilitiesAction.INSTANCE, (c, r, l) -> c.rest.fieldCapsAsync((FieldCapabilitiesRequest) r, c.options, l(l)));
        // writes
        h.put(IndexAction.INSTANCE, (c, r, l) -> c.rest.indexAsync((IndexRequest) r, c.options, l(l)));
        h.put(BulkAction.INSTANCE, (c, r, l) -> c.rest.bulkAsync((BulkRequest) r, c.options, l(l)));
        h.put(DeleteAction.INSTANCE, (c, r, l) -> c.rest.deleteAsync((DeleteRequest) r, c.options, l(l)));
        h.put(UpdateAction.INSTANCE, (c, r, l) -> c.rest.updateAsync((UpdateRequest) r, c.options, l(l)));
        // admin: indices
        h.put(DeleteIndexAction.INSTANCE, (c, r, l) -> c.rest.indices().deleteAsync((DeleteIndexRequest) r, c.options, l(l)));
        h.put(RefreshAction.INSTANCE, (c, r, l) -> c.rest.indices().refreshAsync((RefreshRequest) r, c.options, l(l)));
        h.put(IndicesExistsAction.INSTANCE, RestBridgeClient::indicesExists);
        h.put(GetSettingsAction.INSTANCE, (c, r, l) -> c.rest.indices().getSettingsAsync((GetSettingsRequest) r, c.options, l(l)));
        h.put(DeleteIndexTemplateAction.INSTANCE, (c, r, l) -> c.rest.indices().deleteTemplateAsync((DeleteIndexTemplateRequest) r, c.options, l(l)));
        // admin: indices, typed (1.x) variants -- see "After the 2.x bump" in the README
        h.put(CreateIndexAction.INSTANCE, RestBridgeClient::createIndex);
        h.put(PutMappingAction.INSTANCE, RestBridgeClient::putMapping);
        h.put(GetMappingsAction.INSTANCE, RestBridgeClient::getMappings);
        h.put(PutIndexTemplateAction.INSTANCE, RestBridgeClient::putTemplate);
        h.put(GetIndexTemplatesAction.INSTANCE, RestBridgeClient::getTemplates);
        // admin: cluster
        h.put(ClusterStateAction.INSTANCE, RestBridgeClient::clusterState);
        return h;
    }

    /** Narrows the untyped dispatch listener to the RHLC's typed listener parameter. */
    @SuppressWarnings("unchecked")
    private static <T> ActionListener<T> l(ActionListener<Object> listener) {
        return (ActionListener<T>) (ActionListener<?>) listener;
    }

    private static void indicesExists(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        IndicesExistsRequest request = (IndicesExistsRequest) r;
        org.opensearch.client.indices.GetIndexRequest get = new org.opensearch.client.indices.GetIndexRequest(request.indices())
                .indicesOptions(request.indicesOptions());
        get.local(request.local());
        c.rest.indices().existsAsync(get, c.options, ActionListener.wrap(
                exists -> l.onResponse(new IndicesExistsResponse(exists)), l::onFailure));
    }

    /*
     * The five handlers below use the RHLC's typed (server-request) admin variants. On 1.3 they are the only faithful
     * mapping: they send include_type_name=true, so custom mapping type names survive exactly as with the
     * TransportClient. They are deprecated in 1.x and removed in 2.0; at the bump they become the untyped
     * org.opensearch.client.indices.* requests (CreateIndexRequest, PutMappingRequest, GetMappingsRequest,
     * PutIndexTemplateRequest, GetIndexTemplatesRequest), converted from the server request.
     */
    @SuppressWarnings("deprecation")
    private static void createIndex(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        c.rest.indices().createAsync((CreateIndexRequest) r, c.options, l(l));
    }

    @SuppressWarnings("deprecation")
    private static void putMapping(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        c.rest.indices().putMappingAsync((PutMappingRequest) r, c.options, l(l));
    }

    @SuppressWarnings("deprecation")
    private static void getMappings(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        c.rest.indices().getMappingAsync((GetMappingsRequest) r, c.options, l(l));
    }

    @SuppressWarnings("deprecation")
    private static void putTemplate(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        c.rest.indices().putTemplateAsync((PutIndexTemplateRequest) r, c.options, l(l));
    }

    @SuppressWarnings("deprecation")
    private static void getTemplates(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        GetIndexTemplatesRequest request = (GetIndexTemplatesRequest) r;
        org.opensearch.client.indices.GetIndexTemplatesRequest get =
                new org.opensearch.client.indices.GetIndexTemplatesRequest(request.names());
        get.setLocal(request.local());
        get.setMasterNodeTimeout(request.masterNodeTimeout());
        c.rest.indices().getTemplateAsync(get, c.options, ActionListener.wrap(l::onResponse, e -> {
            // the transport action answers an unknown template name with an empty list, REST with a 404
            if (e instanceof OpenSearchStatusException && ((OpenSearchStatusException) e).status() == RestStatus.NOT_FOUND) {
                l.onResponse(new GetIndexTemplatesResponse(Collections.emptyList()));
            } else {
                l.onFailure(e);
            }
        }));
    }

    /**
     * The RHLC has no cluster-state API. This reads {@code GET /_cluster/state/metadata[/indices]} with the low-level
     * client and rebuilds a {@link ClusterState} that carries the cluster name and the indices' metadata (settings,
     * mappings, aliases). Routing, nodes and blocks are not populated.
     */
    private static void clusterState(RestBridgeClient c, ActionRequest r, ActionListener<Object> l) {
        ClusterStateRequest request = (ClusterStateRequest) r;
        String[] indices = request.indices();
        String endpoint = "/_cluster/state/metadata"
                + (indices == null || indices.length == 0 ? "" : "/" + String.join(",", indices));
        Request get = new Request("GET", endpoint);
        get.setOptions(c.options);
        c.rest.getLowLevelClient().performRequestAsync(get, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                try (InputStream in = response.getEntity().getContent()) {
                    l.onResponse(parseClusterState(in));
                } catch (Exception e) {
                    l.onFailure(e);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                l.onFailure(exception);
            }
        });
    }

    @SuppressWarnings("unchecked")
    static ClusterStateResponse parseClusterState(InputStream in) throws IOException {
        Map<String, Object> body = XContentHelper.convertToMap(XContentType.JSON.xContent(), in, true);
        ClusterName clusterName = new ClusterName(String.valueOf(body.getOrDefault("cluster_name", ClusterName.DEFAULT.value())));
        Map<String, Object> metadata = (Map<String, Object>) body.getOrDefault("metadata", Collections.emptyMap());

        Metadata.Builder builder = Metadata.builder();
        Object clusterUuid = metadata.get("cluster_uuid");
        if (clusterUuid != null) {
            builder.clusterUUID(clusterUuid.toString());
        }
        Map<String, Object> indices = (Map<String, Object>) metadata.getOrDefault("indices", Collections.emptyMap());
        for (Map.Entry<String, Object> index : indices.entrySet()) {
            Map<String, Object> indexBody = new LinkedHashMap<>((Map<String, Object>) index.getValue());
            Object aliases = indexBody.get("aliases");
            if (aliases instanceof List) {
                // the REST (API) form lists alias names; the parser reads the stored form {"<alias>": {...}}
                Map<String, Object> aliasObjects = new LinkedHashMap<>();
                for (Object alias : (List<Object>) aliases) {
                    aliasObjects.put(String.valueOf(alias), Collections.emptyMap());
                }
                indexBody.put("aliases", aliasObjects);
            }
            // IndexMetadata.fromXContent reads one {"<name>": {...}} object, so each index gets its own parser
            XContentBuilder single = XContentFactory.jsonBuilder().startObject().field(index.getKey(), indexBody).endObject();
            try (XContentParser parser = XContentType.JSON.xContent().createParser(NamedXContentRegistry.EMPTY,
                    DeprecationHandler.THROW_UNSUPPORTED_OPERATION, BytesReference.bytes(single).streamInput())) {
                builder.put(IndexMetadata.fromXContent(parser), false);
            }
        }
        ClusterState state = ClusterState.builder(clusterName).metadata(builder).build();
        return new ClusterStateResponse(clusterName, state, false);
    }
    //endregion

    //region Exception translation
    private static final Pattern ERROR_TYPE = Pattern.compile("type=([a-z_]+), reason=(.*)\\]$", Pattern.DOTALL);
    private static final Pattern NO_SUCH_INDEX = Pattern.compile("no such index \\[([^\\]]*)\\]");

    private static ActionListener<Object> translating(ActionListener<Object> listener) {
        return new ActionListener<Object>() {
            @Override
            public void onResponse(Object response) {
                listener.onResponse(response);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(translate(e));
            }
        };
    }

    /**
     * Re-creates the transport-layer exception types callers catch from the REST error the RHLC reports.
     * Anything else is passed through unchanged.
     */
    static Exception translate(Exception e) {
        if (!(e instanceof OpenSearchStatusException)) {
            return e;
        }
        OpenSearchException ose = (OpenSearchException) e;
        Matcher m = ERROR_TYPE.matcher(String.valueOf(ose.getMessage()));
        if (!m.find()) {
            return e;
        }
        String type = m.group(1);
        String reason = m.group(2);
        switch (type) {
            case "index_not_found_exception": {
                IndexNotFoundException translated = new IndexNotFoundException(indexName(ose, reason), e);
                return translated;
            }
            case "resource_already_exists_exception": {
                ResourceAlreadyExistsException translated = new ResourceAlreadyExistsException(reason);
                translated.initCause(e);
                return translated;
            }
            case "version_conflict_engine_exception":
                return new VersionConflictEngineException((ShardId) null, "{}", e, reason);
            default:
                return e;
        }
    }

    private static String indexName(OpenSearchException e, String reason) {
        List<String> index = e.getMetadata("opensearch.index");
        if (index != null && !index.isEmpty()) {
            return index.get(0);
        }
        List<String> resource = e.getMetadata("opensearch.resource.id");
        if (resource != null && !resource.isEmpty()) {
            return resource.get(0);
        }
        Matcher m = NO_SUCH_INDEX.matcher(reason);
        return m.find() ? m.group(1) : reason;
    }
    //endregion

    private static ThreadPool newThreadPool() {
        // AbstractClient needs a thread pool (BulkProcessor and friends schedule on it); REST calls don't use it
        return new ThreadPool(Settings.builder().put("node.name", "yangdb-rest-bridge").build());
    }
}
