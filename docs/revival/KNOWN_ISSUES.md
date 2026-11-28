# Known Issues

Confirmed defects found while reviving the project (2026-11). Found by loading the Dragons tutorial data through the
REST API and running every example query in the docs, with expected results computed from what is actually stored
in OpenSearch. Status is tracked in the [ROADMAP](ROADMAP.md) checklist.

## Loader

### L1 — Relation endpoint types rewritten · **fixed**
`CSVTransformer.populatePropertyFields` picked the ontology pair by destination type only, then looped over every match,
so the last pair's source type won. All `OriginatedIn` edges were stored as Person→Kingdom (CSV: Dragon/Horse→Kingdom)
and all `RegisteredIn` edges as Horse→Kingdom (CSV: Guild→Kingdom). Now matches source *and* destination type; a row
with no matching pair is reported as a failure.

### L2 — Pre-1970 epoch-millis dates dropped · **fixed**
`DataLoaderUtils.parseValue/validateValue` had no epoch-millis parser; positive values happened to parse via a fallback,
negative ones (before 1970) failed. Property values that fail validation are dropped silently (69/100 tutorial
persons lost `birthDate`); for time-partitioned relations (Fire, Freeze) the failure aborted the whole file.

### L3 — `INSERT` silently overwrites · **fixed**
The `directive` parameter was accepted but ignored: every write used `OpType.INDEX`. Duplicate ids (e.g. Person 367 owns
Dragon 10 *and* Horse 10 → both edges `10_367`) silently overwrote each other while reporting success. `INSERT` now uses
`OpType.CREATE` (duplicates reported as failed items); `UPSERT` overwrites.

