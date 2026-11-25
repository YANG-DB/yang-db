# Bug-fix round 2 — service/API routes

Goal: fix A9, A10, A5, A8, A4 (docs/revival/KNOWN_ISSUES.md). One commit per bug, failing test first.
Branch: `worktree-agent-a5e720cb226ac9738`. KNOWN_ISSUES is updated by the coordinator, not here.

Route tests: `fuse-service/src/test/java/com/yangdb/fuse/services/appRegistrars/QueryRoutesTest.java` (Jooby on a free
port, QueryControllerRegistrar + PageControllerRegistrar + generic language routes, recording QueryController proxy).

| Bug | Status | Root cause | Fix | Commit |
|-----|--------|-----------|-----|--------|
| A9 `POST /v1/run` 404 | fixed | `(req, rsp)` handler discarded `API.runV1`'s Result; Jooby fell through to `/:lang/run` | `runV1` is `void` and `resp.send`s (also fixes Dragons `/query/v1/logical/run`) | b66400ed7 |
| A10 `POST /cypher` needs type | fixed | `postCypher`/`postSparql` passed `null` type to `postJsonQuery` | pass `TYPE_CYPHERQL` / `TYPE_SPARQL` | c18b17aba |
| A5 `sparkQL` key | fixed | registry key was `sparkQL`; case-insensitive match can't reach `sparql` | `TYPE_SPARQL = "sparql"`, `TYPE_SPARQL_LEGACY = "sparkQL"` also registered by AsgSparqlModule; docs updated | f21de57a6 |
| A8 `elastic/view` 404 | fixed (removed) | viewer page and `/elastic` routes never existed; OpenSearch requests are built per step by unipop controllers during page fetch and never stored | removed both `/elastic/view` routes and `elasticQueryUrl` from Query/PageResourceInfo (`@JsonIgnoreProperties` keeps old JSON readable) | 44e3b99a5 |
| A4 Dragons clause routes | fixed (removed; maintainer chose option 1) | see below | deleted `DragonsExtensionRegistrar`, `DragonsExtensionQueryController`, `ExtensionQueryDriver`; `DragonsExtension` contributes only the playground registrar; `DragonsExtensionTest` asserts the routes are gone | see git log |

## A4 findings (decision: option 1)
The `clause` language was the Knowledge domain's JSON clause DSL: `AsgClauseTransformer` →
`JsonQueryTranslator` (+ `BusinessTypesProvider`, `FolderBasedTypeProvider`, parser model), deleted in 040f4deb8 with
the knowledge extension. It builds Knowledge-ontology queries (`Entity`/`Evalue`, `ontology` default "Knowledge"), so
it doesn't apply to Dragons. Dragons' `ExtensionQueryDriver.createClause`/`runClause` and the `transformer` field are
leftovers of that copy. Options:
1. Remove the clause routes, `ExtensionQueryDriver` clause code, `/ext` redirect and the `cypher/logical/run`,
   `query/v1/logical/run` aliases (they duplicate core routes) from the Dragons extension. Small, honest.
2. Port the clause DSL as a generic `clause` language (QueryLanguages, clause → V1 Query → AsgQuery) rewritten
   against an arbitrary ontology: ~600 lines from history plus design of the DSL for non-Knowledge ontologies.
3. Keep as-is, documented as broken.
Recommendation: 1.

## Unrelated notes
- `mvn -pl fuse-service -am test` without `-Dtest` also runs dependency modules' tests (fuse-dv-unipop...), which may
  start embedded OpenSearch: always restrict with `-Dtest`.
