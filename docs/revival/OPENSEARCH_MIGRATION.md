# Migrating YANG-DB from OpenSearch 1.3.0 to a current OpenSearch

Conclusions, decisions and the technical migration guide. The step-by-step execution plan (sub-steps, dependency
graph, agent lanes) is in [OPENSEARCH_UPGRADE.md](OPENSEARCH_UPGRADE.md). Facts below were verified against this
repository and Maven Central on 2026-11-21. Items marked *(confirm in census)* are expectations that step 5.0 must
check against the real compiler output.

---

## 1. Summary of conclusions
1. **Target 2.19.x first, then 3.x.** Upgrade 1.3.0 → **2.19.7** (latest 2.x) as Phase 5, then → **3.x** (latest 3.10.0)
   as Phase 6. Going straight to 3.x would also mean a JDK move (11 → 21), Lucene 8 → 10, and losing the ability to
   read the 1.x indices users have today.
2. **Keep the `Client` interface and bridge it over REST.** `TransportClient` doesn't exist in 2.x. Instead of rewriting
   the ~45 files that use `org.opensearch.client.Client`, build **one adapter** that implements `Client` on top of the
   REST high-level client, and use it where we connect to a *remote* cluster. The REST high-level client is still
   published for both 2.19 and 3.10, so the adapter carries over to Phase 6.
3. **Remove mapping types properly.** 2.x has no mapping types. Our index templates and a handful of request builders
   still pass them. This is the one change with behavioural risk, and the Phase 3 structural mapping tests guard it.
4. **Most of the rest is mechanical:** package relocations and dependency alignment (Lucene, netty, Jackson).
5. **Existing data upgrades in place** from 1.3 to 2.19 (2.x reads 1.x indices). 3.x reads only indices created by 2.x,
   so data must pass through 2.19, or be reindexed, before Phase 6.
6. **Effort:** Phase 5 is roughly 5–7 agent-streams of work plus integration. The critical path is census → package
   relocations → type removal → integration → verification. The bridge runs in parallel and can start now.

## 2. Why YANG-DB is on 1.3.0
- The `dev-opensearch` branch had a half-finished bump: the server artifact at 2.5.0, the clients at 1.3.0. It didn't
  compile, because the client code extends `TransportClient`, and that class exists only in the 1.x line. We checked
  the 2.0.0–2.3.0 jars, and no `transport` client artifact is published for 2.x.
- We pinned everything to **1.3.0** (`opensearch.version`, `opensearch.client.version`) and kept `lucene.version=8.10.1`,
  the Lucene that 1.3 ships. Revival stabilization (Phases 0–4) happened on that baseline. The full build is
  **1,157 tests, 0 failures**, and the Docker e2e suite passes all checks.

## 3. Version decision
| Option | JDK | Lucene | Reads existing 1.x data | Remote-client story | Verdict |
|---|---|---|---|---|---|
| Stay on 1.3.0 | 11 | 8.10 | yes | `TransportClient` (removed upstream) | end-of-life; no security fixes |
| **2.19.7** | 11 (17 also supported) | 9.x | **yes, in place** | REST high-level client + our bridge | **Phase 5 target** |
| 3.10.0 | **21 required** | 10.x | no (2.x indices only) | REST high-level client still published; `opensearch-java` is the long-term client | **Phase 6**, after 2.19 |

Why two hops:
- each hop has one dominant risk: the API and types for 2.x, then the JDK and Lucene for 3.x
- users get a supported data path (1.3 → 2.19 → 3.x)
- 2.19 is a releasable stopping point on its own

