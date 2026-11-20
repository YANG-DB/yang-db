# YANG-DB

**YetAnotherNonNativeGraph-DB** — a graph query engine that adds an ontology-driven property-graph layer on top of
[OpenSearch](https://opensearch.org/) indices. Model your domain as entities and relationships, map them onto
existing (or generated) OpenSearch indices, and query them as a graph, without moving the data into a separate
graph database.

- **Logical ontology over physical indices** — entities, relationships and enumerations are mapped onto OpenSearch
  indices through an index provider (static, time-partitioned, unified, nested, or redundant layouts).
  See [IndexProvider](docs/info/components/IndexProvider.md).
- **Several query front-ends** — the native V1 query language, plus Cypher, GraphQL and SPARQL translators.
  Ontologies are defined as JSON and registered through the catalog API.
- **Cost-based planning** — queries are compiled into an execution plan using index statistics, then translated
  into TinkerPop graph traversals executed against OpenSearch.
- **REST API with Swagger UI**, paged cursors, and query-result projections.

> **Status (2026):** the project is being revived after a few quiet years. `main` builds against
> OpenSearch **1.3.0** (moving to 2.x is planned). The full build is green (1,123 tests with the integration suites
> running), and every documented tutorial query passes end to end on Docker
> ([e2e suite](docker/e2e/README.md)). A stabilization pass fixed the tutorial data loader, relationship identity and direction, time
> partitions and JSON responses. Progress and open issues are in [docs/revival/](docs/revival/ROADMAP.md).

## Quickstart

Requires Docker (with `docker compose`), JDK 11 and Maven 3.6+. This builds the engine with the Dragons sample
ontology and runs it next to OpenSearch 1.3.0 and OpenSearch Dashboards:

```bash
cd docker
./buildDocker.sh Dragons
docker compose -f docker-compose-os.yml up -d
curl http://localhost:8888/fuse/health
```

Load the Dragons tutorial data and check every documented example query against it (Python 3, standard library only):

```bash
python3 docker/e2e/doc_queries.py --load --reset
curl -H 'Accept: application/json' \
  'http://localhost:8888/fuse/query/cypher/run?ontology=Dragons&cypher=MATCH%20(d:Dragon)%20RETURN%20*'
```

Then open the Swagger UI at http://localhost:8888/swagger#. Full details in [docker/README.md](docker/README.md),
and the [Dragons tutorial](docs/tutorial/sample/dragons/create-ontology.md) walks through creating the ontology,
[loading data](docs/tutorial/sample/dragons/load-data.md) and
[querying it](docs/tutorial/sample/dragons/query-the-data.md).

## Building from source

```bash
mvn clean install                          # core modules, with tests (JDK 11)
mvn clean install -P core,dragons-assembly # also builds examples/ and the distribution into distrib/dragon
```

| Module | What it contains |
|---|---|
| `fuse-model` | Core model: ontology, query, results, and index-provider schema |
| `fuse-asg` | Query abstract-syntax-graph and the Cypher / GraphQL / SPARQL translators |
| `fuse-core` | Query dispatching, Java client, ontology transformers for OWL / GraphQL schema / SQL DDL (not exposed via the REST API) |
| `fuse-dv` | OpenSearch data layer: execution planning (`epb`), statistics (`stat`), plan-to-traversal translation (`gta`), traversal execution (`unipop`, `core`) |
| `unipop-core` | TinkerPop structure/predicate layer the traversals run on |
| `fuse-service` | REST server and Swagger UI |
| `fuse-geojson` | GeoJSON support |
| `fuse-test` | Test framework with an embedded OpenSearch node |
| `distribution` | Assembles the runnable engine (with the Dragons sample configuration) into `distrib/dragon`; the Docker image is built from it. Built with `-P dragons-assembly` |
| `examples/dragons` | The Dragons sample domain: the `DragonsModule` service extension (`ext`) and the tutorial data generator (`datagen`). Not part of the default build |

## Documentation

- [Project overview](yang-db-high-level.md)
- [Architecture](docs/info/components/architecture.md) · [Ontology](docs/info/components/ontology.md) ·
  [Query language](docs/info/components/query_language.md) · [Execution planner](docs/info/components/execution_planner.md)
- [REST API](docs/info/api.md)
- [Revival roadmap](docs/revival/ROADMAP.md) · [Known issues](docs/revival/KNOWN_ISSUES.md) ·
  [Live status](docs/revival/STATUS.md)

### Cypher support notes
- Node label union `(n:A|B)` is not supported (openCypher 9 grammar). Use `MATCH (n) WHERE n:A OR n:B`.
- Cyclic patterns, which close a loop back to an already-matched node, are rejected with an explicit error.
- A count filter (`WHERE count(r) > n`) counts all of the start node's relationships; row filters on the far end
  only restrict which neighbours are returned.

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md).
Open pull requests against `main`; `master` is a frozen 2019 snapshot.

## License

[Apache License 2.0](LICENSE)

## Maintainer

YANG-DB is created and maintained by **Lior Perry** —
[LinkedIn](https://www.linkedin.com/in/lior-perry-yang-db/) · [Portfolio](https://yang-db.github.io/yang-db/portfolio.html) ·
[OpenSearch community](https://opensearch.org/author/lior-perry/)
