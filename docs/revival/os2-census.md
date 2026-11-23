# Phase 5.0: OpenSearch 2.19.7 compile census

Step 5.0 of [OPENSEARCH_UPGRADE.md](OPENSEARCH_UPGRADE.md). Method: on a throwaway worktree of `phase5-opensearch-2.19`
(base `09b1c8699`), set `opensearch.version` and `opensearch.client.version` to **2.19.7** and `lucene.version` to
**9.12.3** (the version the 2.19.7 server pom declares). Then build online (`compile`, then `test-compile` with
`-Pcore,dragons-assembly`) and apply throwaway fixes until each module compiled, recording every compiler error on the
way. `-Xmaxerrs 5000` was set, so javac never truncated its output. The end state was green: `clean test-compile` over
all **23 reactor modules**, including examples, distribution and `extensions/graphql`. Run date: 2026-11-21.

**Headline:** the compile-level breakage is much smaller than OPENSEARCH_MIGRATION.md planned for. **207 compiler
errors, plus 20 relocated imports fixed by script before the compiler reached them**, in about 35 files. 152 of the
errors are in `fuse-dv-core`, and most of those are in three `Client` wrapper classes. The real risks are
**dependency conflicts that compile cleanly but fail at runtime** (§4): the netty pin, Jackson, log4j, and a new one
(**ANTLR: GraphQL vs Painless**).

---

## 1. Summary: compiler errors per module × category
The categories are from OPENSEARCH_MIGRATION.md §4: **a** package relocation, **b** removed or changed API, **c** mapping
types, **d** TransportClient / remote construction site, **e** embedded node / harness, **f** dependency conflict,
**g** other.

| module | src | a | b | c | d | e | f | g | total |
|---|---|---|---|---|---|---|---|---|---|
| fuse-core | main | 1 |  | 1 | 2 |  |  |  | 4 |
| fuse-test-framework | main | 1 |  | 1 | 1 |  |  |  | 3 |
| fuse-dv-unipop | main | 25 | 5 | 3 |  |  |  |  | 33 |
| fuse-dv-unipop | test |  |  | 3 |  |  |  |  | 3 |
| fuse-dv-core | main | 96 |  | 56 |  |  |  |  | 152 |
| fuse-dv-core | test |  |  | 4 |  |  |  |  | 4 |
| fuse-dv-stat | main |  | 3 | 2 |  |  | 1 |  | 6 |
| fuse-dv-stat | test |  |  | 1 |  |  |  |  | 1 |
| fuse-service | main |  |  | 1 |  |  |  |  | 1 |
| **total (compiler-reported)** | | **123** | **8** | **72** | **3** | **0** | **1** | **0** | **207** |
| pre-fixed by script / hidden by stubs | | +20 imports | +12 (PIT methods, §3.2) | | +6 files (§3.4) | | +4 runtime (§4) | | |

Modules with **zero** errors on 2.19.7: `unipop-core`, `fuse-model`, `fuse-extension-api`, `fuse-geojson`, `fuse-asg`
(only one relocated import, `common.Strings`), `fuse-dv-asg`, `fuse-dv-gta`, `fuse-dv-epb` (apart from its (d) site),
`fuse-extension-graphql`, `fuse-domain-dragons-ext`, `fuse-domain-dragons-datagen`, and `fuse-domain-dragons-assembly`
(distribution). `es-plugins` isn't in any reactor profile, so it wasn't built.

How to read the counts:
- The 96 (a) errors in fuse-dv-core all come from three files that use `import org.opensearch.action.*;`:
  `ActionFuture`, `ActionListener` and `ActionResponse` left that package. Adding three imports fixes them.
- javac reports only the first missing abstract method per class, so the PIT methods were found with `javap` (§3.2).
- With `-fae`, a module stops compiling at its first failing module upstream. The table counts errors as each module
  was first reached, after the upstream temporary fixes in §5.

---

