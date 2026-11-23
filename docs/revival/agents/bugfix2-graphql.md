# Bug-fix round 2 — GraphQL extension and V1→ASG transformer

Goal: fix G4 (GraphQL exposes only the first target type of a multi-target relationship) and Q13
(`QueryToAsgTransformer` keeps state across queries). Branch: `worktree-agent-ad56233455714682d`.

## Q13 — fixed · c69f11946
- **Root cause:** `QueryToAsgTransformer` collected ASG elements in an instance `HashMap` that was never cleared, and
  `AsgQuery` stored a *live view* (`values()`) of it. `AsgModule` binds `QueryToCompositeAsgTransformer` (its subclass)
  as an **eager singleton** for `QueryTransformer<Query, AsgQuery>`, which every V1 query goes through (Cypher, SPARQL,
  V1, GraphQL). So every query's ASG carried the elements of all earlier queries, and a later query overwrote
  same-eNum elements of earlier results. It was not thread-safe either.
- **Users:** `AsgModule` (singleton binding); `QueryToCompositeAsgTransformer` (calls `super.transform` for inner
  queries too); `AsgGraphQLTransformer` (had a per-query `Provider` workaround); fuse-asg tests.
- **Fix:** the map is a local of `transform`, passed through `buildSubGraphRec`; the ASG gets a copy of its values;
  the factories are `final` (stateless lookup tables). The GraphQL `Provider` workaround was removed.
- **Test:** `fuse-core/.../dispatcher/asg/QueryToAsgTransformerTest` (2 tests: second query has only its own
  elements; a later query doesn't change an earlier result). It failed before the fix (`expected:<[0, 1]> but was:<[0, 1, 2, 3, 4, 5]>`).
  fuse-core: 25 tests green. fuse-asg plus graphql: 349 tests, 0 failures.

## G4 — fixed · 59b1b6611
- **Root cause:** `OntologyToGraphQLSdl` took `findFirst()` of a relationship's side-B types, and the translator
  could only emit one `ETyped` per relationship field. The interface type resolver always picked the first
  implementation, and the union resolver returned the Query type.
- **Modelling (graphql-java 24):** a relationship from an entity type to several types gets an abstract type
  `<sideA>_<rel>` (`Person_Own`). It is an **interface** of the fields the side-B types share (same name and type)
  when there are any, so `Own { name }` stays valid. Otherwise it is a **union**, queried with fragments only. Inline
  fragments (`... on Dragon`) narrow the types queried.
- **Translation:** one queried type gives `Rel → ETyped`. Several give `Rel → EUntyped(vTypes)`, the same shape the
  Cypher translator (multi-label node) and `UntypedRelationInferTypeAsgStrategy` produce. The abstract field
  returns one `Typed` marker per queried type, and the type resolver maps each marker to its object type, so every
  fragment's fields are visited. Fields reached through several markers are translated once: fetch results and
  `pathContext` are keyed by the path with list indexes removed. An interface that is itself an ontology entity
  (imported SDL `Character`) keeps the old `Typ[Character]` side B. `Query.Builder.unTyped(types, tag)` was added
  in fuse-model.
- **Tests:** `GraphQLDragonsQueryTest` gained 6 new tests plus a rewritten one (schema interface, all owned, dragons
  only, horses only, common plus per-type fields, a shared relationship under the interface, and a union for types
  with nothing in common). Before the fix, 6 of the 7 failed (4 failures, 2 errors). `AsgGraphQLTransformerTest`
  gained a check that 5 multi-type queries pass the M2 ASG rewrite and validation. graphql module: 44 tests green.
- **Docs/e2e:** in `query-the-data.md`, GraphQL:3 is now `Own { ... on Dragon { name } }` and a new GraphQL:4 covers
  dragons and horses. Playground `openapi.json` has GQ3 updated and GQ4 added. `docker/e2e/doc_queries.py` has a
  GQ4 expectation (owners of either, dragons, horses, and both kinds of Own edges).

## Open / notes
- **Behaviour change:** `{ person { Own { name } } }` used to return dragons only and now returns dragons and
  horses. Type-specific fields need a fragment (`Own { color }` is now a validation error; use
  `Own { ... on Dragon { color } }`).
- **Not run:** `DistributionGraphQLTest` (I added the GQ3/GQ4 queries to it). Running the distribution module was
  denied by the permission classifier. The equivalent check passes in the graphql module.
- **Not verified live:** the Docker stack on :8888 currently has no Dragon/Horse indices (only `person`, 100 docs),
  so the e2e GQ3/GQ4 could not be run, and loading data is off-limits. Run `docker/e2e/doc_queries.py --only GQ3 GQ4`
  after a reload.
- **Noticed, not fixed:** `TraversalWiringFactory.populateGraphEnum` adds `eProp(fieldName)` rather than the
  property's pType. This is harmless while name == pType, as in Dragons.
