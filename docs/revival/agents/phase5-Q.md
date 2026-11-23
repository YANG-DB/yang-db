# Phase 5.2 (agent Q): package relocations and changed APIs

## Goal
OpenSearch 1.3.0 -> 2.19.7: fix census categories (a) relocations and (b) removed/changed APIs across all modules,
including the 3 `Client` wrappers in fuse-dv-core. Everything compiles (`test-compile -Pcore,dragons-assembly`)
apart from (c) mapping types (stream R) and (d) TransportClient sites (stream P).

## Tasks
- [x] relocation script (census §5.2) over every reactor module (es-plugins excluded: not in any profile)
- [x] wildcard files: explicit `core.action` / `common.action` imports, `core.xcontent.*`
- [x] (b) XContentParser.bigIntegerValue, XContent.mediaType, CompressorRegistry, MediaTypeRegistry, EsUtil maps
- [x] wrappers: untyped overloads + 4 PIT methods
- [x] rebase onto the bridge (5.1) and relocate `fuse-opensearch-client`
- [x] clean test-compile with local placeholders: all 24 reactor modules green
- [x] rebased onto 36fbd5cca (bridge + stream S deps); Jackson 2.18 check: CountConstraintOp wire format restored
- [x] unit tests run (fuse-dv-stat excluded)

## Status
DONE. 6 commits on top of 36fbd5cca. With the local placeholders, clean test-compile of all 24 modules is green on
the real 2.19.7 / Jackson 2.18 versions. Remaining errors are (c) and P's bridge admin calls (see final report).
Test run (placeholders applied): model 30/0, core 18/0, asg 312/0, unipop 152/0, dv-asg 38/0, gta 61/0, service 19/0,
dragons-ext 7/0, datagen 34/0, graphql 37/0, assembly 4/0. dv-core 79 run, 11F/8E: all ITs failing on the typed
mapping wrapper ("Root mapping definition has unsupported parameters [Own : ...]") = R. dv-epb 432 run, 2E:
NoSuchMethodError from the stale 1.3-built fuse-dv-stat jar in ~/.m2 (dv-stat excluded with -pl), env only.
Note: `-Dtest='!**/*IT'` did NOT exclude ITs.

## Discoveries
- `-pl` builds resolve stale 0.80-SNAPSHOT jars from ~/.m2 (built against 1.3), which put both
  `org.opensearch.common.xcontent.XContentParser` (1.3) and `core.xcontent` on the classpath: "reference is ambiguous"
  errors. Always build the full reactor (or `-am`).
- Local placeholders (never committed) live in the scratchpad as `c-local.patch` (census §5.4 placeholders) and
  `f-local.patch` (StatUtil commons-codec -> java.util.Base64), `p-local.patch` (bridge: 5 typed RHLC admin calls
  stubbed, 2 bridge-era tests with typed calls). TransportClient stubs are only needed in fuse-service now.
- The bridge's `RestBridgeClient` uses RHLC typed admin overloads (createAsync/putMappingAsync/getMappingAsync/
  putTemplateAsync with server request types, getTemplateAsync) that 2.19 removed: stream P must convert them to
  `org.opensearch.client.indices.*` requests (P's own comment at line ~255 plans this).
- Bridge tests (`RestBridgeClientTest`, `RestBridgeClientRemoteIT`), `ClientProviderBridgeIT` (dv-core) and
  `ClientProviderTest` (dv-stat) use typed prepareIndex/Get/Update/Delete/TermVectors/Explain: (c).
- Unit-test errors in dv-unipop are all runtime dependency: `NoClassDefFoundError jackson StreamWriteConstraints`
  (Jackson 2.13 vs 2.18, stream S) and the embedded node (stream T).

## Log
- 2026-11-22: reset to 76a4bd12e; 4 commits; rebased onto the bridge (1f81c86b9), conflicts in the (d) files resolved
  in favour of the bridge.
