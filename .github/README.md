# YANG-DB

**YetAnotherNonNativeGraph-DB** — a graph query engine that adds an ontology-driven property-graph layer on top of
[OpenSearch](https://opensearch.org/) indices. Model your domain as entities and relationships, map them onto
existing (or generated) OpenSearch indices, and query them as a graph, without moving the data into a separate
graph database.

- **Logical ontology over physical indices** — entities, relationships and enumerations are mapped onto OpenSearch
  indices through an index provider (static, time-partitioned, unified, nested, or redundant layouts).
  See [IndexProvider](../docs/info/components/IndexProvider.md).
- **Several query front-ends** — the native V1 query language, plus Cypher, GraphQL and SPARQL translators.
  Ontologies can also be imported from OWL, GraphQL schemas and SQL DDL.
- **Cost-based planning** — queries are compiled into an execution plan using index statistics, then translated
  into TinkerPop graph traversals executed against OpenSearch.
- **REST API with Swagger UI**, paged cursors, and query-result projections.

> **Status (2026):** the project is being revived after a few quiet years. `main` builds against
> OpenSearch **1.3.0**; moving to OpenSearch 2.x is planned. Some legacy tests are disabled pending rework.

## Quickstart

Requires Docker (with `docker compose`), JDK 11 and Maven 3.6+. This builds the engine with the Dragons sample
ontology and runs it next to OpenSearch 1.3.0 and OpenSearch Dashboards:

```bash
cd docker
./buildDocker.sh Dragons
docker compose -f docker-compose-os.yml up -d
curl http://localhost:8888/fuse/health
```

Then open the Swagger UI at http://localhost:8888/swagger#. Full details in [docker/README.md](../docker/README.md),
and the [Dragons tutorial](../docs/tutorial/sample/dragons/create-ontology.md) walks through creating the ontology,
[loading data](../docs/tutorial/sample/dragons/load-data.md) and
[querying it](../docs/tutorial/sample/dragons/query-the-data.md).

## Building from source

```bash
mvn clean install                          # core modules, with tests (JDK 11)
mvn clean install -P core,dragons-assembly # also builds the Dragons sample distribution into distrib/dragon
```

| Module | What it contains |
|---|---|
| `fuse-model` | Core model: ontology, query, results, and index-provider schema |
| `fuse-asg` | Query abstract-syntax-graph and the Cypher / GraphQL / SPARQL translators |
| `fuse-core` | Query dispatching, ontology import (OWL, GraphQL schema, SQL DDL), Java client |
| `fuse-dv` | OpenSearch data layer: execution planning (`epb`), statistics (`stat`), plan-to-traversal translation (`gta`), traversal execution (`unipop`, `core`) |
| `unipop-core` | TinkerPop structure/predicate layer the traversals run on |
| `fuse-service` | REST server and Swagger UI |
| `fuse-geojson` | GeoJSON support |
| `fuse-test` | Test framework with an embedded OpenSearch node |
| `fuse-domain` | The Dragons sample domain (demo data, assembly, system tests) |

## Documentation

- [Project overview](../yang-db-high-level.md)
- [Architecture](../docs/info/components/architecture.md) · [Ontology](../docs/info/components/ontology.md) ·
  [Query language](../docs/info/components/query_language.md) · [Execution planner](../docs/info/components/execution_planner.md)
- [REST API](../docs/info/api.md)

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](../CONTRIBUTING.md) and the [Code of Conduct](../CODE_OF_CONDUCT.md).
Open pull requests against `main`; `master` is a frozen 2019 snapshot.

## License

[Apache License 2.0](../LICENSE)

## Maintainer

YANG-DB is created and maintained by **Lior Perry** —
[LinkedIn](https://www.linkedin.com/in/lior-perry-yang-db/) · [Portfolio](https://yang-db.github.io/yang-db/portfolio.html) ·
[OpenSearch community](https://opensearch.org/author/lior-perry/)
