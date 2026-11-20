# Stream B — Traversal identity (`stream-B-traversal`)
*(Mirrored by the lead from the agent's worktree copy, 2026-11-20.)*

Worktree `.claude/worktrees/agent-a43e06c700eb4b7ef`, branch `worktree-agent-a43e06c700eb4b7ef`.

## Goal
Query results keep entity identity = type + id end to end (REST service on the tutorial data), and undirected
patterns return both directions: KNOWN_ISSUES Q1, Q2, Q3, Q4 and the untyped `match (p)` part of Q5. Done = touched
modules green with ITs running, plus the e2e doc-query suite passing for these queries against the Docker stack.

## Tasks
- [x] Round 1: Q2/Q4 edge converter (stored types, direction, doc-based ids), Q1 RL eTags, Q3 Assignment.Builder,
      untyped nested index (merged)
- [x] Round 2: service path — Q1 AsgQueryUtil.reverse, Q3 forwardPaths cycle check, untyped distinct key,
      Q2 filter-step type check (merged)
- [x] Round 3: unipop-core UniGraphVertexStep keyed by type+id, one query per type (merged as fe77d54f2)
- [x] Round 4: "No valid plan" for undirected queries — root cause found + fixed locally (ReverseRelationOpValidator)
- [x] Round 4: commit the validator fix → `571c60504` (epb 433 green)
- [x] Round 4: Q3 / QF undirected from a relation's target side → `9c9789fde`
- [x] Round 4: QE horses (GTA EntityFilterOp dropped the target type) → `e2d3691e7`
- [x] Round 4 report to the lead (2026-11-20)
- [ ] Lead: merge `571c60504`, `e2d3691e7`, `9c9789fde`; rebuild the image; run e2e suite (S13)

## Status
Round 4 done (2026-11-20), awaiting merge. Branch = main `ac146fd66` + `571c60504`, `e2d3691e7`, `9c9789fde`. Working
tree clean apart from this file (uncommitted by rule).

Live verification on the Docker stack (tutorial data, run-2 load), all as expected: Q2a 475, Q2 349, Q3 30, Q5 2437,
QA 1623 (all 5 types), QE 96 (no horses), QF 7 persons / 1 guild / 7 dragons, directed Q9 11,503.

Container `dragons-graph` currently runs `fuse-dv-unipop`, `fuse-dv-gta`, `fuse-dv-epb` jars built from this branch's
HEAD (= the 3 commits above on top of main) and the ORIGINAL logback.xml. The image's original jars are saved in the
session scratchpad as `*.container-orig.jar`. A rebuilt image from main (after merge) replaces all of this.

**Exact next step:** none for stream B unless the lead's e2e run after merge shows new failures. Open items for
others are listed under Discoveries.

## Discoveries
- Edge identity (round 1): `HashEdgeIdProvider` = label+outId+inId, no delimiter; now `"<label>:<doc id w/o .out/.in>"`.
  rID consumers only compare/sort/print (graph cursors, CsvTraversalCursor, AssignmentDescriptor); none parse it.
- `DiscreteEdgeConverter` must pick the edge schema per document (stored direction + entityA/entityB types).
- Service path (round 2): `ForwardOnlyPathsTraversalCursor` (cursorType forwardPaths) + `AssignmentsQueryResult.distinct`
  — both compared bare ids. The promise-filter step (EntityFilterOp) searched the first bulk vertex's index.
- unipop-core `UniGraphVertexStep` grouped bulks by bare id (round 3).
- Round 4 root cause of "UnionPlanSearcher - No valid plan was found": `ReverseRelationOpValidator` requires a reversed
  relation's direction to differ from the query's. After `AsgQueryUtil.reverse` correctly keeps RL as RL, every
  reversed undirected step was rejected. The DFS planner's global pruner (`CheapestPlanPruneStrategy`) keeps only the
  single cheapest partial plan per step, so whenever the cheapest start was the far end (count tie → hash tie-break,
  data dependent), the search dead-ended and returned null. This explains why it looked like "data/state": which end
  wins depends on counts. The lead's report that reverting the reverse fix didn't help is not consistent with this;
  the validator unit test proves the rejection.
- The epb planner doesn't log validator rejections (DEBUG on `com.yangdb.fuse.epb` showed nothing).
- Ruled out (round 4): swallowed exceptions during planning (none in logs); missing indices (same set as run 1);
  empty-list entries in the edge schema map for BOTH lookups (the map only has non-empty keys).
- `-Dtest` overrides surefire's pom excludes, so `._*` sidecars get run as tests; add `, !**/._*` to `-Dtest`.
  Not fixable with a one-line pom change.
- `fuse-dv-epb -am` pulls in `fuse-dv-stat`, whose tests use port 9300 → never run it with -am while Docker is up;
  run epb tests with `-Dtest='com/yangdb/fuse/epb/**/*, !**/._*'`.
- `OpensearchIndexProviderMappingFactoryIT` leaves single-index templates that break later index creation; keep
  `TraversalIdentityIT` before it in `TestSuiteIndexProviderSuite`.
- Round 4 QE root cause: `EntityFilterOpTranslationStrategy.appendPropertyGroup` (non-first entity + EntityFilterOp)
  first removes the preceding has(label) steps (`cleanPreviousFilteringSteps`) and only re-adds the type under the
  `filterEntity` option; M1 uses `none` → the target type was never checked. Masked before round 1 because every
  owned thing was mislabeled with the schema's first type. Even DIRECTED `(p:Person)-[:Own]->(d:Dragon)` returned
  horses; verified the container jars do contain rounds 2-3 fixes (javap).
- Round 4: `(k:Kingdom)-[:RegisteredIn]-(g:Guild)` (undirected written from the target side) is rejected by the
  ontology validator (`CompositeValidatorStrategy`) — ASG-side, not stream B; report to lead/stream A.
- Q3 `(g:Guild)-[:RegisteredIn]-(k:Kingdom)` returned 0 with the validator fix in place: plan starts at Kingdom, RL,
  then EntityFilterOp on Guild — likely the same GTA bug class or the filter step; re-check after the GTA fix.
- Round 4 Q3/QF root cause: the service wraps the schema provider in `GraphElementSchemaProvider.Cached`
  (`CachedGraphElementSchemaProviderFactory`), which pre-fills OUT and IN entries for every (vertex label, edge label)
  with EMPTY lists. `Impl.getEdgeSchemas(label, BOTH, rel)` = "OUT entry unless null, else IN" → with Cached the empty
  OUT list wins, so an undirected step from a relation's target side (Guild/MemberOf, Kingdom/RegisteredIn,
  Dragon/Own) gets no schema; `DiscreteVertexController.canDoWithoutQuery` treats "no schema" as "no query needed" →
  0 edges, silently. Tests used the plain Impl (null → IN fallback) so they passed. Found by swapping a debug
  fuse-dv-unipop jar into the container (prints `providerClass=...$Cached schemas=[]`).