## 2. Dependency changes the bump forces (before any code compiles)
| Change | Old → new | Why |
|---|---|---|
| `org.opensearch.client:transport` | **does not exist for 2.x** (the last release is 1.3.20; 2.19.7 returns 404) → `org.opensearch.plugin:transport-netty4-client:2.19.7` | All 9 poms that declared it (root dependencyManagement, es-plugins, fuse-core, dv-core, dv-epb, dv-stat, dv-unipop, fuse-service, fuse-test-framework). The old artifact also brought `Netty4Plugin`, which `ElasticEmbeddedNode` needs. Its other plugins (reindex, percolator, parent-join, rank-eval, mustache) aren't used at compile time. |
| `lucene.version` | 8.10.1 → **9.12.3** (or delete the pin) | The 2.19.7 server pom declares 9.12.3 |

With that swap alone, the reactor reaches javac in every module.

---

## 3. Per category: concrete changes (old → new) and files

### 3.1 (a) Package relocations — 123 errors + 20 pre-fixed imports
| Old | New | Files |
|---|---|---|
| `org.opensearch.action.ActionFuture` | `org.opensearch.common.action.ActionFuture` | dv-core: `LoggingActionFuture`, `ESPutIndexTemplateRequestBuilder`; through `org.opensearch.action.*`: `LoggingClient`, `TimeoutClientAdvisor`, `MockClient` |
| `org.opensearch.action.ActionListener` | `org.opensearch.core.action.ActionListener` | dv-core: `LoggingActionFuture`, `LoggingActionListener`, plus the 3 wildcard files above |
| `org.opensearch.action.ActionResponse` | `org.opensearch.core.action.ActionResponse` | the 3 wildcard files above |
| `org.opensearch.common.xcontent.{XContentParser, XContentBuilder, NamedXContentRegistry, DeprecationHandler, XContentBuilderExtension, XContent, XContentGenerator, XContentLocation, ToXContent}` | `org.opensearch.core.xcontent.*` (`XContentType`, `XContentFactory` and `json.JsonXContent` **stay** in `common.xcontent`) | dv-unipop: `QueryBuilder`, `AbstractFuseXContentParser`, `FuseXContentBuilderExtension`, `FuseJsonXContent`, `FuseJsonXContentParser`, `SearchHitUtils` (the last 3 use `common.xcontent.*`: add `import org.opensearch.core.xcontent.*;`) |
| `org.opensearch.common.transport.TransportAddress` | `org.opensearch.core.common.transport.TransportAddress` | the 6 (d) files: core `SnifferFuseClient`; dv-core, dv-stat `ClientProvider`; epb `ElasticClientProvider`; both `ElasticEmbeddedNode`s. This goes away with the bridge |
| `org.opensearch.rest.RestStatus` | `org.opensearch.core.rest.RestStatus` | dv-core: `PersistantNodeStatusResource`, `PersistentResourceStore` |
| `org.opensearch.common.Strings` | `org.opensearch.core.common.Strings` | asg: `DefaultETagAsgStrategy` |
| `org.opensearch.common.bytes.BytesReference` | `org.opensearch.core.common.bytes.BytesReference` | dv-unipop: `SearchHitUtils` |
| `org.opensearch.common.text.Text` | `org.opensearch.core.common.text.Text` | dv-unipop test: `ElementControllerCompositTest` |

The relocation script used (perl, idempotent) is in §5.2.

