# YANG-DB Revival Roadmap

Living plan for bringing YANG-DB back to a maintained, promotable state. Update the checklists as work lands.
Bugs referenced by ID (e.g. `L1`, `Q2`) are described in [KNOWN_ISSUES.md](KNOWN_ISSUES.md).
Live progress and blockers of the running work: [STATUS.md](STATUS.md). Resume points per agent: [agents/](agents/README.md).

## Goals
1. Simpler to test-drive
2. Simpler to maintain
3. Smaller code footprint
4. Focused tests that don't depend on brittle static fixtures
5. Extensible by outside contributors

## Working principles
- **Modularize, don't rewrite.** Extract what doesn't belong in core, consolidate duplicates, formalize existing seams.
- **Stable before push.** A batch of work is pushed only when the definition of "stable" below holds.
- **Fix-on-sight is scoped.** Pre-existing breakage unrelated to the current task is flagged (`@Ignore` + comment, or an entry in KNOWN_ISSUES) rather than fixed inline.
- **Verify against stored data.** Query expectations are computed from what is actually in OpenSearch, not from source CSVs (the loader has corrupted data before).

## Definition of "stable"
1. `mvn clean test` passes **with the integration suites actually running** (non-zero test counts for the `*IT` classes — a suite that crashes in setup currently reports 0 tests and `BUILD SUCCESS`, see `T1`).
2. Every example query in the docs returns the correct result against the Docker quickstart stack loaded with the tutorial data (end-to-end doc-query suite, `S13`).

