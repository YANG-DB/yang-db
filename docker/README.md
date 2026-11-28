# Running The Dragons Graph
This is the quickstart for YANG-DB: it runs the Dragons sample graph on top of OpenSearch using docker compose.

[docker-compose-os.yml](docker-compose-os.yml) starts three services:
 - OpenSearch 2.19.7 (single node, security plugin disabled - for local testing only)
 - OpenSearch Dashboards 2.19.7
 - Fuse - the YANG-DB graph engine, preloaded with the Dragons ontology

## Prerequisites
- Docker with the `docker compose` plugin
- To build the image yourself: JDK 11 or 21, and Maven 3.6+ (see Requirements in [PROJECT.md](../PROJECT.md#requirements))

## Prebuilt image
Each release from 0.9.1 on publishes `ghcr.io/yang-db/yang.db:<version>` (linux/amd64 and linux/arm64; `latest` is
the latest release). Run it without building anything:
```bash
YANGDB_IMAGE=ghcr.io/yang-db/yang.db YANGDB_TAG=0.10.0 docker compose -f docker-compose-os.yml up -d
```
The `yangdb/yang.db` images on Docker Hub are from 2020 (Elasticsearch era) and are not updated.

The OpenSearch version is set by `OS_VERSION` in [.env](.env) and must match `<opensearch.version>` in the root `pom.xml`.

## Building
From this `docker/` folder run:
```bash
chmod +x buildDocker.sh
./buildDocker.sh Dragons
```

This will:
- Run the maven build (`mvn clean install -DskipTests=true -P core,dragons-assembly`)
- Generate the `distrib/dragon` folder, which holds the jars and the start script for the graph engine (Fuse)
- Build the docker image `yangdb/yang.db:Dragons` from that folder

It takes a couple of minutes once Maven has downloaded the dependencies (the first build downloads about 0.8 GB). The
images take about 5 GB of disk: OpenSearch 2 GB, OpenSearch Dashboards 2.4 GB, YANG-DB 0.8 GB.

## Running the Docker-compose services
```bash
docker compose -f docker-compose-os.yml up -d
```
This starts:
 - OpenSearch - http://localhost:9200
 - OpenSearch Dashboards - http://localhost:5601
 - Fuse (graph engine) - http://localhost:8888

Fuse waits for OpenSearch to report healthy before starting. Check it's up with:
```bash
curl http://localhost:8888/fuse/health
```
Then open the **Dragons playground**, http://localhost:8888/dragons/swagger: `POST /dragons/tutorial/load` loads the
tutorial data in one call, and `GET /query/cypher/run` offers every tutorial query in its *Examples* list. The full
API reference (OpenAPI 3.1) is at http://localhost:8888/swagger.

To stop everything: `docker compose -f docker-compose-os.yml down` (add `-v` to also delete the data volumes).

## Swagger API
The next API references allows access to all the Graph's functionality and configuration.
 - See [Docs](../docs/info/api.md)

![](../docs/info/img/swagger.png)

### Calling the Get Info API
Run `curl http://localhost:8888/fuse`, or navigate to `http://localhost:8888/swagger#/API/get_` and call `execute`.

The next response is expected:
```json5
{
  "requestId": "FR870994186772480000",
  "elapsed": "00000080",
  "renderElapsed": "00000313",
  "totalElapsed": "00000393",
  "data": {
    "resourceUrl": "/fuse",
    "resourceId": null,
    "healthUrl": "/fuse/health",
    "internal": "/fuse/internal",
    "queryStoreUrl": "http://172.27.0.4:8888/fuse/query",
    "searchStoreUrl": "/fuse/search",
    "catalogStoreUrl": "http://172.27.0.4:8888/fuse/catalog/ontology"
  }
}
```
## Dragons Tutorial
For more advanced usage and tutorial please refer to the [Tutorials folder](../docs/tutorial)
- [Dragons Tutorial Ontology](../docs/tutorial/sample/dragons/create-ontology.md)
- [Dragons Tutorial Data](../docs/tutorial/sample/dragons/load-data.md)
- [Dragons Tutorial Query](../docs/tutorial/sample/dragons/query-the-data.md)
- [Dragons Tutorial Projections](../docs/tutorial/sample/dragons/projection-and-count.md)