### 3.2 (b) Removed or changed API — 8 errors + 12 hidden
| Old | New | File |
|---|---|---|
| `XContentParser` gains abstract `bigIntegerValue()` and `bigIntegerValue(boolean)`; `contentType()` now returns `MediaType` (an `XContentType` return still compiles, because it's covariant) | implement both | dv-unipop `FuseJsonXContentParser` |
| `XContent.type()` → `XContent.mediaType()` (returns `MediaType`); `XContentGenerator.contentType()` / `writeRaw*(…, XContentType)` now take `MediaType` | rename; the generator signatures matter only if we implement `XContentGenerator` (we don't today) | dv-unipop `FuseJsonXContent` |
| `XContentFactory.xContentType(InputStream)` removed | `org.opensearch.core.xcontent.MediaTypeRegistry.xContentType(InputStream)` (returns `MediaType`) | dv-unipop `SearchHitUtils` |
| `org.opensearch.common.compress.CompressorFactory` removed | `org.opensearch.core.compress.CompressorRegistry` (`isCompressed`, `compressor`, `uncompressIfNeeded`) | dv-unipop `SearchHitUtils` |
| `org.opensearch.common.collect.ImmutableOpenMap` (`keys()`) removed from public APIs | `java.util.Map` (`keySet()`) | dv-stat `EsUtil` |
| **Hidden (found with javap):** `Client` gained 4 abstract PIT methods: `createPit`, `deletePits`, `getAllPits`, `pitSegments` | implement them (delegate, or throw `UnsupportedOperationException`) | dv-core `LoggingClient`, `TimeoutClientAdvisor`, `MockClient` (4 × 3 = 12) |

`AbstractClient` keeps the same shape: `AbstractClient(Settings, ThreadPool)` and
`doExecute(ActionType<R>, Req, ActionListener<R>)`. Only `ActionListener` and `ActionResponse` moved to `core.action`.
That matters for the bridge (§6.1).

### 3.3 (c) Mapping types — 72 errors
The full `Client` diff, 1.3.0 → 2.19.7 (javap). Each typed overload lost its `type` parameter:

| 1.3.0 | 2.19.7 |
|---|---|
| `prepareIndex(index, type)` / `prepareIndex(index, type, id)` | `prepareIndex(index)` + `.setId(id)` |
| `prepareGet / prepareDelete / prepareUpdate / prepareTermVectors / prepareExplain(index, type, id)` | `(index, id)` |
| `prepareBulk(globalIndex, globalType)` | `prepareBulk(globalIndex)` |

Other type-related removals:

| Old | New |
|---|---|
| `IndexRequestBuilder.setType(..)`, `SearchRequestBuilder.setTypes(..)` | removed. Drop them; anything that filtered by type must filter by index or by a field |
| `CreateIndexRequestBuilder.addMapping(type, Map)` | `setMapping(Map)` (no type wrapper) |
| `PutIndexTemplateRequestBuilder.addMapping(type, String, XContentType)` | `setMapping(String, MediaType)` |
| `PutIndexTemplateRequest.mapping(type, Map)` | `mapping(Map)` |
| `IndexMetadata.getMappings()` (type → mapping map) | `IndexMetadata.mapping()` (a single `MappingMetadata`) |
| `IndexTemplateMetadata.mappings()` returns `ImmutableOpenMap<type, CompressedXContent>` (`keysIt()`, `get(type)`) | returns a single `CompressedXContent` |
| `QueryBuilders.idsQuery(String... types)` | `idsQuery()` |
| `QueryBuilders.typeQuery(type)` | removed |
| `new SearchHit(docId, id, Text type, fields, metaFields)` | `new SearchHit(docId, id, NestedIdentity, fields, metaFields)` |

Files, main code (17), with error counts:

| Module | File | Errors |
|---|---|---|
| fuse-core | `client/SnifferFuseClient`: `setTypes(RESOURCE)` on the `fuse_node_info` scan | 1 |
| fuse-test-framework | `index/MappingElasticConfigurer` | 1 |
| fuse-service | `embedded/MappingElasticConfigurer` | 1 |
| fuse-dv-unipop | `controller/search/QueryBuilder` (`idsQuery(types)` ×2, `typeQuery`) | 3 |
| fuse-dv-core | `opensearch/ElasticIndexProviderMappingFactory` (4 chained `addMapping(label, …)` on the base builder) | 4 |
| fuse-dv-core | `opensearch/ESPutIndexTemplateRequestBuilder` (its own `addMapping(type, Map)` is no longer an override; `request.mapping(type, Map)`) | 2 |
| fuse-dv-core | `ontology/schema/load/IndexProviderBasedGraphLoader`, `LoadUtils` (`setType(node.getType())`) | 2 |
| fuse-dv-core | `resource/PersistentResourceStore` (5), `PersistantNodeStatusResource` (2): typed get, index and delete on the system index | 7 |
| fuse-dv-core | `opensearch/logging/LoggingClient`, `opensearch/TimeoutClientAdvisor`, `mock/opensearch/MockClient`: the typed `Client` overloads (overrides plus delegation) | 41 |
| fuse-dv-stat | `es/populator/ElasticDataPopulator` (`setType`), `util/EsUtil` (`prepareIndex(index, type, id)`; `getAllTypesFromIndex` is meaningless without types) | 2 |

Files, tests (5): dv-core `IndexProviderBasedCSVLoaderIT` (`prepareGet(idx, null, id)` ×2) and
`OpensearchIndexProviderMappingFactoryIT` (`template.mappings().keysIt()` / `.get(typeKey)`: the structural
assertions must drop the type key, as planned); dv-unipop `DiscreteElementReduceControllerTests` and
`DiscreteTraversalTest` (`addMapping("pge", json, XContentType.JSON)`), and `ElementControllerCompositTest` (the
`SearchHit` constructor); dv-stat `StatTestUtil` (`setTypes`).

**Semantic follow-ups for stream R** (these compile once the type argument is dropped, but the behaviour changes):
- `PersistentResourceStore`, `PersistantNodeStatusResource` and `SnifferFuseClient` keep several resource kinds in one
  system index, separated by **type** (`RESOURCE` …). Without types, documents of different kinds share one id space
  and one mapping. They need a discriminator field or separate indices.
- `ElasticIndexProviderMappingFactory`: the generated mapping maps are still keyed `{label: {properties…}}`. They must
  be emitted without the type wrapper (`{properties…}`). The temporary fix only made the code compile.
- `QueryBuilder.typeQuery`: the unipop type constraint needs a replacement (a term query on the label field, or an
  index filter).
- `EsUtil.getAllTypesFromIndex` (dv-stat) now always returns one entry.
- `ElementGlobalTypeSearchAppender` contains a `"_type"` terms filter, but it's commented out today, so nothing to do.

### 3.4 (d) TransportClient / remote construction sites — 3 errors seen, 8 files in total
`org.opensearch.client.transport.TransportClient` and `org.opensearch.transport.client.PreBuiltTransportClient` are
gone. The compiler saw them only in fuse-core and fuse-test-framework, because throwaway stub classes (§5.1) stood in
for them downstream. The complete footprint:

| Module | File | Use |
|---|---|---|
| fuse-core | `client/SnifferFuseClient` | builds a `PreBuiltTransportClient` |
| fuse-dv-core | `executor/opensearch/ClientProvider` | builds one (the main remote `Client` provider) |
| fuse-dv-epb | `epb/plan/statistics/provider/ElasticClientProvider` | builds one; its public `getStatClient()` returns `TransportClient` |
| fuse-dv-stat | `stat/es/client/ClientProvider` | 3 public static methods return `TransportClient`; `StatCalculator` calls them |
| fuse-service | `services/embedded/ElasticEmbeddedNode` | `static TransportClient getClient(...)` (the reuse-cluster path) |
| fuse-service | `services/embedded/ElasticIndexConfigurer`, `MappingElasticConfigurer` | `configure(TransportClient)` parameter type → change it to `Client` |
| fuse-test-framework | `index/ElasticEmbeddedNode` | `PreBuiltTransportClient` on the reuse-running-cluster path (T1) |

### 3.5 (e) Embedded node / harness — 0 compile errors
- `Node`'s constructor is **unchanged**: `protected Node(Environment, Collection<Class<? extends Plugin>>, boolean)`
  and `InternalSettingsPreparer.prepareEnvironment(Settings, Map, Path, Supplier<String>)` exist as before. Both
  `PluginConfigurableNode` subclasses compile as-is. `Node.client()` and `Node.start()` are unchanged.
- `org.opensearch.transport.Netty4Plugin` is still the class name, now in `transport-netty4-client` (§2).
  `org.opensearch.painless.PainlessPlugin` compiles from `lang-painless:2.19.7`.
- Runtime items that compiling can't show, for stream T:
  1. the ANTLR conflict (§4.3), which decides whether Painless can load at all;
  2. 2.x settings names (`cluster_manager` replaces `master` in node roles; `cluster.initial_cluster_manager_nodes`),
     `discovery.type=single-node`;
  3. Painless `ExtensiblePlugin` SPI extensions when it's loaded as a classpath plugin;
  4. netty alignment (§4.1). The embedded node *is* the 4.1.139 netty consumer.

### 3.6 (f) Dependency conflicts — 1 compile error
- **commons-codec:** dv-stat `StatUtil` used `org.apache.commons.codec.binary.Base64`, which arrived transitively
  through the 1.3 `transport` artifact. Fix: declare `commons-codec` in fuse-dv-stat (the 2.19 rest client brings
  1.18.0), or use `java.util.Base64.getUrlEncoder().withoutPadding()`.
- The runtime conflicts are in §4.

### 3.7 (g) Other — none
Three deprecation warnings, no other errors.

---

## 4. Answers to OPENSEARCH_MIGRATION.md §8 (plus dependency versions)

### 4.1 Netty and Jooby 1.x: **yes, one version serves both (4.1.139.Final), and the A1 pin must move**
- OpenSearch 2.19.7 (`transport-netty4-client`) needs **netty 4.1.139.Final**.
- `mvn dependency:tree -Dincludes=io.netty -pl fuse-service` today gives a **mixed** tree:
  - `netty-buffer` and `netty-common` at **4.1.73** (the A1 pin wins);
  - `netty-codec`, `-codec-http`, `-handler`, `-transport`, `-resolver` and `-transport-native-unix-common` at
    **4.1.139**;
  - jooby-netty's `netty-codec-http2` and `netty-transport-native-epoll` at **4.1.43**;
  - `netty-tcnative-boringssl-static` at 2.0.25.
- That's the A1 failure mode again. A bytecode linkage check (the bytecode references of each jar resolved against a
  candidate classpath) shows:
  - `netty-transport` 4.1.139 against `netty-common` 4.1.73: `NoClassDefFoundError io/netty/util/Recycler$EnhancedHandle`;
  - `netty-codec-http` 4.1.139 against `netty-common` 4.1.73: missing `StringUtil.decodeHexNibble(B)` and
    `substringBefore`.

  **The current pin breaks the embedded node and HTTP at runtime.**
- `jooby-netty` 1.6.9 against a **uniform 4.1.139** classpath: **237 of 237** `io.netty` references
  (methods, constructors, fields) resolve. Netty 4.1 keeps binary backward compatibility, so Jooby compiled on 4.1.43
  links on 4.1.139.
- **Action (stream S):** replace the A1 `netty-buffer:4.1.73.Final` pin with a managed **4.1.139.Final** for *every*
  netty artifact, including `netty-codec-http2`, `netty-transport-native-epoll` and `netty-transport-classes-epoll`.
  Importing `io.netty:netty-bom:4.1.139.Final` is easiest. Bump `netty-tcnative-boringssl-static` to **2.0.84.Final**
  (netty 4.1.139's version), or exclude it if TLS isn't used. Then re-run the A1 `ByteBuf` test. No shading and no
  Jetty fallback should be needed. This is still to be proven at runtime by the fuse-service tests.

### 4.2 RHLC async coverage: **yes for every data action; admin is partial**
`RestHighLevelClient` 2.19.7 (javap) has async variants for all the bridge's read and write actions:
`searchAsync`, `indexAsync`, `bulkAsync`, `getAsync`, `deleteAsync`, `updateAsync`, `scrollAsync`/`searchScrollAsync`,
`clearScrollAsync`, `mgetAsync`/`multiGetAsync`, `msearchAsync`/`multiSearchAsync`, `fieldCapsAsync`, `explainAsync`,
`termvectorsAsync`, `mtermvectorsAsync` (also `countAsync`, `existsAsync`, the PIT calls, by-query actions).

Admin calls go through `IndicesClient`:
- `createAsync`, `deleteAsync`, `putMappingAsync`, `getMappingAsync`, `putTemplateAsync`,
  `getIndexTemplateAsync`, `deleteTemplateAsync`, `refreshAsync`, `existsAsync`, `getAsync`.
- **Caveat 1:** create, put/get mapping, put template, get templates and get index take **client-side request
  types** (`org.opensearch.client.indices.CreateIndexRequest`, `PutIndexTemplateRequest`, `GetMappingsRequest`, …).
  They don't take the server `ActionRequest`s that `AbstractClient.doExecute` receives, so the bridge must translate
  requests and responses for these. Delete and refresh take the server types directly.
- **Caveat 2:** `ClusterClient` has **no cluster-state call** (only health, settings, component templates and remote
  info). `admin().cluster().prepareState()` (used by dv-stat `EsUtil`, among others) needs the low-level client
  (`performRequestAsync(GET /_cluster/state/…)`) plus parsing, or a rewrite of the call site to `GetMappings`.
- The constructor is unchanged: `new RestHighLevelClient(RestClient.builder(hosts…))`.

### 4.3 Embedded `Node` construction and plugin loading
The API is unchanged (§3.5). `Netty4Plugin` comes from `org.opensearch.plugin:transport-netty4-client`, and
`PainlessPlugin` from `org.opensearch.plugin:lang-painless`. Both are loaded as classpath plugins the same way as on
1.3.

**New blocker found by the census: the ANTLR runtime.**
- `lang-painless` 2.19.7's parser is generated with **ANTLR 4.13.1** (`RuntimeMetaData.checkVersion("4.13.1")`;
  painless depends on antlr4-runtime 4.13.1).
- The root pom pins **`antlr4-runtime` 4.7.2**, because graphql-java `2020-01-17T02-41-10-578985f` is generated with
  4.7.2 (the Phase 4 G1 fix).
- ANTLR changed its serialized-ATN format in 4.10, so the two generated parsers can't share one runtime: with 4.7.2,
  Painless scripts (the C1 count filters) fail to load; with 4.13.1, every GraphQL query fails.
- Fix options:
  - **upgrade graphql-java** to a release generated with ANTLR ≥ 4.10 (any graphql-java built with ANTLR 4.11+, still
    Java 11). This means API changes in `extensions/graphql`;
  - or shade/relocate ANTLR inside the GraphQL extension;
  - or keep Painless out of any JVM that serves GraphQL. That fails for tests that run both in one JVM, so it isn't
    really an option.

### 4.4 Versions OpenSearch 2.19.7 requires
| Dependency | Repo today | 2.19.7 declares | Evidence of breakage |
|---|---|---|---|
| Lucene | 8.10.1 (pinned) | **9.12.3** | none at compile time after the bump |
| Jackson (core and dataformats) | `jackson.version` 2.13.4.2, `jackson.core.version` 2.13.4 (managed, so they **override** OpenSearch's) | **2.18.9** | linkage: `opensearch-x-content` 2.19.7 against jackson-core 2.13.4 is missing `StreamReadConstraints`, `StreamWriteConstraints`, `JsonFactory.setStreamReadConstraints`, `StreamReadFeature.USE_FAST_DOUBLE_PARSER` (Jackson 2.15+). **Every XContent parse fails at runtime.** Move to 2.18.x, and align `jackson-dataformat-*` and `jooby-jackson`'s databind |
| log4j | 2.17.1 (managed) | **2.25.5** | the tree is mixed today: `log4j-api` 2.17.1 next to `log4j-jul` 2.25.5. Align to 2.25.5 |
| netty | 4.1.73 pin | **4.1.139.Final** | §4.1 |
| antlr4-runtime | 4.7.2 pin | **4.13.1** (painless) | §4.3 |
| httpclient / httpcore / httpasyncclient | (transitive) | 4.5.14 / 4.4.16 / 4.1.5 | the rest client |
| commons-codec | (transitive from `transport`) | 1.18.0 (rest client) | §3.6 |

---

## 5. Temporary fixes made (throwaway; reuse them as starting points)
All of these were discarded after the census. They're listed so the streams can reuse the mechanical parts.

### 5.1 Build and stubs
1. Root pom: the versions in §2; `-Xmaxerrs 5000` added to the compiler plugin's `compilerArgs`.
2. All 9 poms: `org.opensearch.client:transport` → `org.opensearch.plugin:transport-netty4-client` (a perl one-liner on
   `<groupId>org.opensearch.client</groupId>\s*<artifactId>transport</artifactId>`).
3. **TransportClient stubs**, copied into the `src/main/java` of fuse-test-framework, fuse-core, fuse-service, dv-core,
   dv-epb and dv-stat, so the code downstream could compile. Streams Q/R/T can use the same trick until the bridge
   (5.1) lands:
   ```java
   package org.opensearch.client.transport;
   public abstract class TransportClient extends org.opensearch.client.support.AbstractClient {
       protected TransportClient(org.opensearch.common.settings.Settings s) { super(s, null); }
       public TransportClient addTransportAddress(org.opensearch.core.common.transport.TransportAddress a) { return this; }
       @Override protected <Q extends org.opensearch.action.ActionRequest, R extends org.opensearch.core.action.ActionResponse>
       void doExecute(org.opensearch.action.ActionType<R> a, Q q, org.opensearch.core.action.ActionListener<R> l) {
           throw new UnsupportedOperationException(a.name()); }
       @Override public void close() {}
   }
   // + org.opensearch.transport.client.PreBuiltTransportClient extends TransportClient { (Settings, Class<? extends Plugin>...) }
   ```

### 5.2 Relocations (category a): perl, idempotent
Run `perl -pi reloc.pl <files>` with:
```perl
s#org\.opensearch\.action\.ActionFuture\b#org.opensearch.common.action.ActionFuture#g;
s#org\.opensearch\.action\.ActionListener\b#org.opensearch.core.action.ActionListener#g;
s#org\.opensearch\.common\.Strings\b#org.opensearch.core.common.Strings#g;
s#org\.opensearch\.common\.bytes\.BytesReference\b#org.opensearch.core.common.bytes.BytesReference#g;
s#org\.opensearch\.common\.text\.Text\b#org.opensearch.core.common.text.Text#g;
s#org\.opensearch\.common\.transport\.TransportAddress\b#org.opensearch.core.common.transport.TransportAddress#g;
s#org\.opensearch\.common\.xcontent\.(DeprecationHandler|NamedXContentRegistry|XContentBuilder|XContentBuilderExtension|XContentParser|ToXContent|ToXContentObject)\b#org.opensearch.core.xcontent.$1#g;
s#org\.opensearch\.common\.ParseField\b#org.opensearch.core.ParseField#g;
s#org\.opensearch\.rest\.RestStatus\b#org.opensearch.core.rest.RestStatus#g;
```
Then:
- for each `import org.opensearch.common.xcontent.*;`, add `import org.opensearch.core.xcontent.*;`;
- for each `import org.opensearch.action.*;`, add explicit imports of `common.action.ActionFuture`,
  `core.action.ActionListener` and `core.action.ActionResponse`.

macOS `sed -E` doesn't support `\b`; use perl.

### 5.3 Removed APIs (category b)
- `FuseJsonXContentParser`: add both `bigIntegerValue` methods.
- `FuseJsonXContent`: `type()` → `mediaType()`.
- `SearchHitUtils`: `CompressorFactory` → `CompressorRegistry`; `XContentFactory.xContentType(in)` →
  `(XContentType) MediaTypeRegistry.xContentType(in)`.
- `EsUtil`: `ImmutableOpenMap` → `Map`, `keys()` → `keySet()`, `getMappings()` →
  `singletonMap("_doc", index(index).mapping())`.
- `StatUtil`: commons-codec → `java.util.Base64`.

### 5.4 Mapping types (category c): placeholders only, *not* the real semantics
- **Call sites:** a Python regex pass that rewrites:
  - `.prepare{Get,Delete,Update,TermVectors,Explain}(a, b, c)` → `(a, c)`;
  - `.prepareIndex(a, b, c)` → `.prepareIndex(a).setId(c)`;
  - `.prepareIndex(a, b)` → `.prepareIndex(a)`;
  - and deletes `.setType(…)` and `.setTypes(…)`.

  Arguments may contain one level of parentheses. Read and write files as latin-1: some sources aren't valid UTF-8.
- **Client wrappers** (`LoggingClient`, `TimeoutClientAdvisor`, `MockClient`), a regex pass that:
  - drops the 3-arg `prepareIndex` override;
  - turns `prepareIndex(String, String)` into `prepareIndex(String)`;
  - drops the type parameter from the typed overloads and their delegation calls;
  - appends the 4 PIT methods.
- `ElasticIndexProviderMappingFactory`: split the chained `.addMapping(label, …)` onto
  `request.addMapping(label, …)` (an `ESPutIndexTemplateRequestBuilder`). `ESPutIndexTemplateRequestBuilder`: remove
  the `@Override`, and `mapping(type, map)` → `mapping(map)`. **Still emits the type wrapper; R must remove it.**
- `MappingElasticConfigurer` (×2): `addMapping(key, map)` → `setMapping(map)`.
- `QueryBuilder`: `idsQuery(types)` → `idsQuery()`, `typeQuery(v)` → `matchAllQuery()`. **This is a placeholder
  only: it changes query semantics.**
- Tests:
  - `addMapping("pge", json, JSON)` → `setMapping(json, JSON)`;
  - in the `SearchHit` constructor, `new Text("test")` → `null`;
  - in `OpensearchIndexProviderMappingFactoryIT`, the type-key assertions → `template.mappings().string()`.

---

## 6. Recommended stream split (adjusts OPENSEARCH_UPGRADE.md)
The evidence changes the plan in three ways:
1. **5.2 is small and doesn't need two agents.** The Q1/Q2 split by module group isn't justified: 20 import lines,
   3 wildcard fixes and 5 small API changes, nearly all in dv-unipop and dv-core. One agent, Q.
2. **The three `Client` wrappers** (`LoggingClient`, `TimeoutClientAdvisor`, `MockClient`) mix categories a, b and c
   in the same files. Give them wholly to **Q**, so R and Q don't conflict there.
3. **5.4 grows.** The real risks are runtime dependency conflicts (netty, Jackson, log4j and the new ANTLR/GraphQL
   one). They're invisible to the compiler and must land **first**, because nothing can be tested until they do.

| Stream | Agent | Owns (modules / files) | Work | Estimate |
|---|---|---|---|---|
| **5.4 Dependencies** (lands first) | S | root `pom.xml`, all module poms, `fuse-service/pom.xml` (A1 pin), `extensions/graphql` (if graphql-java is upgraded), `docker/.env` | (1) versions 2.19.7, Lucene 9.12.3; (2) `transport` → `transport-netty4-client` in 9 poms; (3) netty BOM 4.1.139 + tcnative 2.0.84, re-check A1; (4) Jackson 2.18.x, log4j 2.25.5; (5) **ANTLR: upgrade graphql-java to an ANTLR ≥ 4.10 build and fix `extensions/graphql` against its API** (or shade); (6) commons-codec for dv-stat; (7) Docker `OS_VERSION=2.19.7` | **8–12 h** (graphql-java alone 3–5 h) |
| **5.2 Relocations + API** | Q (one agent) | fuse-asg, fuse-dv-unipop (`utils/opensearch/*`), fuse-dv-core (`opensearch/logging/*`, `TimeoutClientAdvisor`, `MockClient`), fuse-dv-stat (`EsUtil` maps), `fuse-dv-core` resource classes' imports | §5.2 and §5.3 changes; the wrappers: drop the typed overloads, add the PIT methods. Use the §5.1 stubs until P merges | **3–4 h** |
| **5.3 Mapping types** | R | fuse-dv-core (`ElasticIndexProviderMappingFactory`, `ESPutIndexTemplateRequestBuilder`, loaders, `PersistentResourceStore`, `PersistantNodeStatusResource`), fuse-core `SnifferFuseClient` (type filter only), fuse-dv-unipop `QueryBuilder` (`typeQuery`/`idsQuery`), fuse-dv-stat (`ElasticDataPopulator`, `EsUtil`), both `MappingElasticConfigurer`s, the 5 test files in §3.3 | untyped templates and mappings; a discriminator for the system index's resource kinds; replace `typeQuery`; structural IT updates | **8–10 h** |
| **5.1 Bridge** | P (already running) | new `fuse-opensearch-client`; the 8 files in §3.4 | as planned, plus: client-side request translation for create/mapping/template; cluster state via the low-level client; `configure(TransportClient)` → `Client` | unchanged (+2 h for the two caveats in §4.2) |
| **5.5 Harness** | T | both `ElasticEmbeddedNode`s, fuse-test-framework | runtime only (no compile work): node settings for 2.x (cluster_manager naming), the Painless SPI as a classpath plugin, the reuse path through the bridge, verify netty on the node | **4–6 h** |

**Merge order:** 5.4 → 5.2 → 5.3 → 5.1 → 5.5, as before. 5.2 and 5.3 can start in parallel right after this census:
they touch different lines, and the conflicts are limited to the wrapper files, which Q owns. The critical path is now
**5.4 (ANTLR/graphql-java) → 5.5**, not 5.3.

The compile streams (Q and R) total about **11–14 h**, against the original plan's 2 + 1 agents. The dependency stream
(S) carries the most risk.
