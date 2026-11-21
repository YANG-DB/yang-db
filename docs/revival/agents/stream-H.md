# Stream H — robust (structural) DSL/JSON assertions

## Goal
Replace brittle whole-string JSON comparisons in unipop + fuse-model tests with structural assertions,
without weakening them. Test code only.

## Tasks
- [x] unipop helper `QueryDslAssert` (fuse-dv-unipop test, package controller.search)
- [x] ConstraintSearchAppenderTest (5 live literals converted; 2 dead ES5 commented literals removed)
- [x] CountFilterQueryTranslatorTest, DistinctFilterQueryTranslatorTest
- [x] fuse-model helper `JsonTreeAssert` + QueryTest (2 long literals -> per-element; Q1..Q11 moved off lenient JSONAssert)
- [x] mutation check per class (all failed as expected, reverted)
- [x] module test counts unchanged, 0 failures; one commit per module

## Status
DONE. Next step: coordinator cherry-picks b7af9b282 and 4b4bb78c4.

## Discoveries
- Production bug: `AggregationBuilder.BuildFilterScript` emits `a=120` for `eq` (painless assignment, not `==`)
  and `a>120` for `gte` (should be `>=`). The count-filter tests pin both (now with NOTE comments).
- Count filter terms agg uses default size 10, so only the top-10 buckets are ever filtered (not asserted anymore).
- `DistinctFilterQueryTranslatorTest` is a verbatim copy of `CountFilterQueryTranslatorTest` (tests the
  CountFilter translator; no Distinct translator exists).
- QueryTest.testDeSerialization: the "q11" block re-reads Q005.json (Q011.json exists, untested); q2 block asserts
  q1Obj not null instead of q2Obj. Left as is (reported).
- QueryTest Q1..Q11 used JSONAssert LENIENT, which ignored element order; now order-checked.

## Commits
- b7af9b282 Phase 3: assert fuse-dv-unipop query DSL structurally instead of as JSON strings
- 4b4bb78c4 Phase 3: assert fuse-model query DSL structurally instead of as JSON strings

## Log
- start at e48e7ce09
- baseline/after: fuse-model 27 tests, unipop 156 (24 skipped), 0 failures
- mutation unipop: 5 deliberate breaks -> exactly 5 failures; key-reorder control passed
- mutation fuse-model: 3 breaks -> 3 failures
