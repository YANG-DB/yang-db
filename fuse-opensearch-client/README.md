# fuse-opensearch-client

A remote `org.opensearch.client.Client` over HTTP. OpenSearch 2.x has no `TransportClient`, and nothing in 2.x
implements `Client` for a remote cluster. `RestBridgeClient` (an `AbstractClient`) fills that gap: every
`client.prepareX(...).get()` reaches `doExecute(action, request, listener)`, which forwards the request to the REST
high-level client (RHLC). Code that only knows `Client` keeps working unchanged.

Design and context: [docs/revival/OPENSEARCH_MIGRATION.md §4.1](../docs/revival/OPENSEARCH_MIGRATION.md).

## Use
```java
Client client = ClientFactory.remote(new String[]{"opensearch"}, 9200,
        Settings.builder().put("cluster.name", "Dragons").build());
```
- Hosts: `host`, `host:port` or `scheme://host:port`. A host without a port gets the given HTTP port.
- Like the `TransportClient`, construction is lazy: nothing connects until the first request.
- `close()` closes the REST client and the bridge's thread pool.
- `Settings` keys: `cluster.name` (informational, not verified), `opensearch.http.socket_timeout_ms` (default 300000),
  `opensearch.http.connect_timeout_ms` (default 5000).

## Config mapping: transport port → HTTP port
The bridge talks to the REST port (9200), not the transport port (9300). Every remote construction site accepts a new
explicit HTTP-port key. If it's absent, the old transport-port key is mapped to **transport port − 100**
(9300 → 9200; the test convention 9460 → 9360 also holds). That fallback is **deprecated**: it logs a warning once.

| Construction site | Old key (transport) | New key (HTTP) | Fallback |
|---|---|---|---|
| fuse-dv-core `ClientProvider` (service config) | `opensearch.port` | `opensearch.http.port` | `opensearch.port − 100`; neither key → 9200 |
| fuse-core `SnifferFuseClient` (`application.conf`) | `opensearch.port` | `opensearch.http.port` | `opensearch.port − 100` |
| fuse-dv-epb `ElasticClientProvider` (`StatConfig`) | `opensearch.stat.port` | `opensearch.stat.http.port` | `opensearch.stat.port − 100` |
| fuse-dv-stat `ClientProvider` (properties) | `es.client.transport.port` / `statistics.client.transport.port` | `es.client.http.port` / `statistics.client.http.port` | transport − 100 |
| test framework `ElasticEmbeddedNode.getClient(cluster, transportPort)` (reuse a running cluster) | `-Dfuse.test.transport.port` | `-Dfuse.test.http.port` | the node's HTTP port when `transportPort` is the configured transport port, else − 100 |

`client.transport.ignore_cluster_name` is still accepted and has no effect. Hosts keep their keys
(`opensearch.hosts`, `opensearch.stat.hosts`, `es.nodes.hosts`, `statistics.nodes.hosts`).

## Covered actions
| Area | Action → RHLC call |
|---|---|
| Reads | search → `searchAsync`, scroll → `scrollAsync`, clear-scroll → `clearScrollAsync`, multi-search → `msearchAsync`, get → `getAsync`, multi-get → `mgetAsync`, explain → `explainAsync`, field caps → `fieldCapsAsync` |
| Writes | index → `indexAsync`, bulk → `bulkAsync`, delete → `deleteAsync`, update → `updateAsync` |
| Indices admin | create → `indices().createAsync`, delete → `indices().deleteAsync`, exists → `indices().existsAsync` (client `GetIndexRequest`), refresh → `indices().refreshAsync`, put mapping → `putMappingAsync`, get mappings → `getMappingAsync`, put template → `putTemplateAsync`, get templates → `getTemplateAsync` (404 → empty list, as the transport action), delete template → `deleteTemplateAsync` |
| Cluster admin | state → low-level `GET /_cluster/state/metadata[/indices]`, rebuilt into a `ClusterState` holding the cluster name and the indices' metadata (settings, mappings, aliases). Routing, nodes, blocks and templates are not populated |

`RestBridgeClient.supportedActions()` returns this set.

