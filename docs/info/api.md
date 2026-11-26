## YANG-DB REST API

### API reference
Once the engine has started, open the API reference (OpenAPI 3.1, the spec file is
`distribution/resources/assembly/Dragons/public/assets/swagger/openapi.json`):

    http://localhost:8888/swagger
    http://localhost:8888/redocly/redocly

V1 query bodies are specified by a JSON Schema, served at `GET /fuse/query/v1/schema`; see
[docs/spec/v1](../spec/v1/README.md).


YangDb has multiple API endpoints that serve different purposes:

### Management
Allows to query the version & configuration info (`[GET] /fuse/internal/version`, `[GET] /fuse/internal/config`)
and the service health (`[GET] /fuse/health`).

Allows to initiate a DB sequencer used to generate unique ids to the newly generated graph elements
(`[GET] /fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId`; run it once - a second call fails with
"already exists").

### Catalog
Allows insert & query information regarding the catalog & schema - ontology & indices.

The creation of logical ontologies inside the schema model is done here.

 * Add an ontology (V1 ontology JSON) to the catalog / list or get catalog ontologies:

    [POST] http://localhost:8888/fuse/catalog/ontology
    [GET]  http://localhost:8888/fuse/catalog/ontology
    [GET]  http://localhost:8888/fuse/catalog/ontology/{id}

 * List or get the physical schemas (index provider):

    [GET]  http://localhost:8888/fuse/catalog/schema
    [GET]  http://localhost:8888/fuse/catalog/schema/{id}

 * Transform a schema written in another language into a V1 ontology (the body is the schema text):

    [POST] http://localhost:8888/fuse/graphql/ontology/{id}   (GraphQL IDL; from the extensions/graphql plugin)
    [POST] http://localhost:8888/fuse/sparql/ontology/{id}    (OWL)
    [POST] http://localhost:8888/fuse/sql/ontology/{id}       (SQL DDL)


### Query

This is the main query API used to question the Data held by the engine. 

The API supports creating queries in the following languages:

* V1 query language (default language) - `[POST] /fuse/query/v1`, `/fuse/query/v1/run`; also
  `[GET] /fuse/query/v1/schema` (the [JSON Schema](../spec/v1/README.md)) and `[POST] /fuse/query/v1/print`
  (the query as one line of text)
* Cypher query language - `[POST] /fuse/query/cypher`, `[GET] /fuse/query/cypher/run`
* GraphQL language (the extensions/graphql plugin) - `[POST] /fuse/query/graphQL`, `[GET] /fuse/query/graphQL/run`
* SparQL language - `[POST] /fuse/query/sparql`, `[GET] /fuse/query/sparql/run`

There is no SQL query endpoint. Every registered language also answers on `[POST] /fuse/query/{lang}` and
`[GET|POST] /fuse/query/{lang}/run`; an unknown `{lang}` returns 404 with the list of available languages.

Send `Accept: application/json` with every request: without it the query and load endpoints currently answer with a
Java object name (e.g. `QueryResourceInfo@48764515`) instead of JSON.

Each query (in any language) is verified and translated into intermediate language that is used to plan the actual traversal path.

Once a query was created, it needs to define a cursor resource - which is a view on the query results.

#####Cursor
The view (projection) can be of multiple types:

 - Paths (Assignments)
 - Graph 
 - Table (CSV)

Each of the cursors is the responsible for the data fetching, the page resource is the data holder .

### Plan

The plan API enables the user to understand the execution path taken for the query
(`[GET] /fuse/query/{queryId}/plan`, `/plan/print`, `/plan/visualize`; `[GET] /fuse/query/{queryId}/asg` for the
rewritten query).

It is also possible to get the generated traversal used to traverse the data - tinkerpop graph traversal
(`[POST] /fuse/query/v1/traversal` with a V1 query).

### Cursor

The cursor API is the resource which is responsible to fetch the actual data in bulks (pages) and project the results in the requested view
(`[POST] /fuse/query/{queryId}/cursor`).

- Paths - view the data as a list of assignments.

- Graph - view the data as a subgraph.

- Table - view the data as a table (csv)

### Page

The page holds the results in the projected pattern, each page holds a constant number of elements
When fetching the last page it can return a lower number or results or zero elements signaling all the elements
matching the query fetched pattern .

### Data

This is the actual data structure.

`[GET] /fuse/query/{queryId}/cursor/{cursorId}/nextPageData` allows fetching the next page while allowing deletion of the former page is needed.

### GraphApi
This is the standard graph api that is used to ask a set of typical graph queries and operations against the DB

 - find path between vertices
 - get a specific vertex including its properties
 - get a specific vertex including its properties & neighbors

(`[GET] /fuse/query/graph/api/findPath`, `/getVertex`, `/getNeighbors`)
 
   

### Dashboard

This API allows viewing statistical information regarding the amount of graph elements, types and creation times
(`[GET] /fuse/dashboard/entities/{ontology}`, `/fuse/dashboard/created/{ontology}`, `/fuse/dashboard/count`)

### Init

This API is used to generate the physical schema according to the index provider instruction used with the ontology
logical schema.

This API enables both the creation of the indices mapping templates as well as the actual indices
(`[GET] /fuse/load/ontology/{id}/mapping` creates the index templates, `[GET] /fuse/load/ontology/{id}/indices` the
indices; both read the ontology's index provider from the catalog).

### Load
This API is used for initialization of the indices according to the physical ontology;

- init will initialize the tables and tables templates - `[GET] /fuse/load/ontology/{id}/init`
- drop will drop the tables - `[GET] /fuse/load/ontology/{id}/drop`
- load will load a logical graph model or a CSV file into the DB according to the logical ontology -
  `[POST] /fuse/load/ontology/{id}/graph/load`, `[POST] /fuse/load/ontology/{id}/csv/load?type=vertex|edge&label=<type>`

The `directive` parameter of the load / upload calls is `INSERT` (rejects elements whose id already exists) or
`UPSERT` (overwrites them).

### Upload

- upload will load a file/zipfile containing logical graph model into the DB according to the logical ontology -
  `[POST] /fuse/load/ontology/{id}/graph/upload`, `[POST] /fuse/load/ontology/{id}/csv/upload`
