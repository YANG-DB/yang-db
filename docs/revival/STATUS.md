# Stabilize — Live Status

Shared sync page for the Stage 1 agents and the lead. **Per-agent handoff files (goal, tasks, status, next step,
discoveries) for resuming after a restart: [agents/](agents/README.md).** Plan: [ROADMAP.md](ROADMAP.md) · bug details: [KNOWN_ISSUES.md](KNOWN_ISSUES.md).

**How to use (agents):** this file lives in the *main checkout* at
`/Volumes/X9 Pro/projects/yang-db/docs/revival/STATUS.md`. Don't edit it from your worktree copy. Edit **only your own
stream's section**, using small `Edit` replacements (never rewrite the whole file). Update it when you:
find a root cause, commit a fix, hit a blocker, or find something another stream needs. Read the other sections
before touching shared code. Keep entries short and dated (UTC, `YYYY-MM-DD HH:MM`).
Status values: `not started` · `investigating` · `fixing` · `testing` · `done (awaiting merge)` · `blocked`.

## Overview

| Stream | Issues | Status | Branch | Commits | Blocker |
|---|---|---|---|---|---|
| A — Cypher translator | Q5, Q6, Q7, Q8 | **done, all merged** (r3 `ac146fd66`) | `worktree-agent-a52a1b8474fd93794` | `a66ba46c2` Q8, `d25580d0f` Q7, `460153c8b` Q6 | — |
| B — Traversal identity | Q1–Q4, untyped match | **done, all merged** (r4 `f7eab43ae`, `a501228d4`, `79b8b0e01`) | `worktree-agent-a43e06c700eb4b7ef` | round 2 + `fe77d54f2` unipop-core vertex step | — |
| C — Service & loader | A1, L4 | **A1 merged to local main**; L4 → stream E | `worktree-agent-acdc794fa7038a886` | `54896dc37` A1 | — |
| D — Data, docs & e2e | D1, D2, S13 | **merged to local main** | `worktree-agent-a28d8e52a88b602de` | `5e32376ca` D1, `4588df726` S13, `a37981c53` D2 | needs rebuilt image to go green |
| E — Time partitions | L4 range buckets | **done, all merged** (r2 `e498ad46a`); e2e confirmation pending | `worktree-agent-a7164b98179501307` | `6b0a1c346` L4 | — |
| Lead | — | Phases 0–5 done; v0.8.0, v0.9.0, v0.9.1 released. Since 0.9.1: Dependabot cleared (#187), B1 fixed (#188), Phase 7.1 benchmark (#189), community review addressed (#190; JDK 17 tests green), README pointer (#191); image `ghcr.io/yang-db/yang.db:0.9.1` published and e2e-verified (2026-11-25). Next: Phase 7.2–7.3, then Phase 6. Resume: agents/lead.md | `main` | — | — |

## Timeline
- 2026-11-20 — Stage 0 done and pushed (`3ace71186`): 1,064 tests, 0 failures, ITs running.
- 2026-11-20 — Stage 1 launched: four agents, one worktree each.
- 2026-11-20 — All four agents stopped on an account usage limit before committing anything; worktrees reset to
  `main`. Resumed with this status page added.
- 2026-11-20 — Stream A done (Q5 label union rejected explicitly; Q1 + untyped match → B). Stream D done (D1, S13,
  D2). Both merged into local `main` (not pushed).
- 2026-11-20 — D3 resolved by the lead (count examples moved to `Know`). Stream C done: A1 merged (`54896dc37`);
  L4 analysed, awaiting maintainer decision. Stream B still running (critical path).
- 2026-11-20 — Maintainer chose **range buckets** for L4 → new stream E (ports 9250/9350). Stream B done: Q1, Q2,
  Q3, Q4 and untyped `match (p)` fixed, merged to local main. Full `mvn clean test` on merged main started.
- 2026-11-20 — Full `mvn clean test` on merged main (A–D): **1,088 tests, 0 failures**, BUILD SUCCESS, ITs running
  (fuse-dv-core 60 incl. `TraversalIdentityIT`). Rebuilding the Docker image for the e2e suite (pre-L4). Agents in worktrees can't edit this file; the lead copies reports in.

- 2026-11-20 — B r4 merged (`f7eab43ae`, `a501228d4`, `79b8b0e01`): all five run-2 failures correct live. Docker VM
  moved to the external drive. Full `mvn clean test`: **1,120 tests, 0 failures**. Docs, wiki and README updated;
  batch **pushed** at the maintainer's request. e2e run 3 on a rebuilt image is the next step.

## Stream A — Cypher translator (Q1, Q5)
- 2026-11-20 — **Done.** fuse-asg 263 tests, 0 failures (fuse-model 21, fuse-core 34 green).
- Q1 is **not** in fuse-asg: translation yields `RL`, every ASG strategy passes it through, GTA emits `bothE()`, and
  `DualEdgeDirectionSearchAppender` adds no direction filter, so the loss is below GTA (likely the Q4 id collapse, or
  a schema lookup per direction). `PathsTraversalCursor.toRelationship` has no `RL` case (eTag1/eTag2 never set for
  undirected). Debug trap: the query printer draws every non-`R` direction as `<--`. Guard test
  `CypherUndirectedTranslatorTest` pins the correct translator output. **→ handed to stream B.**
- Q5 decision:
  - `(p:A | B)` label union: **unsupported** (openCypher 9 grammar has no node-label union). Now fails with an
    explicit error suggesting `WHERE p:A OR p:B` (document this one) or `(p:A:B)` (YangDB treats it as OR, standard
    Cypher as AND, so don't document it). Test `CypherLabelUnionTranslatorTest`.
  - `match (p) return *`: ASG is correct (untyped node, GTA expands to all types), so the empty result is
    downstream. **→ handed to stream B.** Syntax not blocked.
- Note: stream A's worktree guard blocked edits here; the lead copied its report in.

- 2026-11-20 round 2 — **merged.** Q8: three fuse-dv-asg constraint strategies (Exact/Like/LikeAny) cast untyped
  nodes → now skipped; OR becomes a per-label union. Q7: the count filter is grouped by the traversal's start side and
  the planner reversed the relation (counting *incoming* Know) → new plan validator forbids reversing a count-filtered
  relation (M1 + M2 validators). Count runs over all edges before p2 row filters (docs changed: 16 persons). Q6:
  cyclic patterns → explicit "Cyclic patterns are not supported"; QG example replaced by its non-cyclic chain.
  fuse-asg 267, fuse-dv-asg 30, fuse-dv-epb 429, fuse-dv-core 60 — 0 failures.

## Stream B — Traversal identity (Q2, Q3, Q4)
- Findings (2026-11-20, resumed): edge ids come from `HashEdgeIdProvider` (fuse-dv-unipop) = `label + outId + inId`,
  no delimiter, doc id ignored → Q4. Edge search (`EdgeBulkSearchAppender`) filters only `entityA.id` (no type), and
  `DiscreteEdgeConverter` labels the other end from the schema, not the doc's `entityB.type` → Q2 suspected. Q3 still
  under investigation.
- 2026-11-20 — took over Q1 + untyped `match (p)` from stream A. Writing `TraversalIdentityIT` (fuse-dv-core suite,
  loads a tiny graph via the CSV loader, runs the planner's traversals through `M1ElasticUniGraphProvider`).
- Q3 lead: `Assignment.Builder` (**fuse-model**, outside my modules) keys entities by `eID` only, so Person 22 and
  Dragon 22 in one path merge into one entity. Confirming whether the traversal also drops it; a fuse-model fix
  (key by type+id) may be needed — **lead: OK to touch fuse-model `Assignment.Builder`?** → **approved by lead
  (2026-11-20).**
- Rid consumers (no parser found): `GraphTraversalCursor`/`GraphHierarchyTraversalCursor` dedupe by rID,
  `CsvTraversalCursor` sorts/prints it, `AssignmentDescriptor` prints it (`.`→`_`). Nothing splits it.
- Id-format changes and their consumers: —
- Blockers: —

## Stream E — Time partitions (L4)
- 2026-11-20 — **Done, merged (`6b0a1c346`).** Each value is the inclusive upper bound of its bucket (first bucket open
  below), compared numerically, cut in UTC; dates past the last bucket fail the row. New `TimeSeriesBuckets`; loader
  (`LoadUtils.resolveIndex`, graph loader delegates), query (`PromiseEdgeIndexAppender`), stats (`EBaseStatisticsProvider`)
  all agree. `YYYY`→`yyyy` in 8 Dragons configs. Tests: `TimeBasedIndexPartitionsTest` (7), IT
  `testLoadFireRoutesToTimeBuckets`. fuse-dv-core 68, epb 427, unipop 156, dv-asg 27 — 0 failures.
- Out of scope: `DragonsOntologyPhysicalIndexProviderFactory` (removed with the dragons-sys-test module in Phase 1) still exact-name; appender reads only the
  first time predicate; stats intersect `inSet` values instead of union; fixture dates via `new Date(String)` are
  local-time-zone.
- 2026-11-20 — started: implement range buckets across loader, query-side index selection and stats; out-of-range
  dates fail loudly; `YYYY`→`yyyy`. Ports 9250/9350, data dir `/tmp/yangdb-es-E`.

## Stream B — round 2 (2026-11-20, merged)
- Q1: `AsgQueryUtil.reverse` (fuse-model) turned undirected `RL` into `L` when the planner started from the filtered
  far end → Q2 245/349, Q5. Q3: `ForwardOnlyPathsTraversalCursor` saw Guild 0 → Kingdom 0 as a cycle (bare ids).
  Untyped match: distinct step keyed entity-only results by bare id. Q2 (partial): filter step searched only the
  first entity's index and mapped hits back by id (Horse 7 → Dragon 7).
- Round 3 (approved by lead): `UniGraphVertexStep` (**unipop-core**) groups a vertex batch by bare id, so Horse 7 and
  Dragon 7 in one batch merge → QE/QF still wrong where ids collide. Fix: key by type+id, one query per type.
- Round 3 merged (`fe77d54f2`): `UniGraphVertexStep` keys bulks by type+id, one query per type.
- Unexplained: `match (p)` capped at 1,000 results even with pageSize 100000 (unfiled).

## Stream C — Service & loader (A1, L4)
- Findings: 2026-11-20 — A1 root cause: `RegistrarsUtils.with` (cypher/sparql/graphql query routes) checks
  `req.accepts("application/octet-stream")` first, and `*/*` (or no Accept) matches it → writes
  `data.toString()` to a temp file and downloads it. Load routes (`Results.with`/`json`) already render JSON.
- 2026-11-20 — **A1 committed `efd08ef5e`** (test `RegistrarsUtilsContentNegotiationTest`, real Jooby server).
  Side find: fuse-service's unshaded reactor classpath mixes netty-buffer 4.1.43 (jooby-netty) with codec 4.1.73
  (OpenSearch) → every Jooby HTTP request fails in `mvn test` (installed shaded fuse-core jar hides it). Pinned
  netty-buffer 4.1.73 in fuse-service/pom.xml. Now on L4.
- L4 conclusion (2026-11-20): **intent ambiguous → no behavior change, no commit.** Mechanism: `TimeBasedIndexPartitions.getIndexName`
  (fuse-dv-core `GraphElementSchemaProviderJsonFactory`) only returns an index whose name equals
  `index.format % dateFormat(date)`; 1950 → `idx_fire_1950` → no match → `LoadUtils.resolveIndex` falls back to the
  first index of `getPartitions()`, which are *string-sorted* (`_1000,_1500,_2000,_500`) → `idx_*_1000`. Query side
  (`PromiseEdgeIndexAppender`) uses the same exact-value contract, and other fixtures fit it (hourly
  `yyyy-MM-dd-HH`, weekly `YYYY-W`). The Dragons config (`YYYY` + 500/1000/1500/2000) and IndexProvider.md ("values
  = incremental time buckets", 1900/1950/2000/2050) instead imply range buckets, which no code implements, and
  floor vs ceiling is unspecified. Lead decision needed: (a) implement range buckets in loader + query + stats
  together, or (b) keep exact semantics, fail loudly instead of falling back, and fix the Dragons config/docs.
  Also: `YYYY` is week-year in SimpleDateFormat (late-Dec dates → next year); use `yyyy`.
- Blockers: —

## Stream D — Data, docs & e2e (D1, D2, S13)
- 2026-11-20 — **Done, merged to local main.**
  - D1: Person gets `name`; horses drops the bogus `color` header; Freeze keeps the first of its two dates;
    RegisteredGuilds header fixed, ids `<guild>_<kingdom>_<n>`; Own/OriginatedIn ids type-prefixed
    (`dragon_88_880`, `horse_44_478`). All non-time-partitioned files load with 0 failures.
  - S13: `python3 docker/e2e/doc_queries.py --load --reset`; failures labelled `FAIL (known: Q1, Q4)`; vacuous
    passes flagged; non-zero exit on anything not passing.
  - D2: api.md, load-data.md, projection-and-count.md (now `Person-[Know]->Person`: Know 475 / Person 950),
    Queries.md (label union → `WHERE p:Person OR p:Dragon`; `match (p)` kept pending B).
- e2e on the **old** image (pre L1–L3): Fire/Freeze store 0 docs (L2). PASS Q1, Q2a, QC, QD, count cursor;
  vacuous PASS Q5–Q9; FAIL Q2 (245/349: Q1, Q4), Q3 (L1), QA (untyped), QE (1632 vs 96: Q1–Q4), QF (126 vs 7: Q2);
  ERROR QB (EUntyped→ETyped cast: untyped match), QG (see Q6 below).
- Follow-ups after B/C land: rebuild image, rerun suite; drop api.md's `Accept` note if A1 is fixed; recheck QB text.

## e2e run 1 — 2026-11-20, image built from merged main (A–D, pre-L4)
Full log: `/tmp/yangdb-e2e-run1.log`. Stack is **up** (fuse :8888, OpenSearch :9200) with freshly loaded tutorial data.
- **Loader: all green.** All 13 indices store exactly the expected counts, Fire 23,006 and Freeze 9,814 included (L1–L3 confirmed).
- **PASS:** Q1, Q2a (Know undirected 475), Q9 (distinct Fire 2,964), QC, QD, count cursor, all cursor types respond.
- **Still wrong end-to-end despite B's traversal-level fixes** (image verified to contain B's classes):
  - Q2 `(p1)-[Know]-(p2 {FEMALE})`: 245 rels vs 349 (same as before the fixes)
  - Q3 Guild-RegisteredIn-Kingdom funds>0: 28 vs 30
  - Q5 Fire undirected to BLACK dragon: 1,329 vs 2,437
  - Q6 multi-pattern: Person 5 vs 3
  - QA `match (p)`: partial (Dragon 903/1000, Guild 4/15, Kingdom 6/8, Person 87/100, Horse 0/500)
  - QE `Person-[Own]-Dragon`: returns **Horses** (77), 192 rels vs 96
  - QF: Dragon 12 vs 7
  → **reassigned to stream B** (the service's real execution path differs from what `TraversalIdentityIT` drives).
- **New issues:**
  - **Q7** — count filter `WHERE count(k) > n` returns nothing at all (Q7/Q8 docs examples). → stream A
  - **Q8** — `match (p) where p:Person OR p:Dragon` (stream A's recommended label-union alternative) → HTTP 500
    `EUntyped cannot be cast to ETyped`. → stream A
  - Q6 (cycle pattern rejected by validator) still open. → stream A

## e2e run 4 — 2026-11-20, image built from `5cb0117d2` (QB fix): **ALL 21 CHECKS PASS** ✅
- Full `mvn clean test` first: **1,123 tests, 0 failures** (71 skipped).
- All 16 doc queries pass (QB `match (p) where p:Person OR p:Dragon` included), plus the count cursor and the graph,
  csv, forwardPaths and projection cursors. All 13 indices hold exact counts; Fire and Freeze are in `idx_*_2000`.
- Once pushed, mark it with `docker/e2e/verify_and_mark.sh` (posts `e2e/docker` ✓ on the commit).

## e2e run 3 — 2026-11-20, image built from pushed `main` `56621b0af` → commit status `e2e/docker`: ✗ (20/21)
- Loader: all 13 indices have the exact counts. **L4 confirmed:** all Fire (23,006) and Freeze (9,814) docs are in
  `idx_*_2000`, and the other buckets are empty.
- **PASS (16):** Q1, Q2a, Q2, Q3, Q5, Q6, Q7 (91 persons / 9 matching p1), Q8 (56 / 16), Q9 (2,964 distinct Fire
  pairs), QA, QC, QD, QE, QF, QG, count cursor. All five cursor types respond.
- **FAIL (1): QB** `match (p) where p:Person OR p:Dragon` → `CompositeValidatorStrategy: Property type mismatch
  parent entity: EUntyped`. The Q8 rewrite crash is gone, but the ASG validator rejects the query. This is the form
  the docs recommend instead of label union. → stream A round 4; A hit the usage limit (resets 14:50 Pacific), lead
  investigating meanwhile.

## e2e run 2 — 2026-11-20, image from main after all merges
Full `mvn clean test` first: **1,113 tests, 0 failures**. Log: `scratchpad/e2e-run2.log`.
- Loader: all 13 indices exact counts. **But** all Fire/Freeze docs land in `idx_*_500` (should be `_2000`) → **E round 2**.
- PASS: Q1, QA (`match (p)` now complete!), QC, QD, QG (non-cyclic rewrite), count cursor, all cursor types.
- **Regression:** Q2a, Q2, Q3, Q5, Q6, Q7, Q8, Q9, QF → HTTP 500 `UnionPlanSearcher - No valid plan was found`.
  QB → `Property type mismatch parent entity: EUntyped` (validator). QE still returns Horses.
- **Correction:** B r4 showed the "No valid plan" failure was code (`ReverseRelationOpValidator` vs `RL`, plus a
  schema-provider cache returning empty lists), not data. The lead's jar-swap bisect was misleading, probably
  because shaded jars also carry those classes.
- Lead bisect by swapping jars into the container:
  - A's `AggregatedRelationDirectionOpValidator` (Q7) rejects plain directed plans: Q9 passes with it reverted
    → **A round 3**.
  - The undirected / multi-pattern failure persists even with the **complete run-1 jar set (`f8a96e4da`)** and the old
    `YYYY` config → data/state-related, not a code regression → **B round 4** (only B may swap jars in the container).
  - `fuse-data` volume is empty; deleting the leftover `projection` index (created by the e2e projection-cursor check
    and not removed by `--reset`) didn't help. e2e suite should clean it up (todo).

## New findings needing an owner (lead)
- **Q6 (new)** — Queries.md "owners who know each other" (closes a cycle) is rejected by the ontology validator:
  "Relation with No entity Attached", even rewritten as directed. Engine limitation; fix or document as unsupported.
- **D3** — dangling ids in tutorial data (owners 0–999 vs 100 persons) → **resolved 2026-11-20 by the lead
  (`4802446a5`)**: maintainer chose to keep the data and rewrite the count examples onto `Person-[Know]->Person`
  (`count(k) > 15` → 9 persons; `count(k) > 10 AND p2.gender = FEMALE` → 5). Still to confirm on the rebuilt image:
  the engine applies the row filter before counting, as the doc now says.
- **A2 (new, minor)** — a whole-file load failure (e.g. Fire/Freeze) is reported in a top-level `error` with empty
  success/failure lists — easy to misread as "0 of 0".
- B's scope grew (Q1 + untyped `match (p)` from A); it is the critical path.

- **L4 decided: range buckets (stream E).** Analysis: — code routes each date to the index named exactly after its formatted value
  (1950 → `idx_fire_1950`, nonexistent) and falls back to the first index in *text* order (`_1000`), so everything
  lands there; the query side uses the same exact-name rule. Config + IndexProvider.md describe range buckets that no
  code implements. Options: implement range buckets (loader + query + stats), or keep exact names, fail loudly, fix
  config/docs. Also: `YYYY` is week-year (use `yyyy`).
- From C (minor, unfiled): `RegistrarsUtils.with` error branch has no `return` (possible double send); query-side
  index ordering for date `<`/`>` filters is textual. netty-buffer pinned to 4.1.73 in fuse-service (reactor mixed
  4.1.43/4.1.73 → `NoSuchMethodError`).

- From B (unfiled): a search against a missing index fails silently with empty results; `OpensearchIndexProviderMappingFactoryIT`
  leaves index templates behind that break later index creation (B ordered its IT first); edge search still doesn't
  filter on `entityA.type` (converter drops wrong-type docs, so a search limit may count them); `HashEdgeIdProvider`
  (undelimited ids) still used on the promise path. New relationship id format: `<label>:<csv id>` (e.g. `Fire:FIRE_D02D01`).

## Cross-stream notes
- Only the lead edits ROADMAP.md / KNOWN_ISSUES.md and merges to `main`; nothing is pushed until S14.
- Docker stack is reserved for stream D during Stage 1. Internal disk had 7.4 GB free at resume (stop Docker work
  below 3 GB).
