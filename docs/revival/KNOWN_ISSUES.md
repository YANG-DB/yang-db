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

## Found by the legacy-test triage (Phase 3, stream I; see [IGNORED_TESTS.md](IGNORED_TESTS.md))

### Q9 — Cypher keeps only the first type of a multi-type relationship · **now an explicit error**
`[r:A|B]` translates to a relation of type `A` only (`CypherMatchSimpleTranslatorTest:86`).

### Q10 — Cypher relationship property maps: unnamed relationships crash, extra properties ignored · **fixed**
Verified live (e2e R4–R6): a named relationship with one property works. An **unnamed** one (`-[:Fire {t: 1}]->`)
fails with HTTP 500 `None.get`, and with **several** properties only the first is applied.

### Q12 — Cypher node property maps: unnamed nodes crash, extra properties ignored · **fixed**
`(p:Person {gender: FEMALE, name: 'X'})` applies only the first property; `(:Person {name: 'X'})-[…]` fails with HTTP
500 `None.get`. The node-pattern sibling of Q10 (found by the bug-fix round's agent K). Pinned by e2e R7/R8.

### Q11 — Relationship property plus an end-pattern filter fails validation · open
"No Parent Element found" (`RelationRangeAsgStrategyWithFiltersInRangTest:318`).

### P2 — Range operators on enumerated values crash planner statistics · open
`ClassCastException: ontology.Value cannot be cast to Comparable` (`SmartEpbRedundancyTests:262`).

### M1 — Single-index provider never maps relationship fields · open
`direction`, `entityA`/`entityB` and redundant fields fall to dynamic mapping. Pinned by the ignored test
`OpensearchIndexProviderMappingFactoryIT#testGenerateSingleMappingMapsRelationSides` (Phase 3, stream F).

### M2 — Single-index provider's "ontology" template matches every bare type-named index · open
Its index patterns include every type name (`dragon`, `horse`, `own`…), so it merges into unrelated indices with
those names. This is the "leftover templates break later index creation" trap. Pinned by
`OpensearchIndexProviderMappingFactoryIT#testGenerateSingleMappingPatternsOnlyMatchUnifiedIndex`.

## Count filter

### C1 — Count-filter script uses the wrong operators for `=` and `>=` · **fixed**
`AggregationBuilder.BuildFilterScript` emits `a=120` for `eq`, which is an *assignment* in Painless rather than a
comparison, and `a>120` for `gte`. So `WHERE count(r) = n` and `>= n` don't filter as written. `>` and `<` (used by
the docs) are correct. Fixed: `==`, `>=`, and `!=` (which produced an uncompilable `a120`). An unknown operator now
throws. Pinned by e2e R1/R2.

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

### A3 — Dragons extension routes are never registered · open
`FuseApp` only discovers `AppControllerRegistrarBase` subclasses under `com.yangdb.fuse.services`. The Dragons
extension's `DragonsExtensionRegistrar` lives in `com.yangdb.dragons.services`, so `/ext`, `/fuse/query/clause/run`,
`/fuse/query/cypher/logical/run` and `/fuse/query/query/v1/logical/run` all return 404. The playground's registrar
sits in `com.yangdb.fuse.services.dragons` to be found. A proper fix belongs to Phase 4 (extension discovery).

### A1 — Non-JSON responses by default · **fixed**
Query and load endpoints return a Java `toString()` (e.g. `QueryResourceInfo@48764515`) unless the request sends
`Accept: application/json`.

### A2 — Whole-file load failure easy to miss · open, minor
When a whole CSV fails (e.g. Fire/Freeze before L2), the response carries a top-level `error` with empty
success/failure lists, which reads like "0 of 0 loaded".

## Test infrastructure

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