**Not covered** (fail loudly with `UnsupportedOperationException(action.name())`, never silently):
- term vectors and multi term vectors: the server `TermVectorsResponse` holds Lucene binary term data that can't be
  rebuilt from the REST JSON. The only main-code references are pass-through wrappers (`LoggingClient`,
  `TimeoutClientAdvisor`); nothing calls them for real.
- indices stats (used only by `EsUtil.getAllIndices`, which only an epb test calls, with the embedded client), cluster
  health, and every other action.

**Errors.** The RHLC reports server errors as `OpenSearchStatusException`. The exception types this codebase catches
are re-created: `index_not_found_exception` → `IndexNotFoundException`, `resource_already_exists_exception` →
`ResourceAlreadyExistsException`, `version_conflict_engine_exception` → `VersionConflictEngineException`. Others
pass through unchanged.

**Mapping types (1.3).** Create index, put/get mapping, put/get template use the RHLC's *typed* variants, which send
`include_type_name=true`. Custom mapping type names (e.g. `pge`) therefore survive exactly as with the
`TransportClient`; typed index/get/search requests also keep their type in the REST path.

## Sniffing
No REST node sniffer is wired in. `SnifferFuseClient`'s "sniffing" reads YANG-DB node status from the
`fuse_node_info` index; it never turned on the `TransportClient`'s `client.transport.sniff`. So there is no
OpenSearch-node sniffing to carry over. If wanted later, add `org.opensearch.client:opensearch-rest-client-sniffer`
(not in the build today) and attach a `Sniffer` to `restClient().getLowLevelClient()` in `ClientFactory`.

## Tests
- `RestBridgeClientTest`: one test per covered action (RHLC mocked with the inline mock maker, since its methods are
  final): the request reaches the right RHLC method and the response comes back unchanged; request conversions
  (exists, get templates), the 404 → empty templates rule, cluster-state parsing, error translation, and loud failure
  of unsupported actions.
- `ClientFactoryTest`: port mapping and host parsing.
- `RestBridgeClientRemoteIT`: starts an OpenSearch node on `-Dfuse.test.http.port`/`-Dfuse.test.transport.port`
  and talks to it **only** through `ClientFactory.remote(...)` over HTTP: typed create index, index/get/update/delete,
  bulk, refresh, search, scroll, clear-scroll, multi-get, multi-search, explain, field caps, put/get mappings,
  put/get/delete templates, exists, delete index, cluster state, error types. With
  `-Dfuse.bridge.it.remote=host:port` it runs against an external cluster (e.g. Docker) instead.

## After the 2.x bump
1. **Typed admin variants are removed from the 2.x RHLC.** Replace the five handlers marked in `RestBridgeClient`
   (`createIndex`, `putMapping`, `getMappings`, `putTemplate`, `getTemplates`) with conversions to the untyped
   `org.opensearch.client.indices.*` requests (`CreateIndexRequest`, `PutMappingRequest`, `GetMappingsRequest`,
   `PutIndexTemplateRequest`, `GetIndexTemplatesRequest`), and convert their responses back to the server types
   (`CreateIndexResponse`, `GetMappingsResponse` is untyped in 2.x, `GetIndexTemplatesResponse`). On 2.x the server
   `CreateIndexRequest.mappings()` is a single `String`, not a type-keyed map.
2. **Package relocations** (2.10+): `ActionListener`, `ActionResponse` → `org.opensearch.core.action`;
   `XContentParser`, `XContentBuilder`, `NamedXContentRegistry`, `DeprecationHandler` → `org.opensearch.core.xcontent`;
   `BytesReference` → `org.opensearch.core.common.bytes`; `RestStatus` → `org.opensearch.core.rest`;
   `ShardId` → `org.opensearch.core.index.shard`. Mechanical, like the rest of step 5.2.
3. **Typed request builders** in the tests (`prepareIndex(index, type, id)`, `setTypes`, `addMapping(type, ...)`) go
   away with step 5.3; the IT and unit tests then drop their `pge` type.
4. **Check** that `IndicesExistsAction` still exists on 2.19 (otherwise map `admin().indices().exists(...)` callers to
   the get-index API), that `AbstractClient`'s constructor is still `(Settings, ThreadPool)`, and that the cluster-state
   parser still accepts the 2.19 `GET /_cluster/state/metadata` output (the IT covers it).
5. The IT's embedded node needs `transport-netty4-client` at the 2.19 version (already in `~/.m2`).
