# The Dragons tutorial: from zero to graph queries

The Dragons tutorial runs YANG-DB on your machine, loads a small fantasy world into it, and queries it as a graph
with Cypher. The world has 1,000 dragons, 100 people, 500 horses, 8 kingdoms and 15 guilds, plus about 20,000
relationships between them: who owns, knows, fires on or freezes whom. This page takes you through the whole path;
each step links to a detailed page.

**Time:** about 15 minutes, most of it the first build.

| Step | What you do | Details |
|---|---|---|
| 1 | Build and start YANG-DB with OpenSearch in Docker | this page, [docker/README.md](../../../../docker/README.md) |
| 2 | Load the Dragons ontology and data | [create-ontology.md](create-ontology.md), [load-data.md](load-data.md) |
| 3 | Ask graph questions in Cypher | [query-the-data.md](query-the-data.md), [queries/Queries.md](queries/Queries.md) |
| 4 | Count and materialize results with cursors | [projection-and-count.md](projection-and-count.md) |

---

## Prerequisites
- **Docker** with the `docker compose` plugin, and at least **6 GB of memory** for Docker.
- **JDK 11** and **Maven 3.6+**: the image is built from source.
- **Python 3**, optional, for the one-command loader and the verification script (standard library only).
- **On Linux**, OpenSearch needs `sudo sysctl -w vm.max_map_count=262144`. Docker Desktop on macOS and Windows
  already sets it. With colima on macOS, run `colima ssh -- sudo sysctl -w vm.max_map_count=262144` after
  `colima start --memory 6`.

## Step 1: build and start
```bash
git clone https://github.com/YANG-DB/yang-db.git
cd yang-db/docker
./buildDocker.sh Dragons                         # Maven build (skips tests) + Docker image yangdb/yang.db:Dragons
docker compose -f docker-compose-os.yml up -d    # OpenSearch 1.3.0, OpenSearch Dashboards, YANG-DB
curl http://localhost:8888/fuse/health           # "Alive And Well..." once YANG-DB is up (about a minute)
```

| Service | URL |
|---|---|
| YANG-DB (REST API) | http://localhost:8888/fuse |
| **Dragons playground** | http://localhost:8888/dragons/swagger |
| Full API (Swagger) | http://localhost:8888/swagger# |
| OpenSearch | http://localhost:9200 |
| OpenSearch Dashboards | http://localhost:5601 |

## Step 2: load the ontology and the data
YANG-DB needs to know the **ontology**: the entity and relationship types, defined in
[Dragons.json](../../../../distribution/resources/assembly/Dragons/ontology/Dragons.json). It then creates the
OpenSearch indices for it and loads the [CSV files](csv/). Pick one of three ways:

**A. One click, in the playground.** Open http://localhost:8888/dragons/swagger and execute
`POST /dragons/tutorial/load`. The response lists every file with its rows, successes and failures.

**B. One command.**
```bash
python3 docker/e2e/load_tutorial.py --reset      # from the repository root
```
It runs the same sequence and then checks that the stored document counts match the CSVs.

**C. Step by step with the REST API**, to see what happens:
```bash
cd yang-db
curl 'http://localhost:8888/fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId'
curl -X POST -H 'Content-Type: application/json' \
  --data-binary @distribution/resources/assembly/Dragons/ontology/Dragons.json http://localhost:8888/fuse/catalog/ontology
curl http://localhost:8888/fuse/load/ontology/Dragons/init
curl -X POST -H 'Content-Type: text/csv' --data-binary @docs/tutorial/sample/dragons/csv/Dragons.csv \
  'http://localhost:8888/fuse/load/ontology/Dragons/csv/load?directive=INSERT&type=vertex&label=Dragon'
# ...and so on for every file in csv/ (see load-data.md for the list, types and labels)
```
[create-ontology.md](create-ontology.md) explains the ontology and its physical layout, including the time-partitioned
`Fire` and `Freeze` indices. [load-data.md](load-data.md) covers the CSV loader, `INSERT` vs `UPSERT`, the expected
counts, and the data generator.

## Step 3: query the graph
**In the playground:** open `GET /query/cypher/run` at http://localhost:8888/dragons/swagger, pick a query from the
**Examples** list (e.g. *Q5 - Dragons that fired on (or were fired on by) a black dragon*) and press **Execute**.

**With curl:**
```bash
curl 'http://localhost:8888/fuse/query/cypher/run?ontology=Dragons&cypher=MATCH%20(d:Dragon)%20RETURN%20*'
```

[query-the-data.md](query-the-data.md) walks through the queries step by step: directions, filters, counts,
`distinct`, and multi-pattern questions. [queries/Queries.md](queries/Queries.md) has more examples, including untyped
matches and what is not supported (label union `(n:A|B)`, cyclic patterns).

## Step 4: cursors
A query's results are read through a **cursor**: paths, a graph, CSV, only the counts, or materialized into a
projection index. [projection-and-count.md](projection-and-count.md) shows the `count` and `projection` cursors.

---

## Check that everything works
```bash
python3 docker/e2e/doc_queries.py --load --reset
```
This loads the data and runs every query of this tutorial, comparing each result with what is actually stored in
OpenSearch. It also checks the cursor types and the playground. `ALL PASS` means your setup matches the docs.
Maintainers run the same suite before a release (see [docker/e2e](../../../../docker/e2e/README.md)).

## Troubleshooting
| Symptom | Fix |
|---|---|
| `health` never answers, `docker ps` shows `opensearch` restarting | Give Docker more memory, and set `vm.max_map_count` (see Prerequisites) |
| Ports 8888, 9200 or 5601 already in use | Stop whatever uses them, or change the left-hand side of `ports:` in `docker/docker-compose-os.yml` |
| Loading again reports only failures (`version conflict`) | `INSERT` never overwrites. Reload with `reset=true` (playground, the default) or `--reset` (script), or use `directive=UPSERT` |
| A query returns `Cyclic patterns are not supported` or an unsupported-syntax error | Rewrite as shown in [queries/Queries.md](queries/Queries.md) |
| `buildDocker.sh` fails | It stops on the first Maven error; run `mvn clean install -DskipTests -P core,dragons-assembly` from the repository root to see it |

To stop: `docker compose -f docker/docker-compose-os.yml stop` (keeps the data) or `down -v` (deletes it).

> [installation.md](installation.md) is the old manual installation (Elasticsearch 6.5) and no longer works with the
> current build. Use the steps above.
