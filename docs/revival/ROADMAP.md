# YANG-DB Revival Roadmap

Living plan for bringing YANG-DB back to a maintained, promotable state. Update the checklists as work lands.
Bugs referenced by ID (e.g. `L1`, `Q2`) are described in [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

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

## Current workstream — Stabilize (in progress)
Loader
- [x] `L1` relation endpoint types rewritten — fixed in `CSVTransformer`, test `CSVTransformerTest#testRelationKeepsSourceTypeWhenPairsShareDestination`
- [x] `L2` pre-1970 epoch-millis dates dropped — fixed in `DataLoaderUtils`, test `DataLoaderUtilsTest`
- [ ] `L3` `INSERT` silently overwrites — fixed in `LoadUtils` and both loaders; IT `IndexProviderBasedCSVLoaderIT#testInsertDoesNotOverwriteButUpsertDoes` written, **not yet run** (blocked by `T1`)

Test infrastructure
- [ ] `T1` integration suite crashes (NPE) when an OpenSearch is already running locally, and the build reports false green

Query engine
- [ ] `Q1` undirected pattern `-[r]-` executes as `->`
- [ ] `Q2` relationship endpoint type ignored (horses returned as dragons)
- [ ] `Q3` edge whose endpoints share an id is dropped
- [ ] `Q4` relationship ids concatenated without a delimiter; per-pair collapse even without `distinct`
- [ ] `Q5` untyped `match (p)` returns nothing; label union `(p:A | B)` is a syntax error — fix or document as unsupported
- [ ] `A1` API renders Java `toString()` unless `Accept: application/json`

Tutorial data & docs
- [ ] `D1` tutorial CSV defects (header/row mismatches, header typo, duplicate edge ids)
- [ ] `D2` doc examples and claims that don't match reality (Queries.md, projection-and-count.md, api.md, load-data.md)

Verification
- [ ] `S13` end-to-end doc-query suite committed to the repo and passing
- [ ] `S14` full `mvn clean test` green with ITs running → **push the batch**

## Phase 1 — Shrink the footprint
- [ ] Move `fuse-domain` (Dragons sample, ~30K LOC, 100% demo code) out of the default build

## Phase 2 — One source of truth for test fixtures
- [ ] Shared fixtures module replacing the Dragons ontology/mapping/CSV copies duplicated across 7+ modules

## Phase 3 — Structural assertions instead of golden JSON strings
- [ ] Rewrite `OpensearchIndexProviderMappingFactoryIT` and similar tests; re-enable the tests `@Ignore`d during the OpenSearch migration

## Phase 4 — A real extension point
- [ ] `fuse-extension-api` module + `ServiceLoader` discovery; dogfood by extracting the SPARQL or GraphQL front-end

## Phase 5 — Ongoing hygiene
- [ ] Clear the Dependabot backlog, including the OpenSearch 2.x bump (needs a REST-backed `Client` adapter for the remote-cluster connections)
- [ ] Resolve or retire the `dragons-sys-test` profile failures
- [ ] Prune stale remote branches

## Dev environment notes
- JDK 11 + Maven; Docker via colima on macOS. Quickstart: [docker/README.md](../../docker/README.md).
- Full tutorial load sequence: `GET /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId` → `POST /fuse/catalog/ontology` (Dragons.json) → `GET /fuse/load/ontology/Dragons/init` → `POST /fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex|edge&label=<Type>` per CSV.
- Stop the Docker stack before `mvn test`: it occupies ports 9200/9300 used by the embedded test node (`T1`).
- On external (non-APFS) drives macOS writes `._*` sidecar files that break git and Maven; delete them before building and set `git config core.fileMode false`.
