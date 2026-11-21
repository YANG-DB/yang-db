# Stream G — fuse-dv-epb ignored stats/estimator tests

## Goal
Re-enable 4 `@Ignore`d tests in fuse-dv/fuse-dv-epb with root causes:
EpbJoinSelectionTests#testJoinPlanSelection, StatisticalCostEstimatorTests#calculateStepPattern,
BasicPatternCostEstimatorTest#calculateFullStep (Integer->Long CCE), and
EBaseStatisticsProviderIndicesTests#eRelDateNotInSetFilterSingleIndexHistogramTest (500 vs 510).

## Tasks
- [x] Reproduce all 4 failures with @Ignore removed
- [x] Root-cause the ClassCastException (test fixture)
- [x] Root-cause the histogram mismatch (test expectation)
- [x] Full fuse-dv-epb suite: 433 run, 0 failures, 0 errors, 6 skipped
- [x] Commit once per root cause

## Status
DONE. Next step for the coordinator: cherry-pick 603fd7bfb and d41d798c0 onto main.

## Discoveries
- CCE: `getGlobalSelectivity` returns primitive `long`; Mockito `thenAnswer` lambdas returned boxed `Integer`
  (`StatisticsMockUtils`: `-> 10`; `EpbJoinSelectionTests`: `return 10000;` / `return 1;`). Mockito unboxes via a
  `Long` cast inside the mock. Stack (with `-DtrimStackTrace=false`):
  `StatisticsProvider$MockitoMock.getGlobalSelectivity` <- `EntityRelationEntityPatternCostEstimator.calculateFullStep:103`.
  No production code bug.
- Histogram: `notInSet` on the time field becomes `ne` conditions; `findIndicesToRemove` prunes nothing for `ne`
  (correct — older-index edges all satisfy a negated set). Both indices' buckets (10/5 + 500/10) are multi-valued, so
  the estimate is 510/15. Test expected 500/10 (as if the older index were pruned). Not related to L4 (L4 didn't touch
  `ne`); not the inSet-intersection bug either (both excluded values fall in the same hourly index).
- Not fixed (out of scope, flagged): `findRelevantTimeSeriesIndices` turns `inSet` into one `eq` per value and each
  `eq` removes every other index, so values spanning indices intersect to an empty index list (should be union).
  Stream E reported the same. No test here covers it.
- Fixture quirk (harmless): `EpbJoinSelectionTests` mock compares `direction.name().equals(Rel.Direction.R)` (String vs
  enum, always false). Left as is; the test passes with its current expected plan.
- Stack traces are trimmed by surefire; use `-DtrimStackTrace=false` to see frames inside mocks.

## Commits
- 603fd7bfb Phase 3: return long from getGlobalSelectivity mocks (re-enable EpbJoinSelectionTests, StatisticalCostEstimatorTests, BasicPatternCostEstimatorTest)
- d41d798c0 Phase 3: correct notInSet date histogram expectation (re-enable EBaseStatisticsProviderIndicesTests#eRelDateNotInSetFilterSingleIndexHistogramTest)

## Log
- Fail-before: 14 run, 1 failure (500 vs 510), 3 errors (CCE).
- Pass-after: same 4 classes 14 run, 0 failures (1 skipped is an unrelated, intentional ignore).
- Full epb suite (`-pl fuse-dv/fuse-dv-epb -am -Dtest='com/yangdb/fuse/epb/**/*, !**/._*'`): 433 / 0 / 0 / 6 skipped.
