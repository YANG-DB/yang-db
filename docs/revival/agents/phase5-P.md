# Phase 5.1 — agent P: remote-client bridge (RestBridgeClient)

## Goal
New module `fuse-opensearch-client`: `RestBridgeClient extends AbstractClient` delegating to the RHLC (1.3.0 today,
2.x-compatible); `ClientFactory.remote(hosts, settings)`; swap the 4 remote construction sites + the test-framework
reuse-cluster path. Unit tests per action, remote IT over HTTP.

## Tasks
- [x] action census (grep)
- [x] module + RestBridgeClient + ClientFactory + README (commit 58eb55b0d; module tests 44/0)
- [x] unit tests per action
- [x] remote IT (node on 9360/9460, bridge only) 8/8
- [x] swap sites (3a95dd786): dv-core ClientProvider, epb ElasticClientProvider, dv-stat ClientProvider, SnifferFuseClient, test-framework reuse path
- [ ] fuse-dv-core suite green; compile all

## Status
Next step: DONE. Final report sent. Follow-ups belong to 5.5/5.6 (fuse-service ElasticEmbeddedNode still builds a TransportClient; post-bump list in fuse-opensearch-client/README.md).

## Discoveries
- No `opensearch-rest-client-sniffer` artifact in ~/.m2 (offline) -> sniffer mapping not practical offline.
- RHLC has no cluster-state or indices-stats API: cluster state is used by fuse-dv-stat EsUtil (isIndexExists, getMappingsOfIndex); indices stats by EsUtil.getAllIndices (epb test only).
- fuse-service ElasticEmbeddedNode (main) also builds a PreBuiltTransportClient (embedded service mode) - not in 5.1 scope.

## Log
- reset to origin/phase5-opensearch-2.19 (09b1c8699)
- 58eb55b0d module commit. Swaps edited: dv-core ClientProvider (+ OpensearchGraphConfiguration.clusterHttpPort, ExecutorModule opensearch.http.port), epb ElasticClientProvider (+StatConfig opensearch.stat.http.port), stat ClientProvider (es/statistics.client.http.port), SnifferFuseClient, test-framework reuse path. Whole reactor test-compiles.
- dv-core -am on 9360/9460: BUILD SUCCESS. opensearch-client 44/0, model 27/0, core 18/0 (4 skip), dv-unipop 152/0 (18 skip), dv-core 79/0 (4 skip) incl. ClientProviderBridgeIT 2/2.
- 3a95dd786 swaps committed; stat ClientProviderTest 2/2 standalone.
- Reuse path: dv-core TestSuiteIndexProviderSuite vs separate-JVM node on 9360 (scratchpad StandaloneNode) -> 3 errors: indices:monitor/settings/get unsupported (loud, as designed). Added GetSettingsAction -> 687dd7999; rerun 34/34.
- Final: dv-core -am BUILD SUCCESS (client 45/0, model 27/0, core 18/0, dv-unipop 152/0, dv-core 79/0); whole reactor test-compiles.
