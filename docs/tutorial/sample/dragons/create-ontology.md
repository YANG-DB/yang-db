## Create an Ontology

Since we completed the installation phase (see installation documentation) we can create our
First graph ontology for the Dragons scenario.

Lets review the main entities for HBO's Game of Thrones:
![entities](img/game-of-thrones-entities-diagram.png)

We can list the entities with respect to their functional nature : 
 - Nodes that represent labeled elements with properties
 - Edges represent labeled relationship between nodes (also having properties)


_Side note_ - only a portion of the entities are inside the ontology since we wanted to keep the
entire picture simple and clear to unfamiliar people ...


#### Nodes

 - People
 - Dragons
 - Horses
 - Kingdoms
 - Guilds
 - Professions (stored inside the Person documents, not as an index of their own)
---
#### Edges

 - **Owns** 
   
    -- Person => Dragon
   
    -- Person => Horse

   
 - **Know** 
   
    -- Person => Person

   
 - **MemberOf** 
   
    -- Person => Guild
   
   
 - **Fire** 
   
    -- Dragon => Dragon
   
   
   
 - **Freeze** 
   
    -- Dragon => Dragon
   
   
 - **OriginatedIn** 
   
    -- Person => Kingdom

    -- Dragon => Kingdom
   
    -- Horse => Kingdom

 - **SubjectOf**

    -- Person => Kingdom

 - **RegisteredIn**

    -- Guild => Kingdom

    -- Horse => Kingdom

 - **HasProfession**

    -- Person => Profession

---

      
### Ontology File
This ontology is represented in YANG-DB using a json file which is present under the ontology folder (Dragons.json)

New ontologies can be added freely using the REST API: http://localhost:8888/swagger#/operations/catalog/post_catalog_ontology 

We will focus on the creation of new ontologies in another tutorial. With curl, registering the Dragons ontology is:
```bash
curl -X POST -H 'Content-Type: application/json' --data-binary @Dragons.json http://localhost:8888/fuse/catalog/ontology
```

##### Visualize
If you would like to visualize the logical ontology - a special REST API exits just for that...
http://localhost:8888/swagger#/operations/catalog/get_catalog_ontology__id__visualize 

The next svg file (result of the visualization API call) visualize our Dragons ontology structure:
![ontology](img/illustrateGraphDragonsOntology.svg)


### Physical Schema
Since we have a logical file representing the ontology - we would like to make use of it to generate the appropriate
OpenSearch index templates and actual indexes so that we can upload the graph data.

This part is done using the next REST API : http://localhost:8888/swagger#/operations/init/get_load_ontology__id__init

The full sequence on a fresh stack, which [docker/e2e/load_tutorial.py](../../../../docker/e2e/load_tutorial.py) runs
for you:
```bash
curl 'http://localhost:8888/fuse/idgen/init?names=Entity,Relation,Evalue,Rvalue,workerId'   # id generator, once
curl -X POST -H 'Content-Type: application/json' --data-binary @Dragons.json http://localhost:8888/fuse/catalog/ontology
curl http://localhost:8888/fuse/load/ontology/Dragons/init                                  # templates + indexes
```

This is the swagger screen:

![init-schema](img/yangdb-init-es-schema.png)

We can see the result response replied with the number of indexes it created

Lets see the actual indexes (`curl 'localhost:9200/_cat/indices?v'`, or OpenSearch Dashboards on http://localhost:5601; the screenshot below is from an older setup):
![cerebro-schema](img/cerebro-yandb-dragons-indexes.png)

We can see the indexes with their default shards and replica setting as taken from the default yangDb's configuation.

We can also examine the index mapping template to understand the schema physical structure as it is derived from the logical ontology.

##### Physical Schema Considerations
The actual structure and layout of the index representing logical entities and edges is a complex task since it may take into account
many considerations such as performance, size, structure and such. 

These different considerations are actually configurable by the client using a file that describe these exact physical complexities - in our case
of the Dragons schema it is called DragonsIndexProvider.conf and its location is under the indexProvider folder in the yangDb installation.

The DragonsIndexProvider.conf file describes the actual indexes and their partitions and structure - this file may be auto generated from the logical schema 
with reasonable defaults and is also may be manually crafted for specific performance & structure needs. 

For example, `Fire` and `Freeze` are time-partitioned by their `date` into the range buckets `500`, `1000`, `1500` and `2000`
(each value is the inclusive upper bound of its bucket): a fire in 1950 is stored in `idx_fire_2000`, one in 1000 in
`idx_fire_1000`, and a date after 2000 is rejected at load time. See [IndexProvider.md](../../../info/components/IndexProvider.md#time-buckets).

_The physical schema part is explained in depth in a separate tutorial..._

### What's next ?
Now that we have initiated and created the ontology and the associated indexes in OpenSearch our next tutorial will deal with loading the graph data