- Ruled out (round 4): GTA translation of reversed RL (probe printed a correct VertexStep(BOTH)); the RawSchema wrapper
  chain (not involved); index provider config (same as test fixture apart from index name `person`).
- Out of scope / open: a search on a missing index fails silently (empty result); edge search doesn't filter
  `entityA.type` (converter drops wrong-type docs, but limits may count them); `HashEdgeIdProvider` still used by the
  promise controller; `match (p)` result looked capped at 1000 in run 1 (explained by the distinct bug, re-check).

## Commits
All merged to main unless noted.
- round 1: `1ea3f1bd7` Q1/Q2/Q4 edge converter · `892fdab97` Q1 RL eTags · `77b732126` Q3 Assignment.Builder ·
  `f8a96e4da` untyped nested index
- round 2: `db31209f8` Q1 AsgQueryUtil.reverse · `233ff7e9c` Q3 forwardPaths cycle check · `fdea87c27` untyped
  distinct key · `13fd322ab` Q2 filter step
- round 3: `fe77d54f2` Q2 UniGraphVertexStep type+id
- round 4 (not merged yet): `571c60504` Q1 ReverseRelationOpValidator accepts RL from the far end · `e2d3691e7` Q2
  GTA EntityFilterOp keeps the target type · `9c9789fde` Q1 Cached BOTH edge-schema lookup

## Log
- 2026-11-20 round 1 done: 4 commits, IT `TraversalIdentityIT` added to `TestSuiteIndexProviderSuite`.
- 2026-11-20 round 2: e2e showed fixes didn't hold through the service; found 4 service-path root causes, 4 commits;
  unipop-core edit blocked (out of scope) → flagged.
- 2026-11-20 round 3: unipop-core change approved and committed; all modules green.
- 2026-11-20 round 4: reproduced "No valid plan" live; DEBUG logging gave nothing; traced to ReverseRelationOpValidator
  vs RL (failing unit test, fix); swapped epb jar into container → Q2a/Q2 correct. Q3 undirected (0 rows), QE
  (Horses), QF still wrong on the service; target-side undirected IT passes locally.
- 2026-11-20 rebased onto main `ac146fd66`. `git rebase` stops on a duplicated todo line in this worktree (twice
  now); recovered via WIP commit `35e1e5477` → `git reset --hard main` + `git cherry-pick --no-commit`. Use that
  instead of `git rebase` here. Running epb tests for the validator fix.
- 2026-11-20 committed `571c60504`; found QE root cause in GTA EntityFilterOp translation; gta tests red → fix written.
- 2026-11-20 QE fixed live by GTA jar swap (QE 96, Q5 2437, QA 1623, Q2a/Q2 ok). Q3/QF root cause found via debug
  jar: `GraphElementSchemaProvider.Cached` BOTH lookup returns []; writing failing test (IT with Cached provider).
- 2026-11-20 committed `e2d3691e7` (GTA) and `9c9789fde` (Cached BOTH); all modules green; swapped committed jars into
  the container and restored logback → every listed e2e query correct. Reported to lead.
