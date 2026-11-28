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
- [ ] **Point in Time per cursor**: moved to Phase 7 as **7.8** (it runs on 2.19 already; 3.x keeps the same API).

## Phase 7 — Speed and efficiency (7.1–7.8 and the first round of 7.12 released in **[v0.10.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.10.0)**, 2026-11-28)
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
      of its own (7.11), not a speed item, and the benchmark (all `RETURN *`) can't show a gain from it.
- [x] 7.3 **Plain search for small results** (2026-11-26): each search starts as a plain search; a page that isn't
      full is the whole result, so no scroll is opened, no empty follow-up is sent and nothing is cleared. Only after a
      full page does the same search continue as a scroll (hits already returned are skipped), and an opened scroll
      is cleared when exhausted or when the limit stops it (it used to stay open until its 10-minute timeout).
      OpenSearch's exact hit count also ends a full page that is the whole result, and the scroll leaves out the
      documents already returned. Measured: **−50% OpenSearch operations at scale 1, −43% at scale 10**, the same rows,
      latency within noise (Q9 at scale 10 +9%). Found and fixed on the way: A11 (clock steps) and S1 (start-up hang).
      Results: [docs/benchmark.md](../benchmark.md#phase-73-plain-search-for-small-results).
- [x] 7.4 **Fewer searches per step** (2026-11-26). The plan was one `msearch` per step; the slow log showed that
      every step already sends one search per bulk (ids in one `terms` query, partitions in one index list) and that
      steps depend on each other, so `msearch` has little to combine (about 6% of round trips: the planner's paired
      counts and two far-end searches of a multi-target relationship). The searches multiply with the bulk size
      (1,000 ids per search). Done instead: **growing bulks**, 1,000 doubling to 10,000 per step execution
      (`unipop.bulk.start` / `.multiplier` / `.max`; `start` and `multiplier` were in the config but unused).
      Measured at scale 10: **−33% OpenSearch operations for whole results, −28% and −6% latency for a 100-row first
      page**, same rows; a fixed 10,000 would have made first pages up to 5× slower.
      [docs/benchmark.md](../benchmark.md#phase-74-growing-bulks). The remaining `msearch` pairs could join 7.5.
- [~] 7.5 **Run independent work concurrently**: measured 2026-11-27, **deferred to Phase 6** (JDK 21 virtual
      threads). Comma patterns (Q6, QF, QG) are planned as one chain, each step needing the previous step's ids, so
      they have no parallel branches; the only independent work is `OR` branches (R10), which run back to back and
      cost the sum of the branches (38.6 ms = 20.5 + 19.0 at scale 1). Parallel branches would save at most the
      shorter branch: about 1% of the benchmark's latency, for a thread pool, cancellation and buffering.
- [~] 7.6 **Statistics-based cost estimates**: measured 2026-11-27, **not worth it**. Planning (parse, rewrite,
      plan) takes about 3–4 ms per query warm, 9% of the run including the HTTP round trip; with a cold count cache it
      costs at most about 2 ms more (a single-entity query with a new filter value; within noise with a
      relationship). Switching to statistics could change the plans chosen, for a gain of about 2 ms.
- [x] 7.7 **Evict idle queries** (2026-11-28; streaming part: see below). Measured first: a stored query holds
      its fetched pages (Q9 at scale 10: about 54 MB of heap), and the ttl was never enforced, so every query a client
      didn't delete stayed for good (Q14 was described wrongly as a 5-minute expiry). Now a query is removed after its
      ttl without use (default 5 minutes, checked every 30 s; a slowly paging client keeps it), and the store is
      thread-safe. Streaming: pages are already computed lazily (a 100-row first page of Q9 at scale 10 takes 0.22 s,
      the whole result 2.6 s, from 7.4), so what remains is the cursor's Point in Time (7.8).

- [x] 7.8 **Point in Time per cursor, opt-in** (2026-11-28). Measured first: a PIT holds one context per shard of
      its indices (one over all 19 graph indices: 57), and OpenSearch refused the 7th concurrent one
      (`search.max_open_pit_context`, 300 per node), so "one PIT per cursor for every query" would have failed the 7th
      query within 5 minutes. Built instead, with both modes kept: `opensearch.paging.mode = scroll` (default:
      unchanged) or `pit`, and a cursor request's `"consistent": true|false` overrides it. A consistent cursor opens
      one PIT on the indices its plan reads (lenient about indices that don't exist), every search of the cursor runs
      against it (no indices named, an `_index` filter instead), pages with `search_after` on `_doc, _index, _id`
      (OpenSearch 2.19 has no `_shard_doc`), and the PIT is released when the cursor is exhausted, deleted or expires.
      Verified: `PointInTimeConsistencyIT` (a write between two searches of a consistent cursor isn't seen; without a
      PIT it is), e2e ALL PASS in both modes, 0 PIT contexts left. Scale 10: default responses byte-for-byte identical
      to main; PIT mode −10% OpenSearch operations, latency within noise
      ([docs/benchmark.md](../benchmark.md#phase-78-point-in-time-opt-in)). Phase 6 note: the `_id` sort needs
      `indices.id_field_data.enabled` (on in 2.19, deprecated).
- [ ] 7.9 **Route edge lookups to one shard** by source vertex id (`SearchBuilder` already supports routing): a hop
      touches one shard instead of all. Needs a reindex, so it comes last, and only if 7.1 shows hops dominate. *(large)*
- [ ] 7.10 **Try concurrent segment search** (an OpenSearch cluster setting) on the benchmark; helps large single
      searches at no code cost.
- [ ] 7.11 **Cypher `RETURN` selects elements and properties** (Q15): return only the listed elements, and for
      `RETURN d.name` only those properties, fetching only them. A behaviour change of the API (fewer properties or
      elements per row), with its own tests and release note. *(medium)*
- [~] 7.12 **Profile the engine side** (measured 2026-11-27): OpenSearch is only 2–15% of a query's time on the
      tutorial data (Q9: 41 of 272 ms; Q5: 15 of 146 ms; Q2a: 1 of 60 ms); the rest is the engine's traversal and
      row assembly (Q9 ~170 ms) and JSON rendering (Q9 41 ms, 2.7 MB, about 900 bytes per relationship). Phase 7
      so far cut OpenSearch's work by a third to a half (7.3, 7.4); latency is in the engine. Next: profile Q9 and Q5
      at scale 10 with Java Flight Recorder, then fix the hot spots, measured with the benchmark.
      **First round done (2026-11-28):** the top cost was `String.format` in per-row text (`Assignment.getId` hashes
      the row's `toString`; `distinct` keys rows with `AssignmentDescriptor.print`), 15–20% of the CPU; replaced
      by concatenation of the same text. Responses byte-for-byte identical (ids included); **-12%
      latency at scale 10** (Q9 −10%, QG −19%, QA −21%). The profile is flat now; next candidates are small
      (string hashing in `UniGraphVertexStep`, OpenSearch JSON parsing).

Sequencing: 7.1 first. 7.2 and 7.3 are small and low-risk and can land before Phase 6; 7.4–7.6 are the big wins;
7.9 only on evidence.

## Workstream — V1 language specification ✅ done 2026-11-26 (PRs #196, #198; follow-ups below)
Publish V1 as a spec other tools can build on ([docs/spec/v1](../spec/v1/README.md)).
- [x] JSON Schema of the wire format for the core types, tested against the repo's fixtures, the docs, the Swagger
      examples and the engine's serializer output (`V1QuerySchemaTest`) (#196)
- [x] Rules beyond the schema, each with an example and the engine's answer, checked in CI (`rules.md`, `V1RulesTest`)
- [x] Serve the schema: `GET /fuse/query/v1/schema` (packaged from docs/spec/v1 into fuse-model)
- [x] `RelUntyped` and `WhereByConstraint` in the core; `HQuant` deprecated. Found Q16–Q20 on the way
- [x] Text form, print only: `V1TextPrinter`, `POST /fuse/query/v1/print`
- [x] API reference: Swagger 2.0 file → OpenAPI 3.1 (`openapi.json`, every served route), Swagger UI 5 at
      `/swagger` instead of jooby-apitool, V1 bodies `$ref` the served schema (rules page to here: #198)

Follow-ups (open):
- [ ] Q16: `some` (OR) between property filters behaves as `all`; the biggest of the five, it gives wrong answers
      silently. Workaround documented in rules.md
- [ ] Q17–Q20: where-by without `joinType`, `/validate` checking less than `/run`, `RelUntyped` without `vTypes`,
      cycles crashing. When one is fixed, update its example's marker in rules.md (`V1RulesTest` will fail until then)
      and relax the schema where it guards against it (Q17, Q19)
- [ ] Specify more extension types as they get used (`CountComp`, `InnerQueryConstraint`, ...)
- [ ] ANTLR grammar for the text form, only if writing V1 by hand becomes a need (the printer's notation is the
      starting point)

## Phase 8 — Ongoing hygiene
- [x] CI Maven cache falls back to the latest cache when a `pom.xml` changes (#199). Before, a changed POM meant
      ~600 MB from Maven Central in all three jobs at once, and HTTP 429 failures (#196's JDK 11 job)
- [ ] Clear the Dependabot backlog (the OpenSearch bump itself is Phases 5–6)
- [x] Resolve or retire the `dragons-sys-test` profile failures: retired in Phase 1
- [x] Prune stale remote branches (2026-11-26): 79 deleted. That's every branch already merged into `main`, the six
      2023–24 Dependabot branches, `upgrade-rehearsal`, `planning-opensearch-upgrade`, `release/0.1`–`0.4` and
      `master`. The last seven are kept as tags `v0.1`, `v0.2`, `v0.2.1`, `v0.3`, `v0.3.1`, `v0.4` and
      `archive/master-2019`.
- [ ] Review the 2017–2022 branches with work that isn't on `main` (keep, tag or delete): `develop`, `sql_support`,
      `develop-es7_4_2-support`, `pgql-lang`, `query-resource-monitor`, `materialize`, `common-tasks`, `graphql`,
      `gremlin-server`, `elastic-7.3.0`, `version_0.5_release`, `Jan-2020-RC2-fix_April_2020`, `metrics-report`,
      `newStructure`, and the `feature/*` branches. Recent release branches (`release/0.8.x`, `release/0.9.x`,
      `release-0.9.1`) stay

## Phase 9 — Future ideas (from a comparison with JanusGraph, 2026-11-28)
Candidates for versions after the revival, not commitments: each needs the maintainer's go-ahead and a design note
before work starts. IDs `F1`–`F20` are for referring to them.

**How the two differ.** JanusGraph can't run on OpenSearch alone: it keeps the graph in a separate store (Cassandra,
ScyllaDB, HBase, BerkeleyDB) and uses OpenSearch only as an optional *mixed index* for full-text, geo and range
lookups (OpenSearch 2 and 3 through its Elasticsearch backend since 1.2.0). YANG-DB keeps everything in OpenSearch and
maps an ontology onto existing indices, so the data never moves. That is the reason YANG-DB exists, and none of the
ideas below should give it up.

| Area | JanusGraph | YANG-DB |
|---|---|---|
| Storage | Lists of each vertex's edges in a wide-column store; OpenSearch only for indexes | Documents in OpenSearch; joins done with `terms` lookups |
| Using existing data | No: data must be loaded into JanusGraph | Yes: the ontology maps onto existing indices |
| Query languages | Gremlin (full TinkerPop), reads and writes | Cypher, GraphQL, SPARQL, V1; read-only |
| Planning | Step by step, guided by indexes | Cost-based, using index statistics |
| Writes | Transactions (locking depends on the store) | Bulk CSV/JSON load API only |
| Indexes | Composite, mixed, **vertex-centric** (edges per vertex, for vertices with huge numbers of edges) | OpenSearch mappings; redundant fields on edges |
| Analytics | Spark graph computer, vertex programs (PageRank, connected components) | None |
| Path queries | `repeat()`, `shortestPath()` | Fixed-length patterns; cycles rejected (Q6, Q20) |
| Clients | Gremlin Server; drivers for Python, JS, .NET, Go; many visualizers | REST + OpenAPI, Java client |
| Security | Authentication on Gremlin Server | None; can't send OpenSearch credentials |
| Element TTL | Per vertex and edge label | None (only stored queries expire) |
| Profiling | `profile()` step, Dropwizard metrics | Benchmark harness; nothing per query in the API |

**Tier 1: high value, fits the design**
- [ ] F1 **Authentication, and OpenSearch credentials**: basic auth, API keys or JWT on the REST API; basic auth and
      AWS SigV4 towards OpenSearch. Today's lack of both rules out most real deployments. *(medium)*
- [ ] F2 **Variable-length paths and shortest path**: Cypher `-[*1..5]->` and `shortestPath()`, run as a bounded
      breadth-first search with a visited set. Builds on the cycle handling (Q6, Q20). *(large)*
- [ ] F3 **Query profiling**: `?profile=true` returns, per step, the searches, hits, time and the planner's estimate
      against the actual count (like Gremlin's `profile()`). Turns the Phase 7 slow-log analysis into a feature. *(small–medium)*
- [ ] F4 **Gremlin front-end**, read-only first: the most-used graph language, and the way into JanusGraph's tools
      (Gremlin console, G.V(), graph-notebook). The engine already runs on TinkerPop (unipop); review the old
      `gremlin-server` branch (Phase 8) first. *(medium–large)*
- [ ] F5 **Indexes for vertices with very many edges**: sorted, filterable edges per vertex for hub vertices (nested
      documents, or routing per vertex as in 7.9), the counterpart of JanusGraph's vertex-centric index. *(large)*

**Tier 2: write path and data management**
- [ ] F6 **Write queries**: Cypher `CREATE` / `MERGE` / `SET` / `DELETE` and GraphQL mutations as bulk and update
      requests, checked against the ontology. Optimistic concurrency (`if_seq_no` / `if_primary_term`), not
      transactions with locking. *(large)*
- [ ] F7 **Schema rules enforced on write**: multiplicity (`ONE2MANY`, `SIMPLE`, …) and property cardinality
      (`SINGLE` / `LIST` / `SET`) declared in the ontology and checked by the loader. *(medium)*
- [ ] F8 **Index lifecycle API**: reindex when the ontology changes (`_reindex` and aliases), report index status,
      roll out mapping changes with no downtime. *(medium)*
- [ ] F9 **TTL per entity or relationship type**, through ISM policies on time-partitioned indices. *(small–medium)*
- [ ] F10 **Streaming and incremental loads**: chunked loads with backpressure instead of one bulk request per CSV,
      and an ingest endpoint for Kafka or Data Prepper. *(medium)*

**Tier 3: analytics and ecosystem**
- [ ] F11 **Graph algorithms**: PageRank, connected components, degree and communities over a subgraph, in the engine
      or through Spark (opensearch-hadoop), with results written back as properties. *(large)*
- [ ] F12 **Client SDKs**, Python first, generated from the OpenAPI 3.1 spec. *(small–medium)*
- [ ] F13 **Graph exploration in OpenSearch Dashboards**: a graph-view plugin calling the YANG-DB API (see
      `dashboards/`). *(large)*
- [ ] F14 **Multi-tenancy**: ontologies isolated per tenant, index-name prefixes, the caller's identity passed to
      OpenSearch security roles. Needs F1. *(medium)*
- [ ] F15 **Metrics endpoint**: Micrometer or Prometheus metrics for query latency, OpenSearch operations, open
      cursors and the planner's cache hit rate. *(small)*

**Tier 4: OpenSearch features JanusGraph can't offer**
- [ ] F16 **Vector similarity in patterns**: `WHERE knn(d.embedding, $v, 10)` with OpenSearch k-NN, then expand from
      the matches (GraphRAG: retrieval by embedding followed by graph expansion). *(medium)*
- [ ] F17 **Relevance-scored results**: keep `_score` from text matches and order paths by it. *(small–medium)*
- [ ] F18 **Aggregations in graph queries**: Cypher `count` / `sum` / `avg` with grouping, pushed down as OpenSearch
      aggregations. *(medium)*
- [ ] F19 **Cross-cluster graphs**: entities in different clusters, through cross-cluster search. *(medium)*
- [ ] F20 **Ontology import from existing indices**: infer a draft ontology from live mappings and fields that look
      like foreign keys, next to the OWL and SQL-DDL importers. *(medium)*

**Ideas not to adopt:** a separate storage backend like JanusGraph's (it would undo YANG-DB's reason to exist), and
full transactions with locking (OpenSearch can't support them; F6 uses optimistic concurrency).

**Suggested order:** F1, F3 and F15 (small, unblock real users); then F2 and F16 (the clearest positioning: graph and
vector search over the OpenSearch data you already have); then F4; then the write path (F6–F10).

Sources: [JanusGraph changelog](https://docs.janusgraph.org/changelog),
[JanusGraph Elasticsearch/OpenSearch index backend](https://docs.janusgraph.org/index-backend/elasticsearch/).

## Dev environment notes
- JDK 11 + Maven; Docker via colima on macOS. Quickstart: [docker/README.md](../../docker/README.md).
- Homebrew's Maven runs on Homebrew's newest JDK (27 on 2026-11-26), where Mockito-based tests fail
  (`Could not modify all classes [class java.lang.Object, ...]`). Set `JAVA_HOME` to the JDK 11 first.
- With the repo open in VS Code, its Java extension compiles into the same `target/` folders and leaves broken classes
  that an incremental Maven build reuses ("Unresolved compilation problem", or a type with no package). Build with
  `clean`.
- Stacked PRs: merge the base, retarget the stacked PR to `main`, then merge it. Merged right after its base, #197
  went into the base's branch instead of `main` and had to be re-landed (#198), as #164 did before (#165).
- Full tutorial load sequence: `GET /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId` → `POST /fuse/catalog/ontology` (Dragons.json) → `GET /fuse/load/ontology/Dragons/init` → `POST /fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex|edge&label=<Type>` per CSV.
- With the Docker stack up, run tests on other ports (see the agent rules above) or stop it first (`docker compose -f docker/docker-compose-os.yml stop`, data volumes are kept).
- On external (non-APFS) drives macOS writes `._*` sidecar files that break git and Maven; delete them before building and set `git config core.fileMode false`. They also block OpenSearch from deleting index folders, so keep the test node's data off such drives with `-Dfuse.test.data.dir=/tmp/...`.
