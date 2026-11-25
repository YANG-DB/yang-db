# Bug-fix round 2: data and schema layer

Goal: fix A6, A7, A2, M1, M2 (see `KNOWN_ISSUES.md`), each with a test that fails first, one commit per bug.

| Bug | Status | Commit |
|---|---|---|
| A6 id generator same range | fixed | `42db66252` |
| A7 Snowflake XOR | fixed | `f91e2206d` |
| A2 whole-file load failure | fixed (HTTP status still 200, see below) | `4677b0353` |
| M1 unified relation fields | fixed | `3d079ddcd` |
| M2 unified template patterns | fixed | `76b98fb17` |

## Root causes and fixes

- **A6**: `new IndexRequest(getResponse.getId()).index(...)` put the doc id in the index-name slot and never set
  `.id(genName)`: each call indexed a new auto-id document, the counter stayed at 1. Now the counter doc (id = generator
  name) is written with `setIfSeqNo/setIfPrimaryTerm` from the get (the REST bridge hands `IndexRequest` to the RHLC
  unchanged and translates `version_conflict_engine_exception`); a conflict re-reads the counter (up to 100 times, then
  the existing resilience4j retry). `init` and first use create the counter with `op_type=create`, so re-running init
  no longer resets it, and an existing index is tolerated (`ResourceAlreadyExistsException`; the old code caught
  `OpenSearchParseException`, which is never thrown, so a second init failed).
  Test: `BasicIdGeneratorIT` (added to `TestSuiteIndexProviderSuite`): consecutive ranges, init doesn't reset, 4
  unshared generators x 10 concurrent calls get contiguous disjoint ranges. Before: 2 failures + 1 error.
- **A7**: `% (2 ^ 6)` is `% 4`. Now `% (1L << 6)`. Test: `SnowflakeRequestIdSupplierTest` (expected 6, was 2).
- **A2**: a whole-file failure is a runtime exception (e.g. `FuseErrorException` from the CSV parser) that escaped
  `StandardDataLoaderController`; the logging decorator turned it into `ContentResponse.internalError`: a top-level
  `error`, no data. The controller now answers one commit with no successes and one failure carrying the reason,
  status BAD_REQUEST, and keeps the top-level `error` for existing consumers (e2e `load_tutorial.py`, playground).
  The playground counts such a load once. Tutorial `load-data.md` updated.
  Test: `StandardDataLoaderControllerTest` (before: the exception propagated).
- **M1**: in a unified index the entities create the shared mapping first, so relations went through
  `populateProperty` only. New `populateRelationSides` (direction + both sides) is used by both paths; side objects are
  merged so every relation's redundant fields accumulate.
- **M2**: unified templates listed every type's bare name as a pattern; now only `<index>*`.
  Tests (M1, M2): `OpensearchIndexProviderMappingFactoryIT#testGenerateSingleMappingMapsRelationSides` and
  `#testGenerateSingleMappingPatternsOnlyMatchUnifiedIndex` re-enabled; the unified expectations in
  `expectedTemplates` updated (patterns, merged relation fields). Before: 4 failures in that class.

## Verification

- New ITs + mapping-factory IT, before fixes: 11 run, 6 failures, 1 error; after: 11/11 pass.
- Unit tests: fuse-dv-core 56 run / 0 fail (1 skipped), fuse-service 22 / 0 fail; examples ext compiles.
- Full `TestSuiteIndexProviderSuite`: not completed here. The run on the final tree stalled (machine slept, the embedded
  cluster timed out publishing cluster state) and the coordinator stopped it. The coordinator runs the full reactor
  build on the integration branch.

## Open / notes

- A2: the load routes render with `Results.json`, which ignores the response status, so a failed load still answers
  HTTP 200. Fixing it is route code (`DataLoaderControllerRegistrar`), out of scope for this round.
- The M1 commit was not built on its own (only M1+M2 together was tested).
- Unrelated: `fuse-stat` tests `StatCalculatorTest`, `EsUtilTest`, `StatCalculatorDynamicFieldTest` error with an NPE
  in setup when run as plain unit tests with `-am`; not touched.
- Unrelated: one earlier run of the full IT suite hung in `IndexProviderBasedCSVLoaderIT.setUp` (create index waiting
  forever; the embedded node's cluster-state publish was stuck behind `processPendingDeletes`). The code there uses
  `actionGet()` without a timeout, so a stalled embedded node hangs the suite instead of failing it.