## Phase 0 — Quick wins ✅ (done 2026-11-20)
- [x] Onboarding aligned on OpenSearch 1.3.0; official images; healthcheck-gated startup
- [x] Legacy Elasticsearch 6.5.4 compose files and scripts removed; manual install guide marked legacy
- [x] Project README in [PROJECT.md](../../PROJECT.md) (root `README.MD` is the maintainer's GitHub profile page and must stay)
- [x] `dev-opensearch` renamed to `main` (default branch)
- [x] Quickstart verified end-to-end with Docker

## Workstream — Stabilize ✅ done 2026-11-20 (full build 1,123 tests green, e2e doc-query suite 21/21)
Loader
- [x] `L1` relation endpoint types rewritten — fixed in `CSVTransformer`, test `CSVTransformerTest#testRelationKeepsSourceTypeWhenPairsShareDestination`
- [x] `L2` pre-1970 epoch-millis dates dropped — fixed in `DataLoaderUtils`, test `DataLoaderUtilsTest`
- [x] `L3` `INSERT` silently overwrites — fixed in `LoadUtils` and both loaders, IT `IndexProviderBasedCSVLoaderIT#testInsertDoesNotOverwriteButUpsertDoes`
- [x] `L4` time-partition buckets — numeric range buckets (inclusive upper bounds) across loader, query and stats, out-of-range dates fail loudly (`TimeSeriesBuckets`)

Test infrastructure
- [x] `T1` integration suite crashed when an OpenSearch was already running locally, and the build reported false green

Query engine
- [x] `Q1` undirected pattern `-[r]-` executes as `->`
- [x] `Q2` relationship endpoint type ignored (horses returned as dragons)
- [x] `Q3` edge whose endpoints share an id is dropped
- [x] `Q4` relationship ids concatenated without a delimiter; per-pair collapse even without `distinct`
- [x] `Q5` (label union rejected explicitly; untyped match fixed) untyped `match (p)` returns nothing; label union `(p:A | B)` is a syntax error — fix or document as unsupported
- [x] `A1` API renders Java `toString()` unless `Accept: application/json`

Tutorial data & docs
- [x] `D1` tutorial CSV defects (header/row mismatches, header typo, duplicate edge ids)
- [x] `D2` doc examples and claims that don't match reality (label union → `WHERE … OR …`, count semantics, cyclic example replaced)
- [x] `D3` dangling ids in tutorial relationship data — count examples rewritten to use `Know`
- [x] `Q6` cycle-closing pattern → explicit "Cyclic patterns are not supported" error, documented
- [x] `Q7` count filter returned nothing (planner reversed the counted relation)
- [x] `Q8` `WHERE p:A OR p:B` on an untyped node crashed
- [x] `A2` whole-file load failure easy to misread (fixed in bug-fix round 2)

Verification
- [x] `S13` end-to-end doc-query suite committed (`docker/e2e/`) and **passing**: run 4 (2026-11-20), all 21 checks
      on a freshly rebuilt image. `verify_and_mark.sh` publishes the result as the GitHub commit status `e2e/docker`.
- [x] `S14` full `mvn clean test` green with ITs running (1,120 tests, 0 failures) → batch pushed 2026-11-20 at
      the maintainer's request, before e2e run 3

### Parallel execution plan for the Stabilize workstream
The work splits into streams that touch different modules, so it can run as several agents, each in its own git
worktree. It can't be fully parallel: everyone needs a trustworthy test signal first, the embedded test node has fixed
ports, and the traversal-identity bugs share code and must stay together.

**Stage 0 — foundation (serial, lead)** ✅ done 2026-11-20 — full `mvn clean test`: 1,064 tests, 0 failures, ITs running
- [x] `T1` fix the reuse-running-cluster crash and make suite setup failures fail the build
- [x] Make the embedded test node's ports and data directory configurable (`fuse.test.http.port`, `fuse.test.transport.port`, `fuse.test.data.dir`; defaults 9200 / 9300 / `target/es`)
- [x] Run `L3`'s IT, commit `L3`

**Stage 1 — parallel streams (one agent per stream, separate worktrees)** ✅ done 2026-11-20 (streams A–E; E added for L4)

| Stream | Scope | Modules | Test ports | Notes |
|---|---|---|---|---|
| A — Cypher translator | `Q1` undirected, `Q5` untyped node / label union | `fuse-asg` | none (unit tests) | Decides whether `Q5` is fixed or documented as unsupported; reports the decision for stream D |
| B — Traversal identity | `Q2` endpoint type, `Q3` same-id drop, `Q4` relationship ids | `fuse-dv-unipop`, `fuse-dv-gta`, `fuse-dv-core` cursors | 9210 / 9310 | Critical path: largest and least certain; likely one shared root cause (identity by id only) |
| C — Service & loader | `A1` JSON-by-default rendering, `L4` partition buckets | `fuse-service`, `fuse-dv-core` load | 9220 / 9320 | Starts after `L3` is committed (shares `LoadUtils`) |
| D — Data, docs & e2e | `D1` CSV fixes, `S13` e2e suite in repo, engine-independent parts of `D2` | `docs/`, `docker/` | Docker stack (exclusive) | Queries.md updates wait for stream A's `Q5` decision |

**Stage 2 — integration (serial, lead)**
- [x] Merge streams one at a time (rebase on `main`, full `mvn clean test` with ITs running after each)
- [x] Rebuild the Docker image, run the e2e suite (`S13`) against freshly loaded tutorial data (run 4: all pass)
- [x] Finish engine-dependent `D2` docs, tick this checklist, push the batch (`S14`)

**Rules for agents**
- Work only in your own worktree and modules; don't edit ROADMAP.md / KNOWN_ISSUES.md (the lead updates them from reports).
- Every fix starts with a test that fails for the right reason; one commit per issue ID.
- Run module tests offline without installing (`mvn -o test -pl <module> -am`) so the shared `~/.m2` isn't written concurrently; pass your assigned test ports and a private data dir, e.g.
  `-Dfuse.test.http.port=9210 -Dfuse.test.transport.port=9310 -Dfuse.test.data.dir=/tmp/yangdb-es-B`.
- Never run ITs on the default ports while the Docker stack is up: the suite then *reuses* that cluster and resets its indices, wiping the demo data.
- `fuse-dv-stat` tests read port 9300 from config files, so they only run in Stage 2 (default ports, Docker stopped).
- "Done" = module tests green **with non-zero IT counts**, plus a short report: what changed, root cause, anything found out of scope.

**Expected gain:** stream B sets the pace; running A, C and D alongside it saves roughly their combined duration
(a large share of the workstream's wall-clock time), at the cost of several agents' worth of tokens and a serial
merge step. Beyond these four streams, more agents would mostly add merge conflicts.

## Phase 1 — Shrink the footprint ✅ done 2026-11-20 (PR #160, merged)
- [x] `fuse-domain` was already outside the default build (only `-P dragons-assembly` built it). Restructured, as
      the maintainer chose (in-repo examples, retire sys-tests):
  - `fuse-domain-dragons-assembly` → `distribution/` (it builds the quickstart image; artifactId unchanged)
  - `fuse-domain-dragons-ext` and `-datagen` → `examples/dragons/{ext,datagen}` (new aggregator `examples/pom.xml`,
    profile `examples`; artifactIds unchanged so IDE run configs keep working)
  - `fuse-domain-dragons-test` (`dragons-sys-test`, 19.4K LOC, 322/418 failing, not in CI) **retired**: deleted, kept in
    git history; the `docker/e2e` suite covers the same scenario and passes

### Follow-up — Dragons playground ✅ done 2026-11-20 (PR #161, merged)
- [x] `/dragons/swagger`: Swagger UI 5 page with every tutorial query as a named example, plus a one-click
      `POST /fuse/dragons/tutorial/load`. Its spec is the e2e suite's query list.
- [x] Tutorial main page `docs/tutorial/sample/dragons/README.md` (Docker setup through queries); the wiki links it

## Phase 2 — One source of truth for test fixtures ✅ done 2026-11-20 (PR #162, merged)
Phase 1 had already removed most of the copies this phase targeted (they lived in the retired sys-test module).
Scope chosen by the maintainer:
- [x] Remove dead fixtures. `fuse-test-framework/src/test/resources` (`dragons/`, `dragons_dual/`, `IndexDocs`,
      `schema`, `dragon_mapping.json`, 10 MB) can never be read: the module has no tests and publishes no test-jar.
      Also `dragons_mock.csv` (shipped in fuse-dv-stat's jar, unused) and a dead staging config. −52K lines.
- [x] fuse-dv-core's tests use the **shipped** `distribution/.../Dragons.json` through a Maven `testResource`
      (same classpath path, `schema/Dragons.json`) instead of keeping an identical copy.
- Deliberately kept:
  - the older lowercase Dragons test ontology (fuse-asg `Dragons_Ontology.json`, fuse-service `ontology/Dragons.json`),
    which 18 Cypher tests are written against. Migrating it fits Phase 3.
  - fuse-asg's generic `mapping/Dragons.json`
  - fuse-model's golden expected-output JSONs
  - fuse-dv-core's deliberate index-provider variants (Embedded / Nested / Single)

## Phase 3 — Structural assertions instead of golden JSON strings ✅ done 2026-11-21 (PR #163; bug fixes #164/#165)
Survey (2026-11-20): 95 `@Ignore`s. 9 date from the OpenSearch migration; ~85 are legacy (2017–2021), 70 of them
bare with no reason. The long JSON-literal assertions are concentrated in one IT (58) and a few unit tests.
Parallel streams, one agent and worktree each:
- [x] **F** (done: 5 tests re-enabled, all JSON literals replaced by structural checks derived from the ontology and configs, self-cleaning; 2 product bugs pinned as M1/M2) `OpensearchIndexProviderMappingFactoryIT` (fuse-dv-core): rewrite the 4 stale golden-JSON tests as
      structural assertions against real cluster output, then re-enable them
- [x] **G** fuse-dv-epb: 4 tests re-enabled. Both causes were test bugs: Mockito stubs returned `int` for a `long`, and a wrong `notInSet` histogram expectation (510 is correct). Suite: 433, 0 failures
- [x] **H** (done: `QueryDslAssert` helper in unipop and `JsonTreeAssert` in model; 5 and 3 deliberate breaks each caught; counts unchanged) brittle passing golden-string tests → structural assertions: `ConstraintSearchAppenderTest`,
      `Count/DistinctFilterQueryTranslatorTest` (fuse-dv-unipop) and `QueryTest` (fuse-model)
- [x] **I** triage report: [IGNORED_TESTS.md](IGNORED_TESTS.md). Of 86: 21 obsolete, 8 fixable, 52 keep-disabled, 5 unknown; 4 product bugs filed (Q9–Q11, P2). Actions await the maintainer
- Later (maintainer call): migrate the older lowercase Dragons test ontology (fuse-asg; deferred from Phase 2)

## Phase 4 — A real extension point ✅ done 2026-11-22 (PR #167, merged)
Maintainer's choices: extract **GraphQL**, into a top-level **`extensions/`** folder (default build, bundled in the distribution).
- [x] **A** (done: `fuse-extension-api`, ServiceLoader discovery, MapBinder languages, generic routes, A3 fixed) `fuse-extension-api`: a `YangDbExtension` SPI discovered by `ServiceLoader` (Guice modules and route
      registrars); languages in a `MapBinder<String, QueryTransformer>` instead of the hard-coded switch; generic
      `/fuse/query/{lang}` routes with today's URLs unchanged; fixes A3 (Dragons routes); a hello-world test extension
- [x] **Baseline**: GraphQL turned out not to work end to end through REST (G1–G3 in KNOWN_ISSUES); only its
      in-process unit tests pass. The maintainer chose to extract it **and make it work**.
- [x] **B** (done: `extensions/graphql` ships in the distribution; graphql-java out of core; G1–G3 fixed; GraphQL e2e GQ1–GQ3 pass live) move GraphQL (query translator, schema translation + routes, GraphQL cursor) to `extensions/graphql`,
      plugged in through the SPI. Fix G1 (a `Query` root generated for JSON-registered ontologies such as Dragons), G2
      (stateless schema import) and G3 (multi-document schemas). Add Dragons GraphQL queries to the e2e suite and the
      playground. Drop `graphql-java` from core if possible; the existing GraphQL unit tests move along and must
      still pass
      *(agent N: implemented, unit/REST tests green; e2e GQ1–GQ3 pending the lead's Docker run. graphql-java left
      core entirely.)*
- [x] A4: the Dragons clause routes are broken once registered (fixed in bug-fix round 2: the dead routes are removed)

## Phase 5 — Support a current OpenSearch: 2.19.x ✅ (merged 2026-11-23, PR #169; released as **[v0.9.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.9.0)**)
Conclusions, breaking-change catalog, data migration and rollback: **[OPENSEARCH_MIGRATION.md](OPENSEARCH_MIGRATION.md)**.
Execution plan with sub-steps, dependency graph, agent lanes and ports: **[OPENSEARCH_UPGRADE.md](OPENSEARCH_UPGRADE.md)**.
Starts after Phase 4 merges; the remote-client bridge (5.1) can start any time.
- [x] 5.0 (done: `os2-census.md`, 207 errors; new blocker: ANTLR 4.13.1 for Painless vs 4.7.2 for graphql-java) Spike: bump to 2.19.7 on a throwaway branch and census every compile error by module and cause (lead)
- [x] 5.1 (done) Remote-client bridge `AbstractClient` → `RestHighLevelClient`, swapped into the 4 remote construction sites (agent P)
- [x] 5.2 (done) Package relocations + the Client wrapper classes
- [x] 5.3 (done) Mapping-type removal: templates, loaders, search and term vectors (agent R)
- [x] 5.4 (done) Dependency alignment, including upgrading graphql-java for ANTLR 4.13: Lucene, netty/Jooby, Jackson, log4j, Docker images (agent S)
- [x] 5.5 (done: the fuse-service embedded node uses the bridge; Painless runs on ANTLR 4.13.1) Test harness on 2.x: embedded node, Painless, the reuse-cluster path through the bridge (agent T)
- [x] 5.6 Integration: full build green on 2.19.7, **1,249 tests, 0 failures** (lead)
- [x] 5.7 (Docker e2e **34/34 on OpenSearch 2.19.7**; **in-place upgrade rehearsed 2026-11-23**: data loaded with v0.8.0 on 1.3.0, cluster upgraded to 2.19.7 on the same volume, v0.9.0 returned identical results for all 28 queries plus the cursor checks, with no reload; see OPENSEARCH_MIGRATION.md §9) Verification: e2e on a 2.19 image, bridge IT against a remote cluster, 1.3 → 2.19 data upgrade (lead + agent U)
- [x] 5.8 (docs, upgrade guide and [release notes](../releases/v0.9.0.md) done; wiki updated 2026-11-23: Home + Revival 2026) Docs, wiki, upgrade guide, release notes → PR (lead)
- [x] 5.9 (done 2026-11-23) Release the OpenSearch 1.3 line before the switch: **[v0.8.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.8.0)**
  from branch `release/0.8.x` (cut from main `a24dd19fd`), notes in [docs/releases/v0.8.0.md](../releases/v0.8.0.md),
  distribution attached by the Release workflow (`.github/workflows/release.yml`), label `opensearch-1.3` for 1.3-line
  fixes. Phase 5 shipped as v0.9.0 from `release/0.9.x` (label `opensearch-2.19`); main moves on to `0.10.0-SNAPSHOT`.

## Bug-fix round 2 ✅ (2026-11-23; released as **[v0.9.1](https://github.com/YANG-DB/yang-db/releases/tag/v0.9.1)** on 2026-11-24)
Every open issue in [KNOWN_ISSUES.md](KNOWN_ISSUES.md) fixed, each with a test that failed first: A2, A4–A10, C3, G4,
M1, M2, P2, Q11, Q13, plus T3 (a test suite that wiped a running OpenSearch). Four agents in parallel; handoff files
`agents/bugfix2-*.md`.

## Phase 6 — OpenSearch 3.x (JDK 21, Lucene 10)
- [ ] JDK 21 for build and runtime; then the 3.x census, bridge, data path ("upgrade to 2.19 first") and verification,
      per [OPENSEARCH_UPGRADE.md](OPENSEARCH_UPGRADE.md#phase-6--opensearch-3x-after-phase-5-ships)
- [ ] **Point in Time per cursor**: every search of a query, on every page, reads the same snapshot of the index
      (PIT + `search_after` instead of per-search scrolls). Can land on 2.19 before the 3.x move (PIT exists since 2.4),
      and carries over to 3.x unchanged. Design, limits and 3.x compatibility:
      [OPENSEARCH_MIGRATION.md §10](OPENSEARCH_MIGRATION.md#10-next-consistent-query-snapshots-with-point-in-time-planned)

## Phase 7 — Speed and efficiency
Goal: fewer round trips to OpenSearch, less data per round trip and less planning overhead per query, each change
shown by numbers rather than assumed. Every item must leave the e2e doc-query results unchanged
(`docker/e2e/doc_queries.py`): these change *how* the engine calls OpenSearch, never *what* a query returns.

What the code does today (checked 2026-11-23; corrected 2026-11-26 from the requests OpenSearch actually received,
captured with the search slow log for all 31 playground queries: check claims about requests that way, not by
searching the code):
- Every search was a scroll ([SearchHitScrollIterable](../../fuse-dv/fuse-dv-unipop/src/main/java/com/yangdb/fuse/unipop/converter/SearchHitScrollIterable.java)),
  even one returning a handful of hits: 253 of 253 searches asked for `size: 10000` with a scroll, and each needed an
  extra, empty scroll request to see the end before the clear. Changed in 7.3.
- No multi-search: `msearch` appears only in the client wrappers, never in query execution, so each search of a
  traversal step is its own HTTP round trip.
- Nothing runs concurrently: an `OR` query splits into branches (see C3) that run one after another.
- `_source` **is** filtered (an earlier version of this list said it wasn't; that came from a code search for
  `fetchSource`, which missed `setFetchSource`). Every search sends an explicit `_source.includes` list: the element's
  ontology properties (`DefaultSelectionAsgStrategy` adds them all to the projection) plus the edge fields joins need;
  a property an edge already carries as a redundant field is left out of the far-end search, and GraphQL asks for
  exactly the selected fields. With no includes, `SearchBuilder` sends `_source: false`.
- Cypher `RETURN` selects nothing: the engine returns the whole matched pattern, every element with all its
  properties, whatever the `RETURN` lists (`RETURN d.name` still fetches and returns every field of `d`, and every
  other element of the path). 30 of the 31 documented queries use `RETURN *`. See Q15.
- The planner's cost estimates are live count queries, cached locally for 10 minutes and never using OpenSearch's
  request cache; the statistics module (`fuse-dv-stat`) exists but isn't the default cost source.

Items, in the recommended order:
- [x] 7.1 **Benchmark harness** ✅ (2026-11-24): `docker/bench/bench.py` measures p50/p95 latency and OpenSearch ops
      per query for the 31 playground queries, at scale 1 (tutorial data) or N (`scale_data.py`: N disjoint copies;
      generated-data CSVs from `examples/dragons/datagen` don't match the loader's format). CI runs it after e2e and
      fails on more than 10% more OpenSearch work than `docker/bench/baseline.json`. Results and the comparison
      v0.8.0 (OpenSearch 1.3) / v0.9.0 / v0.9.1 / main: [docs/benchmark.md](../benchmark.md). Found L5, Q14, A11.
- [x] 7.2 **Fetch only the projected fields**: already in place (see above; found 2026-11-26). The remaining gap is
      that Cypher `RETURN` doesn't project (Q15); making it project changes what the API returns, so it is a feature
      of its own (7.10), not a speed item, and the benchmark (all `RETURN *`) can't show a gain from it.
- [x] 7.3 **Plain search for small results** (2026-11-26): each search starts as a plain search; a page that isn't
      full is the whole result, so no scroll is opened, no empty follow-up is sent and nothing is cleared. Only after a
      full page does the same search continue as a scroll (hits already returned are skipped), and an opened scroll
      is cleared when exhausted or when the limit stops it (it used to stay open until its 10-minute timeout).
      OpenSearch's exact hit count also ends a full page that is the whole result, and the scroll leaves out the
      documents already returned. Measured: **−50% OpenSearch operations at scale 1, −43% at scale 10**, the same rows,
      latency within noise (Q9 at scale 10 +9%). Found and fixed on the way: A11 (clock steps) and S1 (start-up hang).
      Results: [docs/benchmark.md](../benchmark.md#phase-73-plain-search-for-small-results).
- [ ] 7.4 **Batch each step's searches with `msearch`**: one round trip per hop instead of one per search; probably the
      largest single gain on multi-hop queries. *(medium)*
- [ ] 7.5 **Run independent work concurrently**: `OR` branches and independent searches in parallel (virtual threads
      once Phase 6 moves to JDK 21), so latency is the slowest branch, not the sum. *(medium)*
- [ ] 7.6 **Statistics-based cost estimates** by default, with live counts as the fallback using
      `request_cache=true`; cuts planning time, which is pure overhead on small queries. *(medium)*
- [ ] 7.7 **Stream results and evict idle cursors**: lower memory and a faster first page on large results. Builds on
      Phase 6's Point in Time per cursor, which also replaces the per-search scroll contexts. *(medium)*
- [ ] 7.8 **Route edge lookups to one shard** by source vertex id (`SearchBuilder` already supports routing): a hop
      touches one shard instead of all. Needs a reindex, so it comes last, and only if 7.1 shows hops dominate. *(large)*
- [ ] 7.10 **Cypher `RETURN` selects elements and properties** (Q15): return only the listed elements, and for
      `RETURN d.name` only those properties, fetching only them. A behaviour change of the API (fewer properties or
      elements per row), with its own tests and release note. *(medium)*
- [ ] 7.9 **Try concurrent segment search** (an OpenSearch cluster setting) on the benchmark; helps large single
      searches at no code cost.

Sequencing: 7.1 first. 7.2 and 7.3 are small and low-risk and can land before Phase 6; 7.4–7.6 are the big wins;
7.7 follows the PIT work; 7.8 only on evidence.

## Workstream — V1 language specification
Publish V1 as a spec other tools can build on ([docs/spec/v1](../spec/v1/README.md)).
- [x] JSON Schema of the wire format for the core types, tested against the repo's fixtures, the docs, the Swagger
      examples and the engine's serializer output (`V1QuerySchemaTest`)
- [x] Rules beyond the schema, each with an example and the engine's answer, checked in CI (`rules.md`, `V1RulesTest`)
- [x] Serve the schema: `GET /fuse/query/v1/schema` (packaged from docs/spec/v1 into fuse-model)
- [x] `RelUntyped` and `WhereByConstraint` in the core; `HQuant` deprecated. Found Q16–Q20 on the way
- [x] Text form, print only: `V1TextPrinter`, `POST /fuse/query/v1/print`
- [x] API reference: Swagger 2.0 file → OpenAPI 3.1 (`openapi.json`, every served route), Swagger UI 5 at
      `/swagger` instead of jooby-apitool, V1 bodies `$ref` the served schema
- [ ] Fix Q16 (OR between property filters), Q17–Q20 (engine)
- [ ] ANTLR grammar for the text form, only if writing V1 by hand becomes a need (the printer's notation is the
      starting point)

## Phase 8 — Ongoing hygiene
- [ ] Clear the Dependabot backlog (the OpenSearch bump itself is Phases 5–6)
- [x] Resolve or retire the `dragons-sys-test` profile failures: retired in Phase 1
- [ ] Prune stale remote branches

## Dev environment notes
- JDK 11 + Maven; Docker via colima on macOS. Quickstart: [docker/README.md](../../docker/README.md).
- Full tutorial load sequence: `GET /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId` → `POST /fuse/catalog/ontology` (Dragons.json) → `GET /fuse/load/ontology/Dragons/init` → `POST /fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex|edge&label=<Type>` per CSV.
- With the Docker stack up, run tests on other ports (see the agent rules above) or stop it first (`docker compose -f docker/docker-compose-os.yml stop`, data volumes are kept).
- On external (non-APFS) drives macOS writes `._*` sidecar files that break git and Maven; delete them before building and set `git config core.fileMode false`. They also block OpenSearch from deleting index folders, so keep the test node's data off such drives with `-Dfuse.test.data.dir=/tmp/...`.