## 4. Breaking changes and where they hit this codebase
### 4.1 Remote connections: `TransportClient` removed
| Construction site | Module | Change |
|---|---|---|
| `executor/opensearch/ClientProvider` | fuse-dv-core | use `ClientFactory.remote(...)` (the bridge) |
| `epb/plan/statistics/provider/ElasticClientProvider` | fuse-dv-epb | same |
| `stat/es/client/ClientProvider` | fuse-dv-stat | same |
| `client/SnifferFuseClient` | fuse-core | same (the sniffer behaviour maps onto the REST client's node sniffer) |
| `ElasticEmbeddedNode` (×2: fuse-test-framework, fuse-service) | test/embedded | embedded: `node.client()` (unchanged); reuse-a-running-cluster path (T1): the bridge |

Also: `TransportAddress` is imported in 6 main files, and the 7 `org.opensearch.client.transport` /
`org.opensearch.transport.client` imports disappear with the construction sites.

**Bridge design.** A new module, `fuse-opensearch-client`:
```java
public final class RestBridgeClient extends org.opensearch.client.support.AbstractClient {
    private final RestHighLevelClient rest;
    @Override
    protected <Req extends ActionRequest, Resp extends ActionResponse> void doExecute(
            ActionType<Resp> action, Req request, ActionListener<Resp> listener) {
        // dispatch on action: SearchAction -> rest.searchAsync(...), IndexAction -> rest.indexAsync(...), ...
        // unknown action -> listener.onFailure(new UnsupportedOperationException(action.name()))
    }
}
```
It needs to cover the action types this codebase uses, about 15 (counts are call sites in main code):

| Area | Actions |
|---|---|
| Reads | search (15), get (9), multi-get (2), multi-search (2), scroll (2) / clear-scroll (3), field caps (2), explain (2), term vectors (4) / multi term vectors (2) |
| Writes | index (13), bulk (8), delete (5), update (4) |
| Admin | indices create / delete, mappings, templates, refresh, cluster state |

Everything else fails loudly, never silently. The construction details (thread pool, `Settings`, listeners) are
*(confirm in census)*. `fuse-dv-core`'s existing `MockClient` is a useful reference for the interface's shape.

### 4.2 Mapping types removed
| Pattern | Files (main) | Modules |
|---|---|---|
| `prepareIndex(index, type …)` | 5 | dv-core, dv-stat |
| `setType(…)` / put-mapping with type | 7 | core, dv-core, dv-stat, model |
| `addMapping(…)` (type-keyed) | 6 | dv-core, service, test |
| `setTypes(…)` / `types(…)` on requests | 4 | core, dv-stat, asg |
| term vectors with a type argument | 3 | dv-core |
| `"_type"` field reference | 1 | dv-unipop |

**Conclusions:**
- **Templates:** Phase 3 showed that on 1.3 the generated templates still carry a type wrapper, and only real indices
  drop it. On 2.x the mapping factory must emit untyped templates.
- **Tests:** `OpensearchIndexProviderMappingFactoryIT` now asserts structure, not strings, so it adapts with small,
  reviewable edits. It's the main guard for this step.
- **Query code:** any `_type` filter or term becomes an index filter. This mostly happens already through the index
  partitions.

### 4.3 Package and API relocations *(confirm in census)*
During the 2.x line, core library classes moved into `org.opensearch.core.*`. Expected relocations:

| Class family | Files (main) |
|---|---|
| `org.opensearch.common.xcontent.*` → `org.opensearch.core.xcontent.*` (parts of it) | 9 |
| `org.opensearch.common.transport.TransportAddress` → `org.opensearch.core.common.transport.*` | 6 |
| `org.opensearch.common.Strings` / `bytes` | 1 each |

This is mechanical work, split across two module groups. Expect a long tail of small API renames (builder methods,
enum constants) that only the compiler census can list completely.

### 4.4 Dependencies
| Dependency | Today | Action |
|---|---|---|
| Lucene | pinned `8.10.1` in the root pom | remove the pin, or align to 2.19's Lucene 9 |
| netty | 4.1.73 pinned in fuse-service for Jooby (the A1 fix: a mismatch broke every HTTP request) | **highest-risk item**: one netty version must satisfy OpenSearch 2.19 and Jooby 1.x. Fallbacks: shade OpenSearch's netty, or run the service on Jooby's Jetty server |
| Jackson | `2.13.4.2` | align to the 2.19 client's requirement |
| log4j | `2.17.1` | align (keep it ≥ the patched line) |
| Docker | `docker/.env` `OS_VERSION=1.3.0`; official images | `OS_VERSION=2.19.7`. Keep the security plugin disabled for the local quickstart (`DISABLE_SECURITY_PLUGIN=true`); check the 2.12+ initial-admin-password rules for any setup that enables security |

### 4.5 Test harness
- Embedded node (`Node` + `Netty4Plugin`): the construction API and plugin classpath change in 2.x *(confirm in census)*.
- **Painless** must be on the test classpath: count-filter queries generate Painless scripts (C1).
- Keep the T1 guarantees: configurable ports and data dir, and setup failures fail the build. The
  reuse-a-running-cluster path switches from `PreBuiltTransportClient` to the bridge.

## 5. Data migration
| Path | When | Steps |
|---|---|---|
| **In-place upgrade 1.3 → 2.19** (recommended) | existing 1.3 deployments | 1. Snapshot (safety). 2. Stop YANG-DB. 3. Upgrade OpenSearch to 2.19.7 (restart / rolling). 4. Deploy the YANG-DB 2.19 build. 5. Re-apply index templates (now untyped) via `GET /fuse/load/ontology/{id}/init` (templates only affect new indices). 6. Smoke test with `docker/e2e/doc_queries.py` (no `--load`) |
| **Reindex** | if templates or mappings must change for existing indices | create new indices from the 2.x templates, then `_reindex` from old to new and swap aliases |
| **Reload** | demo / tutorial data | `python3 docker/e2e/doc_queries.py --load --reset`, or the playground's one-click load |
| **To 3.x later** | Phase 6 | indices must have been created on 2.x: upgrade to 2.19 first, then reindex any 1.x-created index before moving to 3.x |

Time-partitioned indices (`idx_fire_*`, …) upgrade like any others. Bucket routing (L4) is application-side and doesn't
change.

## 6. Verification and acceptance
1. `mvn clean test` green with the integration suites running; test count ≥ 1,157 (the Phase 4 baseline).
2. A 2.19 Docker image passes `docker/e2e/verify_and_mark.sh`: every tutorial query, the R1–R8 regressions, cursors,
   the playground, and the one-click load. The green `e2e/docker` status is on the merged commit.
3. Bridge integration tests run against a **remote** cluster, not only the embedded node.
4. The in-place upgrade is rehearsed: load the tutorial on 1.3, upgrade to 2.19, then queries return the same results.
5. A latency sanity check against 1.3 on the tutorial queries, with no large regressions.

## 7. Rollback
- **Code:** the migration lands as one PR. Reverting it restores the 1.3 build.
- **Data:** OpenSearch can't downgrade indices. Take a snapshot **before** the in-place upgrade; rollback means
  restoring that snapshot onto a 1.3 cluster.

## 8. Open questions for step 5.0 (the census)
- The exact list of relocated and removed APIs per module. It sets the boundaries of streams Q1 and Q2.
- Whether one netty version can serve both Jooby 1.x and OpenSearch 2.19 (the riskiest unknown).
- Whether the RHLC in 2.19 still offers async variants for every action the bridge needs (expected yes).
- The embedded `Node` construction and plugin loading on 2.19.

## 9. References
- [OPENSEARCH_UPGRADE.md](OPENSEARCH_UPGRADE.md): the execution plan
- [KNOWN_ISSUES.md](KNOWN_ISSUES.md): A1 (the netty pin), T1 (test harness), C1 (Painless count scripts), the mapping
  issues M1/M2
- `OpensearchIndexProviderMappingFactoryIT`: the structural mapping checks that guard §4.2
