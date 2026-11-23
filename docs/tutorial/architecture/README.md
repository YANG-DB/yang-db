# Architecture tutorial: how YANG-DB works inside

For developers who want to read, debug or extend the engine. If you only want to *use* YANG-DB, start with the
[Dragons tutorial](../sample/dragons/README.md) instead.

YANG-DB answers graph queries (Cypher, SPARQL, GraphQL, or its native V1 JSON) over data stored in ordinary
OpenSearch indices. An **ontology** defines the graph's types: entities, relationships and properties. An **index
provider** defines where each type lives in OpenSearch. In between, a query is translated several times. Each
translation is done by a different module, and you can inspect its result over the REST API.

```
Cypher text ─1▶ ASG ─2▶ rewritten ASG ─3▶ plan ─4▶ Gremlin traversal ─5▶ OpenSearch searches ─6▶ assignments (JSON)

1 fuse-asg    2 fuse-asg + fuse-dv-asg    3 fuse-dv-epb    4 fuse-dv-gta
5 fuse-dv-unipop, unipop-core, fuse-opensearch-client    6 fuse-dv-core
```

Each numbered arrow is one translation, and the numbers are the stages of [the life of a query](query-lifecycle.md).

## The pages

1. **[The modules](modules.md)**: what each Maven module is for, its key classes, how the modules depend on each
   other, the build profiles, and how the configuration chooses the implementations at runtime.
2. **[The life of a query](query-lifecycle.md)**: one Cypher query followed from the HTTP request to the JSON answer.
   It shows the real intermediate form after every translation (ASG, plan, traversal, the OpenSearch requests,
   result), the module responsible for each step, and how to inspect each one yourself.

Read them in order, or jump to the second page and use the first as a reference.

## Words you'll meet

| Term | Meaning |
|---|---|
| **Ontology** | The logical schema: entity types (`Person`, `Dragon`), relationship types (`Own`) with their allowed endpoint pairs, and properties. JSON, served by `/fuse/catalog/ontology` |
| **Index provider** | The physical schema: which OpenSearch index or partition holds each type, and which fields are copied onto edges |
| **V1** | YANG-DB's native JSON query language: numbered elements (`ETyped`, `Rel`, `EProp`, `Quant1`, …) linked by `next` |
| **ASG** | Abstract Syntax Graph: the query as a tree of elements. Every language is translated into it, and every later stage reads it |
| **EPB** | Execution Plan Builder: the cost-based planner that chooses the order of the steps |
| **GTA** | The translator from a plan to a Gremlin (TinkerPop) traversal |
| **Unipop** | The TinkerPop graph implementation that runs traversal steps as OpenSearch searches |
| **Cursor / page** | How results are delivered: a cursor over a query's results, read one page at a time |
| **Assignment** | One match of the pattern: the entities and relationships bound to the query's tags |

## Related documentation

- Older component notes, with more background on each part: [architecture](../../info/components/architecture.md),
  [ontology](../../info/components/ontology.md), [IndexProvider](../../info/components/IndexProvider.md),
  [query language (V1)](../../info/components/query_language.md),
  [execution planner](../../info/components/execution_planner.md),
  [cost estimation](../../info/components/cost_estimator.md).
- Writing a plugin or a new query language: [CONTRIBUTING.md, *Writing an extension*](../../../CONTRIBUTING.md#writing-an-extension).
- [REST API](../../info/api.md) · [Known issues](../../revival/KNOWN_ISSUES.md) · [Roadmap](../../revival/ROADMAP.md)
