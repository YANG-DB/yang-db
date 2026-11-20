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
- [ ] `A2` whole-file load failure easy to misread (minor, open)

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

## Phase 2 — One source of truth for test fixtures
- [ ] Shared fixtures module replacing the Dragons ontology/mapping/CSV copies duplicated across 7+ modules

## Phase 3 — Structural assertions instead of golden JSON strings
- [ ] Rewrite `OpensearchIndexProviderMappingFactoryIT` and similar tests; re-enable the tests `@Ignore`d during the OpenSearch migration

## Phase 4 — A real extension point
- [ ] `fuse-extension-api` module + `ServiceLoader` discovery; dogfood by extracting the SPARQL or GraphQL front-end

## Phase 5 — Ongoing hygiene
- [ ] Clear the Dependabot backlog, including the OpenSearch 2.x bump (needs a REST-backed `Client` adapter for the remote-cluster connections)
- [x] Resolve or retire the `dragons-sys-test` profile failures: retired in Phase 1
- [ ] Prune stale remote branches

## Dev environment notes
- JDK 11 + Maven; Docker via colima on macOS. Quickstart: [docker/README.md](../../docker/README.md).
- Full tutorial load sequence: `GET /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId` → `POST /fuse/catalog/ontology` (Dragons.json) → `GET /fuse/load/ontology/Dragons/init` → `POST /fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex|edge&label=<Type>` per CSV.
- With the Docker stack up, run tests on other ports (see the agent rules above) or stop it first (`docker compose -f docker/docker-compose-os.yml stop`, data volumes are kept).
- On external (non-APFS) drives macOS writes `._*` sidecar files that break git and Maven; delete them before building and set `git config core.fileMode false`. They also block OpenSearch from deleting index folders, so keep the test node's data off such drives with `-Dfuse.test.data.dir=/tmp/...`.
