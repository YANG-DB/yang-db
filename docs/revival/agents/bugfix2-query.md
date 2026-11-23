# Bug-fix round 2, query-engine agent (C3, Q11, P2)

## Goal
Fix C3 (two count conditions on one relationship), Q11 (rel property + end-pattern filter fails validation),
P2 (range op on an enumerated value crashes planner statistics). One commit per bug, failing test first.

## Status
- [x] C3: fixed (fuse-dv-unipop)
- [x] Q11: fixed (fuse-asg)
- [x] P2: fixed (fuse-dv-epb)

## Root causes
- **C3**: each count condition reaches `CountFilterQueryTranslator` as its own `has(entityA.id, CountFilterP)`.
  The second call did `aggregationBuilder.seek(field)` (which lands on the existing count-filter composite) and then
  `countFilter(field)`, which looked for a *child* of that name and so nested a second, never-built
  `CountFilterComposite` inside the first. Only the first condition reached the bucket selector
  (`count(k) > 15 AND count(k) < 17` ran as `a<17`: 99 persons). Fix: `countFilter` stays on a count filter of the
  same name; conditions accumulate (`AggregationBuilder.countCondition`), and `BuildFilterScript` joins them with
  `&&` (`def a=params.entityA_id_Count;a>15&&a<17`). The single-condition `operator`/`operands` params still work.
- **Q11**: `RelationPatternRangeAsgStrategy.replaceEndPattern` looked for "the quant right after" the end entity
  with `nextAdjacentDescendant(p, QuantBase, 1)`; the one hop let it step over the next element. In an expanded
  multi-hop path the intermediate end entity is followed by the next hop's `Rel`, so the end-pattern filter was
  added to the quant *after that rel*, where it has no entity parent ("No Parent Element found"). Fix: only a
  quant directly after the entity; otherwise the existing branch inserts one. Test counts updated to the correct
  shape (each intermediate end entity: `Q[all]{-[next hop]->, filter}`).
- **P2**: enum statistics are String term histograms, but the constraint expression (an ontology `Value`) was
  passed as is, and the histogram compares it as `Comparable`. Fix: `EBaseStatisticsProvider` compares an enum
  `Value` by its name (lists element-wise, for inSet/inRange). The un-ignored test's old expected plan predated the
  redundant-property push-down; updated like its sibling `redundantPropertyPathSelectionTest2`.

## Tests
- C3: `ElementConstraintSearchAppenderTest#testTwoCountConstraintsOnTheSameField` (new): failed `a>15` vs
  `a>15&&a<17`, passes.
- Q11: `RelationRangeAsgStrategyWithFiltersInRangTest#testUntypedToTypedWithQuantPropsAfterRelStrategyWithoutQuantsInPath`
  un-ignored: failed with the 3 "No Parent Element found" errors, passes.
- P2: `SmartEpbRedundancyTests#redundantPropertyPathSelectionTest` un-ignored: failed with the ClassCastException,
  passes.

Module runs (reactor, under the IT lock, ports 9430/9530): fuse-asg 312 run / 0 failed; fuse-dv-unipop 154 / 0;
fuse-dv-epb 433 / 0; fuse-dv-asg 38 / 0; fuse-dv-stat 17 / 0.

## Commits
- `1ce8d2e6f` C3 · `1b031286e` Q11 · `6c8b5b88e` P2 · this file: the next commit

## Open questions / notes
- Unrelated: in the same run, fuse-dv-core `TestSuiteIndexProviderSuite` had 3 embedded-OpenSearch errors
  (`INIT() - Create Indices error` in IndexProviderBasedCSVLoaderIT and TraversalIdentityIT,
  `ClusterManagerNotDiscoveredException ... publication timed out` in PersistentResourceStoreIT; the suite took
  16,121 s, so the machine probably slept or was overloaded). fuse-dv-core isn't touched by these fixes.
- C3: the aggregation side has no AND/OR structure: `count(k) < 5 OR count(k) > 20` would now be ANDed (before,
  only one of the two applied; both wrong). Count conditions under OR need a design decision (reject, or build
  the script from the traversal's connective).
- C3 not verified live: during this round the Docker stack's `know` index was empty (only `person` had docs), so
  the C3 query could not be rerun against data; e2e R1/R2-style check recommended after merge.
- Unrelated: `RestBridgeClientRemoteIT` (fuse-opensearch-client) failed once in a reactor run and passed in the next
  (flaky / environment). Sparql/OWL tests log `UnknownHostException: yangdb.org` (no network) but pass.
- Side note (not fixed): `BaseEqualityExpression` gives every count prop on a relation the same eNum
  (both C3 props were `201`); harmless today.
