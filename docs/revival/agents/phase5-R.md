# Phase 5.3 (agent R): mapping-type removal

## Goal
Zero category (c) compile errors on OpenSearch 2.19.7: untyped templates/mappings, untyped loaders/search,
explicit kind field in the system index. Update the structural mapping IT. Same query results.

## Tasks
- [ ] local-only compile scaffolding (stubs + Q-style imports/wrappers) kept as a patch, never committed
- [ ] mapping factory + ESPutIndexTemplateRequestBuilder: untyped templates
- [ ] loaders (IndexProviderBasedGraphLoader, LoadUtils, dv-stat ElasticDataPopulator, EsUtil)
- [ ] system index: kind field (PersistentResourceStore, PersistantNodeStatusResource, SnifferFuseClient)
- [ ] unipop QueryBuilder idsQuery/typeQuery
- [ ] MappingElasticConfigurer x2
- [ ] tests: OpensearchIndexProviderMappingFactoryIT, CSVLoaderIT, unipop tests, StatTestUtil

## Status
Started. Next: read code.

## Discoveries

## Log
- reset to origin/phase5-opensearch-2.19 (76a4bd12e)
