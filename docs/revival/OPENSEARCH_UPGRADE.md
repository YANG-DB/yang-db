# Story: support a current OpenSearch (1.3.0 → 2.19.x, then 3.x)

> **Status (2026-11-23):** the 2.19 part (Phase 5) is done and released as
> [v0.9.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.9.0). This page is kept as the execution record. The
> 3.x part (Phase 6) hasn't started.

Background, conclusions and the breaking-change catalog: [OPENSEARCH_MIGRATION.md](OPENSEARCH_MIGRATION.md).

**Goal:** YANG-DB builds, passes its tests and runs the Docker quickstart on a current OpenSearch, with an upgrade path
for existing data. Tracked as **Phase 5** (2.x) and **Phase 6** (3.x) in [ROADMAP.md](ROADMAP.md).

## Why we are on 1.3.0, and what the upgrade has to solve
| # | Blocker | Evidence |
|---|---|---|
| 1 | **`TransportClient` is gone in 2.x.** Six main classes build `PreBuiltTransportClient`s for remote clusters; no 2.x class implements the `Client` interface remotely | `ClientProvider` (dv-core, dv-stat), `ElasticClientProvider` (epb), `SnifferFuseClient` (core), both `ElasticEmbeddedNode`s |
| 2 | **Mapping types are removed in 2.x.** Our index templates still carry a type wrapper on 1.3, and request builders pass types | Phase 3 stream F; `prepareIndex(index, type)`, `types()` |
| 3 | **Package relocations in 2.x** (xcontent, `ParseField`, `TransportAddress`, `Strings` moved to `org.opensearch.core…` by about 2.10) | 13+ `common.xcontent` imports, 7 `client.transport` imports |
| 4 | **Dependency alignment:** Lucene 8.10.1 is pinned in the root pom (2.x uses Lucene 9); netty is pinned to 4.1.73 for Jooby (2.x brings a newer netty); Jackson and log4j versions | root `pom.xml`, `fuse-service/pom.xml` (A1 netty pin) |
| 5 | **The embedded test node** (`Node` + `Netty4Plugin`) must start on 2.x, and Painless must be loadable for count-filter tests | `fuse-test-framework` |

Scale: **45** main files use the `Client` interface. The actions used are `prepareSearch` (15), `prepareIndex` (13),
`prepareGet` (9), `prepareBulk` (8), `prepareDelete` (5), `prepareUpdate`, term vectors, scroll and clear-scroll,
multi-search, multi-get, field caps, explain, and admin `create`/`delete`/`state`. That is about 15 action types.

**Strategy:** keep the `Client` interface everywhere. Build **one adapter** (`AbstractClient` → `RestHighLevelClient`) for
the remote case, so the ~40 files that only use `Client` don't change. Upgrade in two hops: **2.19.x first**, on JDK 11.
**3.x second**: it needs JDK 21 and Lucene 10, and it reads only indices created by 2.x.

---

## Phase 5 — OpenSearch 2.19.x

### Dependency graph
```
            ┌─────────────────────── 5.0 Spike: version bump + compile-error census (lead) ──────────────────────┐
            │                                         │                                                     │
            ▼                                         ▼                                                     ▼
 5.1 Remote-client bridge (agent P)    5.2 Package relocations (agents Q1, Q2)          5.4 Dependency alignment (agent S)
   (can start before 5.0: also              (mechanical, per module group)                (Lucene, netty/Jooby, Jackson,
    compiles on 1.3 RHLC)                            │                                      log4j, Docker images)
            │                                         ▼                                                     │
            │                          5.3 Mapping-type removal (agent R)                                    │
            │                            (templates, loaders, search/termvectors)                            │
            ▼                                         │                                                     │
 5.5 Test harness on 2.x (agent T) ◄──────────────────┴─────────────────────────────────────────────────────┘
   (embedded node, Painless, the reuse-cluster path uses the bridge)
            │
            ▼
 5.6 Integration (lead): merge in order 5.4 → 5.2 → 5.3 → 5.1 → 5.5; full build green
            │
            ▼
 5.7 Verification (lead + agent U): e2e on a 2.19 image, bridge IT against a remote cluster, data upgrade check
            │
            ▼
 5.8 Docs and release (lead): .env, PROJECT.md, wiki, upgrade guide, release notes → PR
```

