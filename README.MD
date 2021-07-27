From Graph to Elastic - A tail of a Yang-DB...
=========================================================================================

[![Coverage Status](https://coveralls.io/repos/github/YANG-DB/yang-db/badge.svg?branch=develop)](https://coveralls.io/github/YANG-DB/yang-db?branch=develop)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)
[![](https://jitpack.io/v/YANG-DB/yang-db.svg)](https://jitpack.io/#YANG-DB/yang-db)
[![](https://img.shields.io/docker/pulls/yangdb/yang.db?style=flat-square)](https://hub.docker.com/u/yangdb)

* Add [yang.db](https://github.com/YANG-DB/yang-db) as maven dependency https://jitpack.io/#YANG-DB/yang-db
* Download [yang.db](https://github.com/YANG-DB/yang-db) get latest docker release  docker pull yangdb/yang.db

### Run 
* Download V0.5 zip file (https://github.com/YANG-DB/yang-db/releases/tag/v0.5)
* Download [yang.db] (https://github.com/YANG-DB/yang-db) latest docker release docker pull yangdb/yang.db
* Run: docker run -p 8888:88 --net=host -it yangdb/yang.db:latest (https://hub.docker.com/r/yangdb/yang.db/tags)

#### Latest News
 * opensearch community lecture given on yangDb
 * https://github.com/YANG-DB/yang-db/blob/develop/docs/info/YangDbOpenSearch.pdf
 * https://github.com/YANG-DB/yang-db/blob/develop/docs/info/KnowledgeGraphDeck.md


#### Project YANG-DB
* http://www.yangdb.org/ 

* [wiki](https://github.com/YANG-DB/yang-db/wiki)
* [Version Info](https://github.com/YANG-DB/yang-db/wiki/Version-Features-Summary)
* [Release.notes](https://github.com/YANG-DB/yang-db/wiki/V0.51---Relaese.notes)

##### Members:

* [Lior Perry](https://www.linkedin.com/in/lior-perry-62135314/) 

* [Roman Margolis](https://www.linkedin.com/in/roman-margolis-2b181531/)

* [Moti Cohen](https://www.linkedin.com/in/moti-cohen-8815a577/) 

* [Elad Wies](https://www.linkedin.com/in/elad-weiss-0b588525/) 

* [Shimon Benoshti](https://www.linkedin.com/in/benishue/) 

##### Contributors:

* [Lior Kogan](https://www.linkedin.com/in/liorkogan/) 

##### Evangelist:

* [Dana](https://www.linkedin.com/in/dana-perry-51148334/) 

##### License:
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/apache/opennlp/master/LICENSE)

##### Code Coverage:
[![Coverage Status](https://coveralls.io/repos/github/YANG-DB/yang-db/badge.svg?branch=develop)](https://coveralls.io/github/YANG-DB/yang-db?branch=develop)

##### Dependencies Tags:
[![](https://jitpack.io/v/YANG-DB/yang-db.svg)](https://jitpack.io/#YANG-DB/yang-db)


Infrastructure Technologies  
-----------------------------------------------------------------------------------------------

* [elasticsearch](https://www.elastic.co/)
* [jooby](https://jooby.org/)
* [tinkerpop](http://tinkerpop.apache.org/)

Introduction
------------
> A Post introducing our new Open source initiative for building a Scalable Distributed Graph DB Over Elasticsearch
> <https://www.linkedin.com/pulse/making-db-lior-perry/>

> Another usage of Elasticsearch as a graph DB
> https://medium.com/@imriqwe/elasticsearch-as-a-graph-database-bc0eee7f7622 

The world of graph databases has had a tremendous impact during the last few years, in particularity relating to social networks and their effect of our everyday activity.

The once mighty (and lonely) RDBMS is now obliged to make room for an emerging and increasingly important partner in the data center: the graph database.

Twitter’s using it, Facebook’s using it, even online dating sites are using it; they are using a relationship graphs. After all, social is social, and ultimately, it’s all about relationships.

There are two main elements that distinguish graph technology: storage and processing. 

Graph DB - Storage
------------------

Graph storage commonly refers to the structure of the database that contains graph data.

Such graph storage is optimized for graphs in many aspects, ensuring that data is stored efficiently, keeping nodes and relationships close to each other in the actual physical layer.

Graph storage is classified as non-native when the storage comes from an outside source, such as a relational, columnar or any other type of database (most cases a NoSQL store is preferable) 

Non-native graph databases usually comprise of existing relational, document and key value stores, adapted for the graph data model query scenarios.

Graph DB - Processing
---------------------

Graph Processing includes accessing the graph, traversing the vertices & edges and collecting the results.

A traversal is how you query a graph, navigating from starting nodes to related nodes, following relationships according to some rules.

finding answers to questions like "what music do my friends like that I don’t yet own?"

Graph Models
------------

One of the more popular models for representing a graph is the Property Model.

![](https://media.licdn.com/dms/image/C4E12AQH552USMnUS1Q/article-inline_image-shrink_1000_1488/0?e=1547683200&v=beta&t=R78C65in40IFykehY0OCTILpNQLGdGe82kd0yRQFUJ8)

### Property model

This model contains connected entities (the nodes) which can hold any number of attributes (key-value-pairs).
### Nodes 

Nodes have a unique id and list of attributes represent their features and content.

Nodes can be marked with labels representing their different roles in your domain. In addition to relationship properties, labels can also serve metadata over graph elements.

Nodes are often used to represent entities but depending on the domain relationships may be used for that purpose as well.


### Relationships 

Relationship is represented by the source and target node they are connecting and in case of multiple connections between the same vertices – additional label of property to distinguish (type of relationship)

Relationships organize nodes into arbitrary structures, allowing a graph to resemble a list, a tree, a map, or a compound entity — any of which may be combined into yet more complex structures.

Very much like foreign keys between tables in relational DB model, In the graph model relationship describes the relations between the vertices.

One major difference in this model (compared to the strict relational schema) is that this schema-less structure enables adding / removing relationship between vertices without any constraints.

Additional graph model is the Resource Description Framework (RDF) model.


Why Elastic
===========
![](https://media.licdn.com/dms/image/C4E12AQFR7RVJvIIVxA/article-inline_image-shrink_1000_1488/0?e=1547683200&v=beta&t=thjzxR2XgRNw1ldZCaRXzn4K7Nuc-giG0zAdtUIW7XU)

Our use-case is in the domain of the social networks. A very large
social graph that must be frequently updated and available for both:

-   simple (mostly textual) search

-   graph based queries.

All the read & write are made in concurrency with reasonable response
time and ever growing throughput.

The first requirement was fulfilled using Elasticsearch – a well
known and established NoSql document search and storage engine capable
of containing very large volume of data.

For the second requirement **we decided that our best solution would be to
use elasticsearch as the non-native graph-DB storage layer**.

As mentioned before, a graph-DB storage layer can be implemented using a
non-native storage such as NoSql storage.

*In future discussion I’ll get into details why the most popular
community alternative for graph-DB – Neo4J, could not fit our needs.*

##### 

Modeling data as graph
----------------------

The first issue on our plate is to design the data model representing the graph, as a set of vertices and edges.

With elastic we can utilize its powerful search abilities to efficiently fetch node & relation documents according to the query filters.

##### Elastic Index

In elasticsearch each index can be described as a table for a specific
schema, the index itself is partitioned into shared which allow scale
and redundancy (with replicas) across the cluster.

A document is routed to a particular shard in an index using the
following formula:

*shard_num = hash(_routing) % num_primary_shards*

Each index has a schema (called type in elastic) which defines the
documents structure (called mapping in elastic). Each index can hold
only a single type of mapping (since elastic 6)

The vertices index will contain the vertices documents with the
properties, the edges index will contain the edges documents with their
properties.


Query Language
--------------

The way we describe how to traverse the graph (data source)

There are few graph-oriented query languages:

-   [Cypher](https://en.wikipedia.org/wiki/Cypher_Query_Language) is
    a query language for
    the [Neo4j](https://en.wikipedia.org/wiki/Neo4j) graph database (see
    openCypher initative)

-   [Gremlin](https://en.wikipedia.org/wiki/Gremlin_(programming_language)) is
    an [Apache Software
    Foundation](https://en.wikipedia.org/wiki/Apache_Software_Foundation) graph
    traversal language for OLTP and OLAP graph systems.

-   [SPARQL](https://en.wikipedia.org/wiki/SPARQL) is a query
    language
    for [RDF](https://en.wikipedia.org/wiki/Resource_Description_Framework) [graphs](https://en.wikipedia.org/wiki/Graph_(discrete_mathematics)).

Some of the languages are more pattern based and declarative, some are
more imperative – they all describe the logical way of traversing the
data.

Cypher
------

Let’s consider Cypher - a declarative, SQL-inspired language for
describing patterns in graphs visually using an ascii-art syntax.

It allows us to state what we want to select, insert, update or delete
from our graph data without requiring us to describe exactly how to do
it.

![alt text](https://s3.amazonaws.com/dev.assets.neo4j.com/wp-content/uploads/cypher_pattern_simple.png)


From logical to physical
------------------------

Once a logical query is given we need to translate it to the physical layer
of the data storage which is elasticsearch.

Elastic has a query DSL which is focused on search and aggregations –
not on traversing, we need an additional translation phase that will
take into account the schematic structure of the graph (and the
underlying indices).

Logical to physical query translation is a process that involves few
steps:

-   validating the query against the schema

-   translating the labels into real schema entities (indices)

-   creating the physical elastic query

This is the process in a high-level review, in practice - there will be
more stages that optimize the logical query; in some cases it is
possible to create multiple physical plans (execution plans) and rank
them according to some efficiency (cost) strategy such as count of
elements needed to fetch...


Conclusion
---------------
We started with discussing the purpose of graphs DB in today’s business use cases and reviewed different models for representing a graph. Understanding the fundamental logical building blocks that a potential graph DB should consist and discussed an existing NoSql candidate to fulfill the storage layer requirements.

Once we selected elasticsearch as the storage layer we took the LDBC Social Network Benchmark graph model and simplified it to be optimized in that specific storage. We discussed the actual storage schema with the redundant properties and reviewed cypher language to query the storage in an sql-like graph pattern language.

We continued to see the actual transformation of the cypher query into a physical execution query that will run by Elasticsearch.

In the last section we took a simple graph query and drilled down into the details of the execution strategies and the bulking mechanism.

Start Using
---------------
 - See [Version Info](https://github.com/YANG-DB/yang-db/wiki/Version-Features-Summary)
 - See http://www.yangdb.org 
 - See http://www.yangdb.org/general-info.html
 - See http://www.yangdb.org/docker.html
 - See http://www.yangdb.org/get-involved.html

Please review the following tutorial pages:
---------------
Installaiton tutorial:

 - https://github.com/YANG-DB/yang-db/blob/develop/docs/tutorial/sample/dragons/installation.md

Schema creation tutorial:

 - https://github.com/YANG-DB/yang-db/blob/develop/docs/tutorial/sample/dragons/create-ontology.md

Data Loading tutorial:

 - https://github.com/YANG-DB/yang-db/blob/develop/docs/tutorial/sample/dragons/load-data.md

Query the Graph tutorial:

 - https://github.com/YANG-DB/yang-db/blob/develop/docs/tutorial/sample/dragons/query-the-data.md

Projection materialization & count tutorial:

 - https://github.com/YANG-DB/yang-db/blob/develop/docs/tutorial/sample/dragons/projection-and-count.md


