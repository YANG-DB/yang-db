# Bug-fix agent J (C1, C2)

## Goal
Fix C1 (count-filter Painless operators) and verify C2 (terms agg default size 10) in fuse-dv-unipop.

## Tasks
- [x] C1: un-pin tests, see them fail (5/5 failed for the right reason), fix BuildFilterScript (eq, gte, neq), pass
- [x] C2: verified NOT real; added a regression test pinning the mechanism
- [x] Full fuse-dv-unipop test run: 152 run, 0 failures, 18 skipped (156 pre-Phase-3 minus 6 deleted, plus 2 new)
- [x] Commits: 086638254 "Fix C1", 17b5580f2 "Verify C2" (test only)

## Status
Done. Next: coordinator cherry-picks both commits and marks C1 fixed and C2 "not a bug" in KNOWN_ISSUES.md.

## Discoveries
- CountFilterCompare has eq, neq, gt, gte, lt, lte. BuildFilterScript rendered eq as "=", gte as ">", and neq as
  nothing ("a120", a Painless compile error). `neq` is reachable: ConversionUtil maps count op `ne` -> CountFilterP.neq.
- C2 not real: DiscreteVertexController.filterByAggregation runs the agg in a separate request and calls
  `searchBuilder.setSize(context.getBulkSize())` (number of start vertices in the bulk). SearchBuilder.build ->
  enforceSize sets that size on every top-level TermsAggregationBuilder; the count-filter terms agg is top-level.
  The query is restricted to the bulk's edges, so buckets <= bulk size; every group fits.
- Side note (not fixed): SearchBuilder.enforceSize's CompositeAggregationBuilder branch casts to
  TermsAggregationBuilder (would throw ClassCastException); unreachable today, nothing builds composite aggs.
- Side note: CountFilterP.between returns AndCountFilterP (a ConnectiveP), whose biPredicate is not a
  CountFilterCompare, so CountFilterQueryTranslator.test() rejects it; count `within` likely doesn't reach the
  count filter. Not verified.
- If bulk size were 0, enforceSize skips (size > 0 check) and the default 10 applies, but then there are no vertices.

## Log
- reset to origin/phase3-structural-assertions (d709e0887), created branch bugfix-J-c1-c2
- tests un-pinned + new all-operators test: 5 failures before fix; 5 pass after
- added testCountFilterTermsSizeIsTheBulkSizeNotTheDefault
