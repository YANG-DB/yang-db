# Phase 4 — agent N: GraphQL as an extension (Stage B)

## Goal
Move the whole GraphQL feature out of core into `extensions/graphql` (a `YangDbExtension`), and make it work end to
end through REST: fix G1 (no Query root for JSON ontologies), G2 (stateful schema import), G3 (multi-file SDL).

## Tasks
- [x] extensions/pom.xml aggregator + extensions/graphql module; root pom core profile; distribution dependency
- [x] move translator, schema translation + routes, cursor, tests; old /graphQL routes delegate to controller.run
- [x] drop graphql-java from core poms (fuse-model, fuse-asg; nothing in core needs it)
- [x] G1 test + fix (a03bf2579), G2/G3 tests + fix (c76400fdc), QueryToAsgTransformer leak (in a03bf2579)
- [x] playground spec GQ1-GQ3 + doc_queries.py + DragonsPlaygroundRegistrarTest + tutorial doc section
- [ ] final full test run (core,examples) and commit of e2e/docs

## Status
Code done. Next: confirm the second full build (full2.log) is green, commit e2e/docs, final report. The lead must
run e2e (doc_queries.py) for GQ1-GQ3: expectations GQ1 = all Dragon docs, GQ2 = Dragon docs with color BLACK,
GQ3 = Own Person->Dragon edges (rels) + owners + owned dragons.

## Round 2 (lead's e2e: GQ1-GQ3 "failed building the cursor request forwardPaths")
Root cause: antlr4-runtime 4.5.3 (lang-painless via fuse-service) won mediation in the distribution once
graphql-java left fuse-model/fuse-asg; graphql-java needs 4.7.2 -> NoClassDefFoundError CharStreams, an Error that
QueryDriverBase.run swallowed into that message. Fixed in ead811cf7 (pin 4.7.2 in root depMgmt, explicit dep in the
extension, cause appended to the error, distribution tests on the shipped classpath: DistributionGraphQLTest).
Next: full core,examples,dragons-assembly run (full3.log) green -> report; lead rebuilds image and reruns e2e.

## Discoveries
- ServiceModule's bindSparqlController / bindSqlController bind their inner SchemaTranslatorController to
  StandardGraphQLController (copy-paste); the routes use getController(StandardSqlController.class) directly, so the
  named bindings are unused. Must re-point them when StandardGraphQLController leaves core.
- GraphQL2QueryTransformer uses ONE global TypeDefinitionRegistry (the last SDL imported): root cause of G1 (JSON
  ontologies never fill it) and G2 (the registry is merged into on every import).
- `GraphQLToOntologyTransformer.translate(Ontology)` (ontology -> SDL) was an unimplemented TODO (now OntologyToGraphQLSdl).
- QueryToAsgTransformer keeps every element it ever transformed (field map): any shared instance leaks elements
  between queries. AsgGraphQLTransformer now gets a fresh one per query; other users not checked (out of scope).
- graphql-java brought antlr4-runtime transitively; fuse-model's Assignment had an unused MurmurHash import of it.
- Jooby's default error handler renders IllegalArgumentException as 400 JSON with message + stacktrace.
- Queries on SDL-imported ontologies also use the generated schema: interfaces become plain types and the root
  fields follow the entity names (StarWars: character/human/droid, no `hero`). All StarWars tests pass unchanged.
- A relationship with several side-B types (Own: Dragon, Horse) gets the first pair's type in GraphQL.

## Log
- 704a5f6a3 move commit. Tests after move: model 27, core 18 (-19: 18 GraphQL tests + 1 cursor-deserializer test moved),
  asg 312, dv-unipop 152, dv-core 77, dv-asg 38, dv-epb 433, dv-gta 61, service 19, dragons-ext 7, datagen 34,
  extension-graphql 19 -- all 0 failures.
- RED (before G1-G3 fixes): GraphQLDragonsQueryTest 4/4 errors (SchemaProblem "A schema MUST have a 'query'
  operation defined"; with a prior StarWars import: FieldUndefined 'dragon'); GraphQLSchemaImportTest 3 of 5 fail
  (second schema returns the first's ontology; failed import breaks the next one; missing WhereClause -> SchemaProblem);
  GraphQLRoutesTest 4 of 7 fail (graphQL run/create on Dragons 400 FieldUndefined; broken SDL returns 200 with the
  previously cached StarWars ontology).
