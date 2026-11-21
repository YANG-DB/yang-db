# Stream F — OpensearchIndexProviderMappingFactoryIT structural assertions (Phase 3)

## Goal
Re-enable the 4 `@Ignore`d tests in `fuse-dv/fuse-dv-core/src/test/java/com/yangdb/fuse/executor/opensearch/OpensearchIndexProviderMappingFactoryIT.java`
(stale pre-type-removal JSON fixtures) by rewriting golden-JSON comparisons as structural assertions
(parsed mappings/templates; expectations derived from ontology + index-provider config). Clean up templates/indices after.

## Tasks
- [x] Probe real cluster output for nested / embedded / single providers
- [x] Rewrite testGenerateNestedMapping, testGenerateEmbeddedMapping, createNestedIndicesTest, createEmbeddedIndicesTest
- [x] Convert testGenerateSingleMapping too; createSingleIndicesTest also re-enabled (passes structurally)
- [x] @Before/@After cleanup of templates + indices
- [x] Mutation check: assertions fail when expected types are changed; both bug tests fail when un-ignored
- [x] Full fuse-dv-core run green, committed

## Status
DONE. fuse-dv-core: 77 tests, 0 failures, 4 skipped; TestSuiteIndexProviderSuite (ITs) 32 run, 0 failures, 3 skipped.
Next step: coordinator cherry-picks 82cb0f5bb; product bugs 1+2 below need a separate fix stream.

## Discoveries
- OpenSearch 1.3 legacy templates (transport `PutIndexTemplateRequest.mapping(type, map)`) STILL store mappings
  type-wrapped (`{"Person":{"properties":...}}`). Only concrete indices are typeless. The old FIXME's premise was
  only half true; the real fixture drift was settings (`index.sort.field=id`/`asc` on every static/time element without
  nested docs) and the shipped ontology.
- Real product bug 1 (UNIFIED relations): single-index provider never maps `direction`/`entityA`/`entityB`
  (+ redundant fields) — relations go through `populateProperty` once entities created the mapping.
  Kept as `@Ignore`d `testGenerateSingleMappingMapsRelationSides`.
- Real product bug 2 (UNIFIED patterns): "ontology" template patterns include bare type names (dragon, horse, own,
  person, fire...), so it merges into any per-type index on the same cluster (the stream-B suite trap).
  Kept as `@Ignore`d `testGenerateSingleMappingPatternsOnlyMatchUnifiedIndex`.
- Latent: `mapProjection` calls `this.indexProvider.getRelation(..).withMapping(CHILD)` — mutates the caller's
  IndexProvider relations (mapping field). Not asserted; harmless for current consumers.
- Ontology property `profession` (type Profession) is not mapped; the nested doc is mapped under key `Profession`
  (provider nested type). Possible field-name mismatch with loaders — not investigated (out of scope).

## Commits
- 82cb0f5bb Phase 3: assert index mappings structurally and re-enable OpensearchIndexProviderMappingFactoryIT

## Log
- worktree at e48e7ce09
- probe run dumped real templates/index mappings (scratchpad probe.txt)
- rewrite passes on first run; mutation run 8 expected failures; restored