### Sub-steps
| Step | Owner | Depends on | Work | Done when |
|---|---|---|---|---|
| **5.0 Spike** | lead | Phase 4 merged (avoids conflicts in fuse-service) | Throwaway branch: set `opensearch.version` and the client versions to 2.19.7, drop the Lucene pin, run `mvn compile` (online), collect **every compile error grouped by module and cause** (relocation / removed API / types / bridge site). Fix the stream boundaries below from the census | `docs/revival/os2-census.md` committed; streams adjusted |
| **5.1 Remote-client bridge** | agent P | none (start any time) | New module `fuse-opensearch-client`: `RestBridgeClient extends AbstractClient`, `doExecute()` delegating to `RestHighLevelClient` for the ~15 action types in use; unsupported actions throw clearly. Swap the 4 remote construction sites (dv-core, dv-stat, epb, `SnifferFuseClient`) behind a small `ClientFactory` | Unit tests per action; IT against a **remote** cluster (the Docker OpenSearch on a spare port), not the embedded node |
| **5.2 Package relocations** | agents Q1 (model, core, asg, service) + Q2 (dv-unipop, dv-core, dv-epb, dv-stat, gta) | 5.0 | Mechanical import and API-rename fixes from the census. No behaviour changes | Each module group compiles on 2.19 |
| **5.3 Mapping-type removal** | agent R | 5.2 (compiling modules) | Index-provider mapping factory and templates without a type wrapper; loaders' `prepareIndex`/bulk without types; search `types()`, term vectors, `_type` fields; the `OpensearchIndexProviderMappingFactoryIT` expectations from Phase 3 updated (they're structural, so the edits are small) | fuse-dv-core suite green on 2.19, including its ITs |
| **5.4 Dependency alignment** | agent S | 5.0 | Lucene to 2.19's version (or let it be managed); netty: one version for OpenSearch + Jooby (re-check the A1 pin and the `ByteBuf` test); Jackson and log4j; `docker/.env` `OS_VERSION=2.19.7`; compose env (security plugin disabled, 2.12+ admin-password rules) | `mvn dependency:tree` has no conflicting duplicates; compose stack starts healthy |
| **5.5 Test harness** | agent T | 5.2, 5.4 (5.1 for the reuse path) | `ElasticEmbeddedNode` on 2.x: `Node`/plugin construction, the Painless module on the test classpath, the reuse-running-cluster path via the bridge (T1 still holds), configurable ports and data dir kept | All modules' ITs run with non-zero counts on 2.19 |
| **5.6 Integration** | lead | 5.1–5.5 | Merge streams one at a time in the stated order; full `mvn clean test` after each; fix stragglers | **Full build green, 0 failures, test counts ≥ today's 1,157** |
| **5.7 Verification** | lead + agent U | 5.6 | 2.19 Docker image → `verify_and_mark.sh` (all e2e checks, including the R1–R8 regressions); bridge IT against the Docker cluster; **data upgrade**: load the tutorial on 1.3, upgrade the node to 2.19 in place, then queries pass (2.x reads 1.x indices); a quick latency sanity check vs 1.3 | Green `e2e/docker` status on the PR head; upgrade steps written down |
| **5.8 Docs and release** | lead | 5.7 | `PROJECT.md`, `docker/README.md`, tutorial prerequisites, wiki Home and Revival pages, `docs/revival/` upgrade guide, release notes; KNOWN_ISSUES for anything left | PR reviewed and merged |

### Parallel lanes and ports (agents keep handoff files per `agents/README.md`)
| Lane | Agents | Starts after | Test ports / data dir |
|---|---|---|---|
| Bridge | P | immediately | 9360/9460, `/tmp/yangdb-es-P` (plus a remote cluster on a spare port) |
| Compile | Q1, Q2, S | 5.0 | 9370/9470, 9380/9480, 9390/9490 |
| Semantics | R | Q1/Q2 merged | 9400/9500 |
| Harness | T | Q*, S merged | 9410/9510 |
| Verify | U | 5.6 | Docker stack (exclusive) |

**Critical path:** 5.0 → 5.2 → 5.3 → 5.6 → 5.7. The bridge (5.1) runs fully in parallel and only gates the reuse
path in 5.5 and the remote IT in 5.7.

**Risks:**
- Jooby 1.x and the netty version (A1 showed a mismatch kills every request): if one version can't serve both,
  shade OpenSearch's netty or move the service to Jooby's Jetty server.
- Behaviour differences on 2.x (scroll, aggregation defaults): the e2e regressions and the Phase 3 structural ITs
  are the safety net.

---

## Phase 6 — OpenSearch 3.x (after Phase 5 ships)
| Step | Depends on | Work |
|---|---|---|
| 6.0 JDK 21 | Phase 5 | **Done (2026-11-28)**: build, tests and engine on JDK 21, still on OpenSearch 2.19 and compiled for Java 11. Only two things broke: Mockito 3.12's byte-buddy couldn't read Java 21 classes (31 bridge tests; byte-buddy raised to 1.18.17), and the engine didn't start without `--add-opens` (J1; the start script passes them now). CI checks JDK 11 and 21; the Docker image runs on 21 |
| 6.1 Lucene 10 / 3.x API census | 6.0 | Same census method as 5.0, against 3.10.x |
| 6.2 Bridge on 3.x | 6.1 | The RHLC is **deprecated since 3.0** but still published (latest 3.10.0, checked 2026-11-23). Keep the bridge on it for 3.10, or move it onto `opensearch-java`; either way the PIT calls (6.5) move with it |
| 6.3 Data path | 6.1 | 3.x reads only 2.x-created indices: document "upgrade to 2.19 first, or reindex" |
| 6.4 Verify and release | 6.2, 6.3, 6.5 | Same gate as 5.7 and 5.8 |
| 6.5 Point in Time per cursor | done on 2.19 as ROADMAP 7.8 (opt-in); on 3.x check that sorting on `_id` still works (`indices.id_field_data.enabled`, deprecated) or use `_shard_doc` if OpenSearch adds it | One PIT per cursor with `search_after` paging instead of per-search scrolls, so a whole query reads one snapshot. PIT is unchanged in 3.x. Design and limits: [OPENSEARCH_MIGRATION.md §10](OPENSEARCH_MIGRATION.md#10-next-consistent-query-snapshots-with-point-in-time-planned) |

## Definition of done (both phases)
1. `mvn clean test` green with ITs running, test count not lower than before the phase.
2. The Docker quickstart on the new OpenSearch passes `docker/e2e/verify_and_mark.sh`. The green `e2e/docker` status
   is on the merged commit.
3. The bridge is verified against a remote cluster, not only the embedded node.
4. A documented, tested upgrade path for existing data.
