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

### L4 — Time-partition bucket choice · open, investigate
All Fire/Freeze documents (years 1900–1999) land in `idx_*_1000`; the other three partitions stay empty.

## Query engine

### Q1 — Undirected pattern executes as directed · open
`(a)-[r]-(b)` behaves exactly like `(a)-[r]->(b)`. Tutorial query 2 returns 245 of 349 correct relationships.

### Q2 — Relationship endpoint type ignored · open
Endpoints are matched by id only. Person 42 owns Horse 7 → returned as Dragon 7. Undirected is worse: Person 42 gets
21 "dragons" (the owners of Dragon 42, via the reverse-direction documents).

### Q3 — Edge between two entities with the same id is dropped · open
`Person:22 → Dragon:22` is stored correctly but never returned.

### Q4 — Relationship ids built without a delimiter · open
`rType + idA + idB` (e.g. `Fire4465`) collides for different pairs (4→465 and 44→65), so `distinct` loses pairs. All
relationships between the same two entities also collapse into one even without `distinct`, contradicting
query-the-data.md.

### Q5 — Untyped node / label union unsupported · open
`match (p) return *` returns nothing; `match (p:Person | Dragon)` is a syntax error. Both appear in Queries.md.

## API

### A1 — Non-JSON responses by default · open
Query and load endpoints return a Java `toString()` (e.g. `QueryResourceInfo@48764515`) unless the request sends
`Accept: application/json`.

## Test infrastructure

### T1 — Integration suite crashes when OpenSearch already runs, and reports success · **fixed**
`TestSuiteIndexProviderSuite` reuses a locally running cluster if one answers on the test HTTP port, but
`ElasticEmbeddedNode.getClient(info)` assumed an embedded node exists → `NullPointerException` in suite setup. Surefire's
parallel JUnit provider then reported 0 tests and `BUILD SUCCESS`, i.e. a false green. Fixed by dropping the parallel
provider (it ran single-threaded anyway) and connecting a transport client when reusing a cluster. Related hardening:
configurable test ports / data dir, and the disk-threshold decider is disabled on the test node.

## Tutorial data & docs

### D1 — Tutorial CSV defects · open
- `Person.csv`: 7 header columns, 8 values per row (full name unlabeled) → `name` never loaded
- `horses.csv`: 6 header columns, 5 values per row
- `Freeze.csv`: 7 header columns, 8 values per row
- `RegisteredGuilds.csv`: header typo `""startDate"`; duplicate edge ids
- Edge ids don't encode entity types, so files sharing a relationship collide (see L3)

### D2 — Docs that don't match reality · open
- Queries.md: references data that doesn't exist (`Bonaparte`, `merchant`), wrong relationship type `memberOf`,
  typo `Preson`, nonexistent property `fire.color`
- projection-and-count.md: shows the count result of a different query
- api.md: documents `/catalog/grqphql|sparql|sql` and SQL querying — no such routes
- load-data.md: expected document counts match none of the shipped CSVs

## Previously flagged (still disabled)
Tests `@Ignore`d during the OpenSearch 1.3.0 migration, each with an explanatory comment:
`OpensearchIndexProviderMappingFactoryIT` (stale type-wrapped JSON fixtures), three `fuse-dv-epb` tests
(Integer→Long `ClassCastException`, one histogram mismatch), `EmbeddedNodeRunner` (manual tool, hangs by design).
