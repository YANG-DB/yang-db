# YANG-DB

[![CI](https://github.com/YANG-DB/yang-db/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/YANG-DB/yang-db/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/YANG-DB/yang-db)](https://github.com/YANG-DB/yang-db/releases/latest)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)

**YetAnotherNonNativeGraph-DB** — a graph query engine that adds an ontology-driven property-graph layer on top of
[OpenSearch](https://opensearch.org/) indices. Model your domain as entities and relationships, map them onto
existing (or generated) OpenSearch indices, and query them as a graph, without moving the data into a separate
graph database.

- **Logical ontology over physical indices** — entities, relationships and enumerations are mapped onto OpenSearch
  indices through an index provider (static, time-partitioned, unified, nested, or redundant layouts).
  See [IndexProvider](docs/info/components/IndexProvider.md).
- **Several query front-ends** — the native V1 query language, plus Cypher, GraphQL and SPARQL translators
  (read queries; coverage differs, see [Limitations](#limitations)). Ontologies are defined as JSON and registered
  through the catalog API.
- **Cost-based planning** — queries are compiled into an execution plan using index statistics, then translated
  into TinkerPop graph traversals executed against OpenSearch.
- **REST API with Swagger UI**, paged cursors, and query-result projections.

> **Status (2026):** the project is being revived after a few quiet years. `main` builds against
> OpenSearch **2.19.7** (upgraded from 1.3.0 in Phase 5; 3.x is planned). The latest release is
> [0.10.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.10.0) (OpenSearch 2.19.7: faster, consistent cursors, the V1 spec); [0.8.0](https://github.com/YANG-DB/yang-db/releases/tag/v0.8.0)
> is the last on OpenSearch 1.3. Each has a ready-built distribution attached. The full build is green (about 1,300 tests, integration suites
> included; each [CI run](https://github.com/YANG-DB/yang-db/actions/workflows/ci.yml) shows the exact count), and
> every documented tutorial query passes end to end on Docker
> ([e2e suite](docker/e2e/README.md)). A stabilization pass fixed the tutorial data loader, relationship identity and direction, time
> partitions and JSON responses. Progress and open issues are in [docs/revival/](docs/revival/ROADMAP.md).

## Requirements

| | |
|---|---|
| Java | **JDK 11** for building, testing and running (the code compiles to Java 11; CI and the Docker image use Temurin/OpenJDK 11). The build and tests also pass on **JDK 17**, which CI checks on every pull request; running the engine on 17 is not tested yet. JDK 21 is Phase 6 of the [roadmap](docs/revival/ROADMAP.md) |
| Maven | 3.6 or later (CI uses 3.9) |
| OpenSearch | **2.19.x** for 0.9 and `main`; 1.3 for the 0.8 line. See [Limitations](#limitations) |
| Docker | with `docker compose`, for the quickstart |
| Python 3 | standard library only, for the data loader, the e2e suite and the benchmark |

## Quickstart

Run the prebuilt image (published to the GitHub container registry for each release from 0.9.1 on) next to
OpenSearch 2.19.7 and OpenSearch Dashboards:

```bash
cd docker
YANGDB_IMAGE=ghcr.io/yang-db/yang.db YANGDB_TAG=0.10.0 docker compose -f docker-compose-os.yml up -d
curl http://localhost:8888/fuse/health
```

Or build the image yourself from this checkout (JDK 11 and Maven). The build takes a couple of minutes once Maven has
downloaded the dependencies. Measured disk use: about 0.8 GB for the Maven repository, 0.9 GB for the build, and
about 5 GB of images (OpenSearch 2 GB, Dashboards 2.4 GB, YANG-DB 0.8 GB):

```bash
cd docker
./buildDocker.sh Dragons
docker compose -f docker-compose-os.yml up -d
```

Load the Dragons tutorial data and check every documented example query against it (Python 3, standard library only):

```bash
python3 docker/e2e/doc_queries.py --load --reset
curl -H 'Accept: application/json' \
  'http://localhost:8888/fuse/query/cypher/run?ontology=Dragons&cypher=MATCH%20(d:Dragon)%20RETURN%20*'
```

Then try the **Dragons playground** at http://localhost:8888/dragons/swagger: one click loads the tutorial data,
and every tutorial query is ready to run from a list. The full API reference (OpenAPI 3.1) is at http://localhost:8888/swagger. Full details in [docker/README.md](docker/README.md),
and the [Dragons tutorial](docs/tutorial/sample/dragons/README.md) walks through creating the ontology,
[loading data](docs/tutorial/sample/dragons/load-data.md) and
[querying it](docs/tutorial/sample/dragons/query-the-data.md).

## Building from source

```bash
mvn clean install                          # core modules, with tests (JDK 11)
mvn clean install -P core,dragons-assembly # also builds examples/ and the distribution into distrib/dragon
mvn clean install -DskipTests -P core,dragons-assembly   # the distribution only, about 2 minutes
```

**Tests.** `mvn test` runs the unit tests *and* the integration tests: classes named `*IT` and the suites start an
embedded OpenSearch node in the same Maven run (surefire runs every test class; there is no separate failsafe
`verify` step). The embedded node uses ports 9200/9300 by default, and the suites refuse to run if an OpenSearch is
already listening there, to protect its data: stop it, or pick free ports with
`-Dfuse.test.http.port=19200 -Dfuse.test.transport.port=19300`. CI runs `mvn clean test` and the Docker end-to-end
suite on every pull request and shows the test counts in each run's summary. Every ignored test states its reason,
which CI checks: see [IGNORED_TESTS.md](docs/revival/IGNORED_TESTS.md).

| Module | What it contains |
|---|---|
| `fuse-model` | The shared data model: ontology, index provider, V1 query, ASG, plans, REST requests, results |
| `fuse-extension-api` | The plugin contract (`YangDbExtension`, `QueryLanguages`) |
| `fuse-core` | Contracts and the query lifecycle (`QueryDriverBase`), ontology providers, schema importers (OWL, SQL DDL), Java client |
| `fuse-asg` | Cypher and SPARQL → ASG translators, ASG rewrite and validation |
| `fuse-dv` | The OpenSearch data view: schema-aware rewrites (`asg`), the cost-based planner (`epb`), plan → traversal (`gta`), traversal execution (`unipop`), drivers, cursors and loaders (`core`), offline statistics (`stat`) |
| `unipop-core` | The TinkerPop graph that runs traversal steps through controllers |
| `fuse-opensearch-client` | The REST bridge: the `Client` interface over OpenSearch's REST API |
| `fuse-service` | REST server and Swagger UI |
| `fuse-geojson` | GeoJSON support |
| `fuse-test` | Test framework with an embedded OpenSearch node |
| `extensions/graphql` | The GraphQL language, as a plugin |
| `distribution` | Assembles the runnable engine (with the Dragons sample configuration) into `distrib/dragon`; the Docker image is built from it. Built with `-P dragons-assembly` |
| `examples/dragons` | The Dragons sample domain: the `DragonsModule` service extension (`ext`) and the tutorial data generator (`datagen`). Not part of the default build |

Each module's purpose and key classes, and the path of a query through them, are explained in the
[architecture tutorial](docs/tutorial/architecture/README.md).

## Documentation

- [Project overview](yang-db-high-level.md)
- **[Architecture tutorial](docs/tutorial/architecture/README.md)**: [the modules](docs/tutorial/architecture/modules.md) and
  [the life of a query](docs/tutorial/architecture/query-lifecycle.md), from text to results
- Older component notes: [Architecture](docs/info/components/architecture.md) · [Ontology](docs/info/components/ontology.md) ·
  [Query language](docs/info/components/query_language.md) · [Execution planner](docs/info/components/execution_planner.md)
- [REST API](docs/info/api.md)
- **[Benchmark](docs/benchmark.md)**: latency and OpenSearch work per query, compared across releases and OpenSearch versions
- [Revival roadmap](docs/revival/ROADMAP.md) · [Known issues](docs/revival/KNOWN_ISSUES.md) ·
  [Live status](docs/revival/STATUS.md)

### Cypher support notes
- Node label union `(n:A|B)` is not supported (openCypher 9 grammar). Use `MATCH (n) WHERE n:A OR n:B`.
- Cyclic patterns, which close a loop back to an already-matched node, are rejected with an explicit error.
- `RETURN` doesn't narrow the answer: every element of the matched pattern comes back with all its properties,
  whatever `RETURN` lists (`RETURN d.name` returns all of `d` and the rest of the path too). Planned (Q15, ROADMAP 7.11).
- A count filter (`WHERE count(r) > n`) counts all of the start node's relationships; row filters on the far end
  only restrict which neighbours are returned.

## Limitations

- **OpenSearch versions.** `main` and 0.9.x run on OpenSearch **2.19.x** (built and tested against 2.19.7), over its
  REST API. 0.8.x runs on 1.3 (transport client). OpenSearch 3.x is planned (Phase 6). Elasticsearch is no longer
  supported; the last Elasticsearch-based code is the frozen `master` branch.
- **Query languages.** All front-ends are **read-only**: data is loaded through the load API (CSV files or a JSON graph), not
  through queries.
  - **Cypher** is the main language: `MATCH … WHERE … RETURN` over node and relationship patterns, with property
    filters, `AND`/`OR` and count filters. Only `MATCH` queries are translated: no write clauses
    (`CREATE`, `MERGE`, `SET`, `DELETE`), no label unions `(n:A|B)`, no cyclic patterns (see the notes above).
  - **GraphQL** (an extension): queries against a schema generated from the ontology; no mutations.
  - **SPARQL**: `SELECT` over triple patterns, translated against the ontology. It is the least tested front-end:
    it isn't part of the e2e suite, and 17 of its translator tests are ignored (9 use predicates the test ontology
    lacks, 4 have invalid SPARQL text, 4 expect a different translation; the reasons are on each test).
  - **V1**, the native JSON query format, covers what the planner supports. "SQL support" is a DDL importer that turns
    table definitions into an ontology; it doesn't run SQL queries.
- **Consistency.** By default each search of a query sees the index as it is when that search runs, so a query's
  steps and later pages can see writes made in between. For one snapshot per query, set `opensearch.paging.mode = pit`
  in `application.conf`, or send `"consistent": true` in a cursor request: the query then reads an OpenSearch Point in
  Time (it holds a server-side context per shard while the cursor has pages left; OpenSearch allows 300 per node).
- **Security.** YANG-DB's REST API has **no authentication or authorization** and no multi-tenancy: run it on a
  trusted network, or behind a gateway that adds them. It connects to OpenSearch over HTTP or HTTPS
  (`https://host:port` in `opensearch.hosts`) but can't send credentials, so the cluster must accept unauthenticated
  requests from it. The quickstart disables the OpenSearch security plugin: for local use only.
- **Scale.** Each query keeps its results in the engine's memory until it's deleted or unused for its TTL (5 minutes by default),
  and a single CSV load is one bulk request. Measured numbers are in the [benchmark](docs/benchmark.md); open issues
  in [KNOWN_ISSUES.md](docs/revival/KNOWN_ISSUES.md).

## Related repositories

- [YANG-DB/knowledge-graph](https://github.com/YANG-DB/knowledge-graph) (formerly `open-graph`): an earlier
  experiment with a knowledge-graph engine on OpenSearch, last updated in March 2023 and not maintained. This
  repository, `yang-db`, is the active project.

## Contributing

Contributions are welcome — see [CONTRIBUTING.md](CONTRIBUTING.md) and the [Code of Conduct](CODE_OF_CONDUCT.md).
Open pull requests against `main`; `master` is a frozen 2019 snapshot.

## License

[Apache License 2.0](LICENSE)

## Maintainer

YANG-DB is created and maintained by **Lior Perry** —
[LinkedIn](https://www.linkedin.com/in/lior-perry-yang-db/) · [Portfolio](https://yang-db.github.io/yang-db/portfolio.html) ·
[OpenSearch community](https://opensearch.org/author/lior-perry/)
