# Phase 5.4 (stream S): dependency alignment for OpenSearch 2.19.7

## Goal
One version each of netty, Jackson, log4j, antlr4-runtime and Lucene across the build, matching what OpenSearch
2.19.7 declares; resolve the ANTLR conflict between Painless (4.13.1) and graphql-java (4.7.2); commons-codec (census f).

## Tasks
- [x] netty-bom 4.1.139.Final (tcnative 2.0.84); A1 pin replaced, jooby-netty exclusions removed
- [x] jackson-bom 2.18.9 (+ 2 forced code fixes)
- [x] log4j-bom 2.25.5
- [x] commons-codec 1.18.0 (dv-stat, dv-epb)
- [x] extra: snakeyaml 2.1, httpclient 4.5.14 / httpcore(-nio,-osgi) 4.4.16 / httpasyncclient 4.1.5
- [x] graphql-java 24.3 (shades ANTLR) + antlr4-runtime 4.13.1; extensions/graphql adapted
- [x] DistributionGraphQLTest ANTLR checks updated
- [x] RegistrarsUtilsContentNegotiationTest (A1 guard): passed on a main-based (OS 1.3) copy with these deps; to be
      re-run on the 2.19 branch once fuse-service compiles

## Status
DONE. Commits `dcb56cb92`, `98bff4a32` on top of `76a4bd12e`. Next (coordinator): after Q/R/P land, run the A1 test,
extensions/graphql (37) and DistributionGraphQLTest on the 2.19 branch; T verifies Painless loads on 4.13.1.

## Discoveries
- graphql-java >= 20 shades ANTLR into `graphql.org.antlr` (checked 22.3, 24.3, 26.2: 197 relocated classes, Java 11
  bytecode). Upgrading it removes GraphQL from the antlr4-runtime contest; no shading of our own needed.
- Jackson 2.18 rejects duplicate enum `@JsonProperty` names (`CountConstraintOp.between`/`within` both "within"; between
  now serializes as "between") and removed `DeserializationContext.mappingException` (fuse-geojson). The census missed
  these because it kept Jackson 2.13. Other modules may hide more Jackson 2.18 behaviour changes: they can't compile yet.
- snakeyaml was 1.15 (tinkerpop) / 1.19 (jooby-apitool) / 2.1 per module; jackson-dataformat-yaml 2.18 needs 2.x.
- `netty-tcnative-boringssl-static` 2.0.84 depends on all 5 platform classifier jars (bigger distribution; harmless).
- graphql-java API changes: ExecutionPath -> ResultPath; scalar/enum-value constructors -> builders; GraphQLLong etc.
  removed (local "Long" scalar); `enforceSchemaDirectives` removed; `getAllTypesAsList()` is immutable.

## Before / after (`mvn -B -Pcore,dragons-assembly dependency:tree`, distinct versions across the reactor)
| artifact | before | after |
|---|---|---|
| netty-buffer, -common | 4.1.73 + 4.1.139 | 4.1.139.Final |
| netty-codec-http2, -transport-native-epoll | 4.1.43 | 4.1.139.Final |
| other netty | 4.1.139 | 4.1.139.Final |
| netty-tcnative-boringssl-static | 2.0.25 | 2.0.84.Final |
| jackson-core / annotations / databind / csv | 2.13.4 / 2.13.4 / 2.13.4.2 / 2.13.4 | 2.18.9 |
| jackson cbor / smile / yaml | 2.18.9 / 2.18.9 / 2.18.9 + 2.9.8 | 2.18.9 |
| jackson jdk8 / jsr310 / afterburner / param-names | 2.10.1 (+2.9.8) | 2.18.9 |
| log4j-api / core / jul | 2.17.1 / 2.17.1 / 2.25.5 | 2.25.5 |
| antlr4-runtime | 4.7.2 | 4.13.1 |
| graphql-java | 2020-01-17T02-41-10-578985f | 24.3 |
| lucene-* | 9.12.3 | 9.12.3 |
| commons-codec | 1.9, 1.11, 1.18.0 | 1.18.0 |
| snakeyaml | 1.15, 1.19, 2.1 | 2.1 |
| httpcore / httpcore-nio | 4.4.4, 4.4.14, 4.4.16 | 4.4.16 |

Remaining multi-version artifacts (not OpenSearch-related, untouched): httpmime 4.5.1/4.5.13, groovy 2.4.6/2.4.11,
javassist, jsr305, commons-beanutils/-collections/-lang/-logging, reactive-streams 1.0.2/1.0.4, scala-xml.

## Log
- Baseline tree captured; poms aligned; online resolve; `mvn -o validate` OK.
- 2.19 branch: unipop-core, fuse-model (27/27), fuse-extension-api, fuse-geojson compile and pass.
- GraphQL validated on a temporary branch from origin/main (OS 1.3, everything compiles) with the same changes plus
  the alignment commit: RegistrarsUtilsContentNegotiationTest 7/7, extensions/graphql 37/37, DistributionGraphQLTest
  4/4; fuse-service netty tree uniform 4.1.139. Temp branch deleted.