### L4 — Time-partition bucket choice · **fixed** (range buckets: each value is the inclusive upper bound)
All Fire/Freeze documents (years 1900–1999) land in `idx_*_1000`; the other three partitions stay empty. Root cause:
a date is routed to the index named exactly after its formatted value (1950 → `idx_fire_1950`, which doesn't exist),
then falls back to the first index in text order (`_1000`). Queries use the same exact-name rule. The Dragons config
and IndexProvider.md describe range buckets that nothing implements. The pattern `YYYY` is week-year (should be `yyyy`).

### L5 — A large CSV load fails as a whole: the loader sends the entire file as one bulk request · open
`LoadUtils.load` puts every row of the posted file into one `BulkRequestBuilder`. OpenSearch rejects a bulk that big
(`rejected_execution_exception ... coordinating operation`, indexing pressure: by default 10% of the heap), and the
whole file fails. Found while loading 10 copies of the tutorial data for the benchmark (`docs/benchmark.md`): Fire,
115,030 rows (230,060 documents), fails against the quickstart's 512 MB OpenSearch heap. Workaround: post the file in
pieces (`docker/e2e/load_tutorial.py` `chunk_rows`, which `docker/bench/bench.py` uses: 20,000 rows). Fix: submit the
bulk in batches (by count or bytes) inside the loader.

## Query engine

### Q1 — Undirected pattern executes as directed · **fixed**
`(a)-[r]-(b)` behaves exactly like `(a)-[r]->(b)`. Tutorial query 2 returns 245 of 349 correct relationships.

### Q2 — Relationship endpoint type ignored · **fixed**
Endpoints are matched by id only. Person 42 owns Horse 7 → returned as Dragon 7. Undirected is worse: Person 42 gets
21 "dragons" (the owners of Dragon 42, via the reverse-direction documents).

### Q3 — Edge between two entities with the same id is dropped · **fixed**
`Person:22 → Dragon:22` is stored correctly but never returned.

### Q4 — Relationship ids built without a delimiter · **fixed**
`rType + idA + idB` (e.g. `Fire4465`) collides for different pairs (4→465 and 44→65), so `distinct` loses pairs. All
relationships between the same two entities also collapse into one even without `distinct`, contradicting
query-the-data.md.

### Q5 — Untyped node / label union unsupported · **fixed**
`match (p) return *` returns nothing; `match (p:Person | Dragon)` is a syntax error. Both appear in Queries.md.
Fix: undirected steps use each stored document's own direction and endpoint types; relationship ids are
`<label>:<csv id>`; results key entities by type+id; untyped match skips nested types' nonexistent indices.
Label union: **decided unsupported** (openCypher 9 has no node-label union) — now rejected with an explicit error
suggesting `WHERE p:A OR p:B`. Untyped match: the ASG is correct; the loss is in execution (stream B).

### Q6 — Cycle-closing pattern rejected · **unsupported, explicit error** (docs updated)
Queries.md's "owners who know each other" pattern is rejected by the ontology validator with "Relation with No entity
Attached", even when rewritten as directed. Fix or document as unsupported.

### Q7 — Count filter returns nothing · **fixed** (planner reversed the counted relation)
`Match (p1:Person)-[k:Know]->(p2:Person) WHERE count(k) > 15 return *` returns no results; the stored data has 9
persons with more than 15 outgoing `Know` relationships.

### Q8 — `WHERE p:A OR p:B` on an untyped node crashes · **fixed**
Two causes: the M2 rewrite cast untyped nodes (stream A), and the entity-properties validator treated an untyped
node with no listed types as having no fields and ignored metadata like `type` (fixed in `5cb0117d2`). Verified in
e2e run 4.
`match (p) where p:Person OR p:Dragon return *` → HTTP 500 `EUntyped cannot be cast to ETyped`. This is the
documented replacement for the unsupported label union.

### End-to-end status of Q1–Q4 (2026-11-20)
The first fixes held at traversal level but not through the REST service. The service's planner and cursors take a
different path, which was fixed in later rounds:
- the plan validator rejected an undirected relation walked from its far end
- the entity-filter translation dropped the target's type
- a caching schema provider returned empty lists for one direction
- the forward-paths cursor, the distinct step and unipop's vertex step all keyed by bare id

Checked live on the tutorial data: Q2a 475, Q2 349, Q3 30, Q5 2,437, `match (p)` 1,623, QE 96, QF 7/1/7, all as
expected. Confirmed by e2e run 4 on a freshly rebuilt image (all 21 checks pass).

### Known limitations (not planned)
- Two count filters whose valid start nodes don't overlap get "No valid plan was found".
- An undirected pattern written from a relation's target side (e.g. `(k:Kingdom)-[:RegisteredIn]-(g:Guild)`) is
  rejected by the ontology validator.
- `match (p)` returns at most 1,000 results regardless of `pageSize`.
- The projection cursor projects nothing for a count-filtered query (`WHERE count(k) > n`); plain queries project
  normally (found 2026-11-20 while reviewing the tutorial).

### Q14 — Stored queries were never removed; large ones filled the heap · **fixed** (0.10, Phase 7.7)
Every query is kept in `InMemoryResourceStore`, with its cursor and fetched pages, so that clients can fetch pages
again. Each query has a ttl (`CreateQueryRequest.ttl`, default 5 minutes), but nothing enforced it: the removal method
(`removeOldQueries`) was never called. An earlier version of this entry said queries were removed after the ttl; that
was wrong. A client that doesn't `DELETE` its queries kept every one in memory for good: about 54 MB per stored Q9
at 10× the tutorial data (measured), so a few dozen ran the 1 GB heap out. The store's map was also a plain
`HashMap` shared by concurrent request threads. Fixed: a query is removed once it hasn't been used for longer than
its ttl (using the query, a cursor or a page, or its composite query, counts as use; ttl 0 or less: never), checked
every 30 seconds in the background; the maps are concurrent. Checked live: a query with a 10-second ttl was gone
after 25 seconds; three Q9 queries from `/run` (default ttl, never deleted) took the heap from 33 to 200 MB and were all removed after 316 s, heap back to 34 MB.
`InMemoryResourceStoreTest`.

### Q15 — Cypher `RETURN` doesn't select elements or properties · open (feature, ROADMAP 7.11)
The engine always returns the whole matched pattern with every ontology property of each element, whatever the
`RETURN` clause lists: `MATCH (p:Person)-[o:Own]->(d:Dragon) RETURN d.name` returns the persons and the `Own`
relationships too, and every property of each dragon. The OpenSearch requests match: they fetch every property of
every element (`DefaultSelectionAsgStrategy` projects them all into `_source.includes`). Found 2026-11-26 from the
search slow log while checking Phase 7.2. GraphQL is not affected: it fetches and returns the selected fields.

### Q16 — V1: `some` (OR) between filters on the same property behaved as AND · **fixed** (0.10)
Found while writing the V1 spec (2026-11-26): `height < 165` OR `height > 182` on Person (8 + 9 = 17 persons) returned 0
as an `EPropGroup`/`Quant1`/`RelPropGroup`/`HQuant` with `some`. Cause (2026-11-28): the OR reached OpenSearch intact as
a `bool` `should`, but the query builder merged the two conditions on `height` into one range clause, as it does for
AND, giving "between 182 and 165", which matches nothing. `CompareQueryTranslator` names each range after its field and
`QueryBuilder.range` reused a same-named range in any context. Fixed: ranges are shared only in AND contexts (`must`,
`filter`); in `should` and `must_not` (which had the same flaw: two exclusions became the negation of their AND) each
condition is its own clause. OR between different properties was never affected. Checked live: 17 persons;
`RangeMergeTest`. Every playground query's response is byte-for-byte unchanged.

### Q17 — V1: `WhereByConstraint` without `joinType` returned an empty response · **fixed** (0.10)
`/run` answered 200 with an empty `data` object (a `NullPointerException` at `AsgNamedParametersStrategy.isForEachJoin`):
the JSON reader left `joinType` null, and only the Java constructors set it. Fixed: a missing `joinType` follows the
operator, as the constructors do (`FOR_EACH` for a single-value operator, `FULL` otherwise). The V1 schema still
requires it. `WhereByConstraintJsonTest`; checked live. Still open: an exception during a run can come back as 200 with
an empty body instead of an error.

### Q18 — `/fuse/query/v1/validate` validates before rewriting, unlike `/run` · open
`validateAndRewriteQuery` transforms and validates but skips the rewrite strategies that `/run` applies first. A query
with a `RelUntyped` runs, but `/validate` answers 200 with an empty body: a validator throws
`NullPointerException` (`CompositeValidatorStrategy.java:55`) on the not-yet-inferred relation type. The same empty
200 answers an unknown relation type, an unknown ontology, a where-by on a missing tag and a non-numeric value for a
numeric property; `/run` reports each with an error (some unclear: `No value present` for the missing tag). The other
way round, `/validate` accepts a where-by on an unknown field and reports two adjacent entities that `/run` lets through
to the planner (`No valid plan was found`). Examples in [docs/spec/v1/rules.md](../spec/v1/rules.md).

### Q20 — V1: a cycle crashes instead of being reported · open
A query whose `next` points back to an element on its path (`A -Know-> B -Know-> A`, by eNum) never reaches
`AsgCycleValidatorStrategy`: `/run` answers `failed building the cursor request forwardPaths:
java.lang.StackOverflowError`, `/validate` answers HTTP 500. The ASG builder follows `next` recursively before
anything checks for cycles. Example in [docs/spec/v1/rules.md](../spec/v1/rules.md) (rule 3).

### Q19 — V1: `RelUntyped` with no `vTypes` drops the relation · open
`Person -[RelUntyped, no vTypes]-> EUntyped` (meaning "any relation") returns the 100 persons and no relationships or
far-side entities. With `vTypes` listed it works (`["Own"]` matches `Rel Own`; `["Own","MemberOf"]` returns both). The
V1 schema requires a non-empty `vTypes`.

## Found by the legacy-test triage (Phase 3, stream I; see [IGNORED_TESTS.md](IGNORED_TESTS.md))

### Q9 — Cypher keeps only the first type of a multi-type relationship · **now an explicit error**
`[r:A|B]` translates to a relation of type `A` only (`CypherMatchSimpleTranslatorTest:86`).

### Q10 — Cypher relationship property maps: unnamed relationships crash, extra properties ignored · **fixed**
Verified live (e2e R4–R6): a named relationship with one property works. An **unnamed** one (`-[:Fire {t: 1}]->`)
fails with HTTP 500 `None.get`, and with **several** properties only the first is applied.

### Q12 — Cypher node property maps: unnamed nodes crash, extra properties ignored · **fixed**
`(p:Person {gender: FEMALE, name: 'X'})` applies only the first property; `(:Person {name: 'X'})-[…]` fails with HTTP
500 `None.get`. The node-pattern sibling of Q10 (found by the bug-fix round's agent K). Pinned by e2e R7/R8.

### Q11 — Relationship property plus an end-pattern filter fails validation · **fixed** (bug-fix round 2)
Fixed: `RelationPatternRangeAsgStrategy` attached the end filter to the quant after the *next* hop's `Rel`; it now uses only a quant directly after the end entity. The ignored range test runs again.
"No Parent Element found" (`RelationRangeAsgStrategyWithFiltersInRangTest:318`).

### P2 — Range operators on enumerated values crash planner statistics · **fixed** (bug-fix round 2)
Fixed: enum statistics are term histograms keyed by name, but the provider compared the ontology `Value` object; enum values (and inSet/inRange lists) are now compared by name. `SmartEpbRedundancyTests#redundantPropertyPathSelectionTest` runs again.
`ClassCastException: ontology.Value cannot be cast to Comparable` (`SmartEpbRedundancyTests:262`).

### M1 — Single-index provider never maps relationship fields · **fixed** (bug-fix round 2)
Fixed: a unified index now maps each relation's `direction`, `entityA`/`entityB` and redundant side fields (`populateRelationSides`); redundant fields of several relations are merged, not replaced. The ignored IT runs again.
`direction`, `entityA`/`entityB` and redundant fields fall to dynamic mapping. Pinned by the ignored test
`OpensearchIndexProviderMappingFactoryIT#testGenerateSingleMappingMapsRelationSides` (Phase 3, stream F).

### M2 — Single-index provider's "ontology" template matches every bare type-named index · **fixed** (bug-fix round 2)
Fixed: unified templates match only `<index>*`, not every type's bare name. The ignored IT runs again.
Its index patterns include every type name (`dragon`, `horse`, `own`…), so it merges into unrelated indices with
those names. This is the "leftover templates break later index creation" trap. Pinned by
`OpensearchIndexProviderMappingFactoryIT#testGenerateSingleMappingPatternsOnlyMatchUnifiedIndex`.

## GraphQL (found while preparing the Phase 4 baseline)

### G1 — GraphQL queries fail on ontologies registered from JSON (e.g. Dragons) · **fixed**
`GET /fuse/query/graphQL/run?ontology=Dragons&graphQL={ Dragon { name } }` returns `A schema MUST have a 'query'
operation defined`: the GraphQL schema generated from the ontology has no `Query` root type.
Cause: every query ran against one global type registry, the last SDL schema imported (empty for JSON ontologies).
Fixed (Phase 4, stage B): queries run against a schema generated from their ontology (`OntologyToGraphQLSdl`), with a
root field per entity type named like the type with a lower-case first letter: `{ dragon { name color } }`. Pinned by
`GraphQLDragonsQueryTest`, `GraphQLRoutesTest` and e2e GQ1–GQ3.

### G2 — GraphQL schema import keeps state across requests · **fixed**
A second `POST /fuse/graphql/ontology/{id}` with the same SDL fails with "tried to redefine existing 'Query' type …
There is already a schema defined". Only a service restart clears it.
Fixed: each import parses into a new registry and schema (`GraphQLSchemaImportTest`, `GraphQLRoutesTest`).

### G3 — GraphQL schema import can't take a schema split across files · **fixed**
`starWars.graphql` needs the `WhereClause` types from `whereSchema.graphql`. The endpoint takes one document; on its
own it fails ("input type 'WhereClause' is not present"), and the two concatenated returned an empty response.
Through REST, GraphQL is therefore not usable end to end today. Only the in-process unit tests (fuse-core
`GraphQL*Test`, fuse-asg) exercise it successfully.
Fixed: the where-clause types are added when a schema defines none of them, concatenated documents import as one,
and an invalid schema answers 400 with the GraphQL errors. GraphQL now lives in the `extensions/graphql` extension.

### A4 — Dragons clause routes are broken once registered · **fixed** (bug-fix round 2)
Fixed: the clause routes, the clause code in `ExtensionQueryDriver`, the dead `/ext` redirect and the duplicate `cypher/logical/run` / `query/v1/logical/run` routes are removed (maintainer's choice: the clause format belonged to the deleted Knowledge extension). The Dragons extension now contributes only the playground.
Now that Phase 4 registers them, the clause routes fail (per code reading): no `clause` language is registered,
`createClause` is never called and its transformer is null, and `/clause/run` sends JSON to `runCypher`. `/ext`
redirects to a file that doesn't exist.

### A5 — The SPARQL language key is `sparkQL` · **fixed** (bug-fix round 2)
Fixed: the language is registered as `sparql` (`TYPE_SPARQL`), with `sparkQL` kept as an alias so existing clients keep working. (The `/fuse/query/sparql/run` URL already answered through the older fixed route; the missing piece was the language name.)
The generic route `/fuse/query/sparql/run` returns 404 because the registered key is `sparkQL`; the old `/sparql` URL
works. Register `sparql` as the key (or an alias).

### A6 — The id generator hands out the same range every time · **fixed** (bug-fix round 2)
Fixed: the counter document's id is the generator name, written with `if_seq_no`/`if_primary_term` and retried on a version conflict, so ranges are consecutive and never overlap, even with concurrent callers; `init` no longer resets a counter. New `BasicIdGeneratorIT`.
`BasicIdGenerator.getNextInternal` writes the new counter with `new IndexRequest(getResponse.getId()).index(...)`.
The document id ends up as the *index name* argument, and `.id(genName)` is never set, so the counter document never
moves past 1. Confirmed live: two calls to `GET /fuse/idgen?id=demo&numIds=10` both returned `1–11`. The loaders
don't call it (loaded ids come from the data), but `SnowflakeRequestIdSupplier` uses it for its worker id. Found while
writing the architecture tutorial.

### A7 — Snowflake worker id uses XOR instead of a power · **fixed** (bug-fix round 2)
Fixed: `% (1L << 6)`. New `SnowflakeRequestIdSupplierTest`.
`SnowflakeRequestIdSupplier` computes `workerIdRange.getLower() % (2 ^ 6)`. In Java `^` is XOR, so this is `% 4`, not
`% 64`. Combined with A6, every node gets worker id 1.

### A8 — The `elastic/view` links answer 404 · **fixed** (bug-fix round 2)
Fixed: the service never keeps a query's OpenSearch requests, so there is nothing to show: the `elastic/view` routes and `elasticQueryUrl` are removed. Use the search slow log (architecture tutorial).
`/fuse/query/:id/elastic/view` and `…/page/:pageId/elastic/view` redirect to a viewer that loads `/fuse/query/:id/elastic`
and `…/page/:pageId/elastic`, and neither route exists. The query resource also advertises an `elasticQueryUrl` that
returns 404. To see the OpenSearch requests, use the search slow log (see the
[architecture tutorial](../tutorial/architecture/query-lifecycle.md#watch-it-yourself)).

### A9 — `POST /fuse/query/v1/run` answers 404 · **fixed** (bug-fix round 2)
Fixed: the handler sends its own response, like `/cypher/run`. New `QueryRoutesTest`.
`QueryControllerRegistrar.v1Context` registers `/v1/run` as `(req, res) -> API.runV1(...)` and discards the `Result`
it returns. The query runs, but Jooby then falls through to the generic `/fuse/query/:lang/run` route, which answers
`404 No query language 'v1'`. Confirmed live with a valid V1 query. `POST /fuse/query/v1` (create) and the
`/v1/plan`, `/v1/traversal` and `/v1/validate` routes work. Found during the documentation review.

### A10 — `POST /fuse/query/cypher` needs `"type": "cypher"` in the body · **fixed** (bug-fix round 2)
Fixed: `/cypher` and `/sparql` take the language from the URL.
The route passes no language to `postJsonQuery`, so a body without `"type"` fails with
`No matching json query translator found for type null`. The language is already known from the URL. The
`/fuse/query/cypher/run` route and the generic `/fuse/query/:lang` routes don't have this problem.

### T3 — The index-provider test suite wipes a running OpenSearch · **fixed** (bug-fix round 2)
Fixed: `TestSuiteIndexProviderSuite` reused any OpenSearch answering on the test port (9200 by default) and its tests
drop and recreate indices, so running it next to the Docker quickstart deleted the Dragons data (it happened during the
round). It now refuses with a clear message unless `-Dfuse.test.reuse_cluster=true`; run ITs on free ports with
`-Dfuse.test.http.port` / `-Dfuse.test.transport.port`.

### B1 — Tests fail when run against the packaged `fuse-core` jar · **fixed**
Fixed: `fuse-core`'s main jar is the plain module jar again; the self-contained jar (for `FuseClient` in external apps)
is an extra artifact, `fuse-core-<version>-all.jar`. Cause: the shaded jar replaced the main artifact and bundled
log4j-api, a multi-release jar, without `Multi-Release: true` in its manifest. Java 11 then ignored log4j's
`META-INF/versions/9` classes and used the Java 8 `StackLocator`, so `LogManager.getLogger()` failed ("No class provided,
and an appropriate one cannot be found") in OpenSearch's `SearchRequestContext`, and the fuse-dv-unipop tests errored
(84 errors in `mvn install -pl fuse-dv/fuse-dv-unipop -am` on main; 0 after). The `-all` jar now sets
`Multi-Release: true` too. Full `mvn clean install -P core,dragons-assembly` with tests: 1,316 tests, 0 failures. Side
effect: the distribution no longer carries the 86 MB fat jar next to the jars it duplicated (the tar.gz goes from 238 MB
to 166 MB).

### G4 — GraphQL exposes only the first target type of a multi-target relationship · **fixed** (bug-fix round 2)
Fixed: a relationship to several types becomes an abstract GraphQL type `<sideA>_<rel>` (an interface with the shared fields, or a union); `... on Dragon` narrows it, and a query over several types translates to an untyped end with those types. Behaviour change: `{ person { Own { name } } }` now returns dragons and horses; a field only one type has needs a fragment. New GQ4 example.
`Own` (Person → Dragon, Horse) shows only one target type in the generated GraphQL schema (Phase 4 stage B).

### G5 — Behaviour change: GraphQL queries always use the schema generated from the ontology · note
Since G1's fix, a query runs against the schema generated from its ontology. Custom root fields of an imported SDL
(e.g. StarWars `hero`) are no longer available; the per-entity-type roots (`human`, `droid`) are. Revisit if anyone
relies on custom roots.

### Q13 — `QueryToAsgTransformer` keeps state across queries · **fixed** (bug-fix round 2)
Fixed: the element map is created per call, and the ASG gets a copy. It was a leak for every language (the transformer is an eager singleton), not only GraphQL. New `QueryToAsgTransformerTest`.
It kept every element it ever transformed, so one query's elements leaked into the next. The GraphQL translator now
creates a fresh one per query (Phase 4); other users of the class were not checked.

## Count filter

### C1 — Count-filter script uses the wrong operators for `=` and `>=` · **fixed**
`AggregationBuilder.BuildFilterScript` emits `a=120` for `eq`, which is an *assignment* in Painless rather than a
comparison, and `a>120` for `gte`. So `WHERE count(r) = n` and `>= n` don't filter as written. `>` and `<` (used by
the docs) are correct. A second cause, for `=` / `<>`: the Cypher translator routed `count(k) = n` to plain
equality, so no count filter was built at all (fixed: `CountExpression` now owns `=`/`<>` on `count(...)`). Fixed: `==`, `>=`, and `!=` (which produced an uncompilable `a120`). An unknown operator now
throws. Pinned by e2e R1/R2.

### C3 — Two count conditions on the same relationship give wrong results · **fixed** (bug-fix round 2)
Fixed: each count condition nested a new, never-built count filter inside the first; conditions on the same field are now combined in one filter script (`a>15&&a<17`). `OR` works too: the translator splits it into branches, each with its own condition. Checked live: AND 4 persons, OR 48, matching the data. e2e R9/R10.
`WHERE count(k) > 15 AND count(k) < 17` returns 99 persons and 358 relationships; the data has 4 start persons with
exactly 16 (found live during the bug-fix round). A single count condition works. Not investigated yet.

### C2 — Count filter may only consider the top 10 groups · **not a bug**
The service runs the count filter as its own request sized to the vertex bulk (`DiscreteVertexController.filterByAggregation`
→ `setSize(bulkSize)`, applied to top-level terms aggregations by `SearchBuilder.enforceSize`). Pinned by
`testCountFilterTermsSizeIsTheBulkSizeNotTheDefault` and e2e R3 (36 of 36).

### T2 — Test-code slips found in Phase 3 (minor) · **fixed** (except the duplicated Distinct test, kept)
- `QueryTest.testDeSerialization`: the "q11" block reads Q005.json, so Q011.json is never tested; the q2 block
  null-checks `q1Obj` instead of `q2Obj`.
- `DistinctFilterQueryTranslatorTest` is a copy of the count-filter test; no distinct translator exists.
- The `EpbJoinSelectionTests` mock compares `direction.name()` (a String) with an enum, which is always false.

## Planner statistics

### P1 — Statistics treat a time-field `inSet` as an intersection · **fixed**
`EBaseStatisticsProvider.findRelevantTimeSeriesIndices` turns `inSet` into one `eq` per value, and each `eq` drops
every other partition index. So values that fall in different time buckets leave no index at all, where it should
be a union. This only affects cost estimates. Found by streams E and G; not covered by a test yet.

## API

### A3 — Dragons extension routes are never registered · **fixed** (Phase 4)
`FuseApp` only discovered `AppControllerRegistrarBase` subclasses under `com.yangdb.fuse.services`, so the Dragons
extension's `DragonsExtensionRegistrar` (`com.yangdb.dragons.services`) was never registered. Now the Dragons jar
declares `com.yangdb.dragons.DragonsExtension` in `META-INF/services/com.yangdb.fuse.extension.YangDbExtension`, which
registers it and the playground (moved to `com.yangdb.dragons.playground`). `DragonsExtensionTest` checks every route
is in the service's route table exactly once. What those routes do once reachable is A4.

### A4 — Dragons extension query routes: clause routes broken, `/ext` dead · **fixed** (bug-fix round 2)
Fixed: the clause routes, the clause code in `ExtensionQueryDriver`, the dead `/ext` redirect and the duplicate `cypher/logical/run` / `query/v1/logical/run` routes are removed (maintainer's choice: the clause format belonged to the deleted Knowledge extension). The Dragons extension now contributes only the playground.
Found when A3 made them reachable (static reading, not run against the stack):
- `POST /fuse/query/clause` passes the body's `queryType` (`clause`) to the standard create path; no `clause` language
  is registered, so it fails with "No Query translator found". `ExtensionQueryDriver.createClause` handles `clause` but
  nothing calls it, and its `transformer` field is never set (would NPE).
- `POST /fuse/query/clause/run` sends `{"clause": ...}` JSON to `runCypher`, which parses it as cypher.
- `GET /ext` redirects to `/public/assets/swagger/swagger-ext.json`, which doesn't exist.
- `GET /fuse/query/cypher/logical/run` and `POST /fuse/query/query/v1/logical/run` reuse the core run handlers and
  behave like `/cypher/run` and `/v1/run` (which is itself broken, A9).
A likely fix: register `clause` as a language (`QueryLanguages`, from `DragonsExtension.modules()`) whose transformer
chains clause → V1 `Query` → `AsgQuery`; the generic `/fuse/query/clause(/run)` routes then replace the special ones.

### A11 — After the clock steps back, every request fails for as long as the step · **fixed** (0.10)
Every request gets an id from `SnowflakeRequestIdSupplier`. It used the `com.github.cnsvili:snowflake` 1.0.1 library,
which refuses to issue ids while the clock is behind its last timestamp, so every request, health checks included,
failed with HTTP 500 `Clock moved backwards. Refusing for N seconds`. In the colima Docker VM the clock stepped back
about 50 seconds several times an hour, which failed benchmark runs and kept a fresh engine from passing its health
check. Fixed: the supplier generates the ids itself, in the same layout (41 bits of time, 6 of worker, 16 of
sequence), and when the clock steps back it keeps counting from its last timestamp; the library is removed. Tests:
`SnowflakeRequestIdSupplierTest` (a 52-second step back; 200,000 ids within one millisecond). The benchmark waits out
the refusal when measuring releases up to 0.9.1.

### A1 — Non-JSON responses by default · **fixed**
Query and load endpoints return a Java `toString()` (e.g. `QueryResourceInfo@48764515`) unless the request sends
`Accept: application/json`.

### A2 — Whole-file load failure easy to miss · **fixed** (bug-fix round 2)
Fixed: a whole-file failure is returned as a load response with one failure carrying the reason, and the load routes answer with that status (400) instead of 200.
When a whole CSV fails (e.g. Fire/Freeze before L2), the response carries a top-level `error` with empty
success/failure lists, which reads like "0 of 0 loaded".

## JDK

### J1 — On JDK 17+ the tests, then the engine, stopped on blocked reflective access · **fixed**
JDK 17 refused the reflection that older libraries use: first `ClassLoader.defineClass` (cglib, on the OWL
translation path, in fuse-core), then `java.util.regex.Pattern.namedGroups` (47 planner tests in fuse-dv-epb). Fixed
for the tests: the root pom's `test.jvm.opens` opens `java.lang`, `java.lang.reflect`, `java.util`, `java.util.regex`,
`java.io`, `java.nio` and `sun.nio.ch` to the test JVMs (JDK 11 accepts the same flags). The JDK 17 CI job now passes:
1,271 tests, 0 failures. The engine itself (Phase 6.0, 2026-11-28) didn't start on JDK 21 either (`InaccessibleObjectException` on `ClassLoader.defineClass` during Guice start-up); `start-fuse-service.sh` now passes the same `--add-opens` set (`--illegal-access`, used before, was removed in JDK 17). The Docker image runs on JDK 21: e2e ALL PASS, the benchmark unchanged.

### S1 — The engine sometimes hangs at start-up, before it logs anything · **fixed** (0.10)
About 1 start in 3 of the Docker image hung for good right after `java ... FuseRunner`, with no further log line and
`/fuse/health` never answering. Thread dump: logback's `ConsoleAppender` (`<withJansi>true</withJansi>` in the logback
configs) loads jansi, whose `CLibrary` extracts a native library and runs `chmod` through `Runtime.exec`; that fork
never returned (OpenJDK 11.0.14 on Alpine 3.14, the image's base). Jansi is only needed for colours on Windows
consoles. Fixed: `withJansi` is `false` in all three logback configs, so the fork never happens. Found 2026-11-26 while
benchmarking Phase 7.3 (it failed the version comparison's health wait). The image's EOL base (Alpine 3.14, JDK
11.0.14) was replaced in 0.10 by `eclipse-temurin:11-jdk`.

### O1 — logback 1.5 rejected the Elasticsearch log appender's `<property>` entries · **fixed** (0.10)
Since logback 1.5 (#187), every start printed 20 `ERROR in ...PropertyModelHandler` lines for the
`com.internetitem.logback.elasticsearch.ElasticsearchAppender` in the distributed logback configs, whose `<property>`
list logback now reads as its own element. The appender shipped logs to `http://localhost:9200/_bulk`, which isn't
OpenSearch inside the Docker stack. Fixed: the appender, its error log and the `logback-elasticsearch-appender`
dependency are removed; console and file logging are unchanged. To ship logs to OpenSearch, collect the container's
output or the `log/` files.

### T1 — Integration suite crashes when OpenSearch already runs, and reports success · **fixed**
`TestSuiteIndexProviderSuite` reuses a locally running cluster if one answers on the test HTTP port, but
`ElasticEmbeddedNode.getClient(info)` assumed an embedded node exists → `NullPointerException` in suite setup. Surefire's
parallel JUnit provider then reported 0 tests and `BUILD SUCCESS`, i.e. a false green. Fixed by dropping the parallel
provider (it ran single-threaded anyway) and connecting a transport client when reusing a cluster. Related hardening:
configurable test ports / data dir, and the disk-threshold decider is disabled on the test node.

## Tutorial data & docs

### D1 — Tutorial CSV defects · **fixed**
- `Person.csv`: 7 header columns, 8 values per row (full name unlabeled) → `name` never loaded
- `horses.csv`: 6 header columns, 5 values per row
- `Freeze.csv`: 7 header columns, 8 values per row
- `RegisteredGuilds.csv`: header typo `""startDate"`; duplicate edge ids
- Edge ids don't encode entity types, so files sharing a relationship collide (see L3)

### D3 — Dangling ids in tutorial relationship data · **resolved (examples rewritten)**
OwnsDragon owners range 0–999 but only 100 persons exist; MembersOfGuilds references guilds up to ~97 of 15. Each
stored person owns at most one dragon, so `count(o) > 50` examples can never match. Decision (maintainer): keep the
data, rewrite the count examples in query-the-data.md to use `Know` (`count(k) > 15` → 9 persons; with
`p2.gender = FEMALE`, `count(k) > 10` → 5).

### D2 — Docs that don't match reality · engine-independent parts **fixed**; engine-dependent parts pending
- Queries.md: references data that doesn't exist (`Bonaparte`, `merchant`), wrong relationship type `memberOf`,
  typo `Preson`, nonexistent property `fire.color`
- projection-and-count.md: shows the count result of a different query
- api.md: documents `/catalog/grqphql|sparql|sql` and SQL querying — no such routes
- load-data.md: expected document counts match none of the shipped CSVs

## Previously flagged (still disabled)
Tests `@Ignore`d during the OpenSearch 1.3.0 migration, each with an explanatory comment:
`OpensearchIndexProviderMappingFactoryIT` (stale type-wrapped JSON fixtures), three `fuse-dv-epb` tests
(Integer→Long `ClassCastException`, one histogram mismatch), `EmbeddedNodeRunner` (manual tool, hangs by design).
