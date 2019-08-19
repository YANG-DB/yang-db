From Graph to Elastic - A tail of a Yang-DB...
=========================================================================================
[![Release](https://jitpack.io/v/YANG-DB/Repo.svg)]
(https://jitpack.io/#YANG-DB/yang-db)
### Use 
* Add [yang.db](https://github.com/YANG-DB/yang-db) as maven dependency https://jitpack.io/#YANG-DB/yang-db
* Download [yang.db](https://github.com/YANG-DB/yang-db) latest docker release  https://cloud.docker.com/repository/docker/tom4tomato/yang.db


#### Project YANG-DB

* [wiki](https://github.com/YANG-DB/yang-db/wiki)

##### Members:

* [Lior Perry](https://www.linkedin.com/in/lior-perry-62135314/) 

* [Roman Margolis](https://www.linkedin.com/in/roman-margolis-2b181531/)

* [Moti Cohen](https://www.linkedin.com/in/moti-cohen-8815a577/) 

* [Elad Wies](https://www.linkedin.com/in/elad-weiss-0b588525/) 

* [Shimon Benoshti](https://www.linkedin.com/in/benishue/) 

##### Contributors:

* [Lior Kogan](https://www.linkedin.com/in/liorkogan/) 

##### Dev/Ops
 
* [Yaniv Eliash](https://www.linkedin.com/in/yaniv-eliash-4039a951)
##### License:

[Apache License 2](http://www.apache.org/licenses/LICENSE-2.0.html)

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

### RDF model

At the core of RDF is this notion of a triple, which is a statement
composed of three elements that represent two vertices connected by an
edge.

It’s called *subject-predicate-object:*

-   Subject will be a resource, or a node in the graph.

-   Predicate will represent an edge – a relationship.

-   Object will be another node or a value. 

Resources (vertices/literal values) and relationships (edges) are
identified by a URI, which is a unique identifier. This means that
neither nodes nor edges have an internal structure; they are purely a
unique label.

When representing data in RDF with triples, we’re breaking it down to
the maximum. Doing a complete stripping of our data, and we end up
finding nodes in the graph that are resources and literal values.

This is a great difference from the property model which allows
attributes to reside on the graph elements themselves.

Traversal
=========

A traversal is how you query a graph, navigating from starting nodes to related nodes, finding answers to questions like "what music do my friends like that I don’t yet own?"

Traversing a graph means visiting its nodes, following relationships according to some rules. In most cases only a subgraph is visited.

The result of traversing is a projection of the resulted sub-graph. The results can take a tabular form, a sub-graph form or a list of paths - (list of graph elements).

Query 
======

Most of the time, graph searching involve some type of filter on property values – whether edge property or relation property.

To enable an efficient search on a large-scale graph, an index must be created for each property type.


##### Possible property types are:

*Strings, Numbers (Integers, Floats), Date, Complex-Fields:(Geo-Location)* 

##### Possible filter Criteria:

*Equal(=), LTE (>;=), STE(&lt;=), In Set , In-Rage (Geo-Search),
contains (\*), fuzzy (~=)*

When we need to filter nodes using text / Date / Range / Geo search we
must consider the proper usage of a dedicated index that will be the
most efficient in terms of search time.

Combining different search criteria introduce different ways to traverse
the graph, therefor some planner is needed to smartly select the best
filter order for executing the query.

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

LDBC Social Network Benchmark
-----------------------------

For purpose of this article we will model and run our simulation over
the famous LDBC Social Network Benchmark (SNB).

This graph models a social graph, and three different workloads based on
top of it. Let’s review the schema (simplified for this article)

#### Domain entities
![](https://media.licdn.com/dms/image/C4E12AQEYjHioWmFiZQ/article-inline_image-shrink_1500_2232/0?e=1547683200&v=beta&t=QxczQsdb6HOxjAkZNfzYfvCMQ-cJvzd7KMRk8offa9o)


##### Person
 * [name ,age ,gender ,birthdate ,Joined ,status ,email ]

##### Post
 * [ content ,language , imageFile ]

##### Location
 * [coordination,country ,type (city/village) ] 

#### Forum
 * [title ,creationDate ] 

#### Tag
 * [type ]

#### Domain Relations
* Person -[**Tagged**]->Location
    * _Date_
    * _Content_   
* Person -[**Friend**]->Person
    * _Date_
* Person -[**Member**]->Forum
    * _Since_
* Person -[**Comment | write **]->Post
    * _Date_
    * _Content_
* Person -[**View**]->Post
    * _Date_
* Post -[**in**]->Forum
    * _Date_
* Forum -[**Has**]->Tag


Questions we need to Ask…
-------------------------

The type of questions we need to ask in our business domain are not only
classical graph type of questions, they include relational and
aggregative questions.

For example:

-   Age histogram of post writes on subject tag “cheap flights”

-   People with most viewed posts which has more than 500 friends living
    in from New-York

-   First & Second circle of friends which live in an area with radius
    less than 5 km & viewed/commented about post in the last week

These Sort of questions mix both classical graph traversal queries with
aggregative type filters and grouping.

In addition, we have the more classical type of graph-based algorithms such as recommendations …

Why Are these questions hard to answer?
---------------------------------------

In a native graph store, we have an index-free adjacency access that
gives us fast traversing over the graph, but in many cases the fastest
way to reach the vertices that answer our question is first filtering
out vertices and only after start traversing from the remaining.

In other cases it would be more efficient first to start from a small
vertices group and filter out as we go along.

We need a physical model that can support both heavy indexing based
filtering and aggregative filtering (something that elasticsearch does
best)

We also need to represent the vertices and edges in an efficient way
that will allow us to minimize the fetching of the data per hop.

### 

### Modeling the physical data layer

Remember we need to answer questions that are both:

* Graph-based questions that relay of traversing between vertices according to constraints

* Aggregative questions such as counting relations types cardinality (based on predicate).

#### Modeling the vertices
The next json document will represent the vertex data for person entity:
```json
{
	Type: person,
	Id: e0000000001,
	name: ‘James Jones’
	age: 45
	gender: M
	birthDate: 01/01/1980
	status: Single
	joined: 01/10/2005
	email: jamjon@gmail.com
}
```
In elastic we can create an index for this entity, the index can be partitioned into a predefined number of partitions that will spread and moderate the load, the partition key can be according to the vertex id.

Since we assume our social graph will grow over time we can plan the capacity of each index ahead and limit its size by giving each person-index a time frame – month / quarter / year based in join time.

Apart from the edge properties and id attributes, we introducing data redundancy to help reduce the need to fetch all the vertices on the other side (given some filter exists on their properties).

#### Modeling the edge
![](https://media.licdn.com/dms/image/C4E12AQGcDT2vo15FDw/article-inline_image-shrink_1500_2232/0?e=1547683200&v=beta&t=525ERGd9LnUIBQ-lyTPPZqmrkX3BB8Duo6cFC4dvQv8)

Friend Edge document:
```json
{
	Type: "friend",
    	Source_Id: "e0000000001",
    	Target_Id: "e0000000001",
    	becameFriend: "01/10/2010"
    
    	Source_name: ‘…’
    	Source_age: 45
    	Source_gender: 'M'
    	Source_birthDate: "01/01/1980"
    	Source_status: "Single"
    	Target_name: ‘…’
    	Target _age: '35'
    	Target _gender:'F'
    	Target _birthDate: "01/01/1990"
    	Target _status: "Married"
}
```


### Modeling the physical data with redundancy 

Redundancy allows us in the case of 'friend-of-friend' query given a filter on age, to push-down the age filter into elastic itself and significantly reducing the amount of target vertices need to be fetched during the traversing.

This type of performance Technic has a cost in terms of storing non-normalized data structure that must be constantly updated in both edge and vertices indexes.

Vertices Vs. Edges in large Social Graphs
-----------------------------------------

In a study made by facebook (named
three-and-a-half-degrees-of-separation) it was claimed that Each person
in the world (at least among the 1.74 billion people active on Facebook)
is connected to every other person by an average of three and a half
other people. 

In this highly connected graph the amount of the edges far outstands the
amount of vertices; Facebook statistics page
[facebook-statistics](<https://www.omnicoreagency.com/facebook-statistics/>) claims an
Average Facebook user has [155 friends](http://www.telegraph.co.uk/news/science/science-news/12108412/Facebook-users-have-155-friends-but-would-trust-just-four-in-a-crisis.html)
(two orders of magnitude).

This has a tremendous effect on our planning of the physical storage
layer – we need to consider how we store the edges, what data we need to
make redundant, how we allow even data distribution on our cluster and
such…

### Scale free Graph

In the course of planning the physical storage layer we also need to
take into consideration that the graph has a scale free behavior –
meaning there will be hubs in the data (highly connected vertices).

The presence of hubs (supper nodes) will give the [degree
distribution](https://mathinsight.org/degree_distribution) a long tail,
indicating the presence of nodes with a much
higher [degree](https://mathinsight.org/definition/node_degree) than
most other nodes.

The fact that in certain queries the responsiveness might reduce
severely, therefore we need a way to address this problem efficiently.

For example, when traversing a supper node like a major city or a supper
famous actor unless we have a very strong filter condition we might
fetch a huge number of connected vertices…

Properties vs vertices
----------------------

Additional issue to consider is the properties vs vertices – it is
possible to model the city a person livs in as a property of the person.
This can simplify indexing and allow ‘in-place’ filtering, the negative
side will be the difficulty to change the model.

For example, adding a property to the city itself (country, zip-code)
this would suggest that the better choice will be to model the city as a
separate entity connected to a person by an edge.

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

Vertices
--------

Cypher uses ASCII-Art to represent patterns. Surround nodes with
parentheses which look like circles, e.g. (node).

To Refer the node, we’ll give it a variable like (p)for person
or (t) for thing. In real-world queries, we’ll probably use longer,
more expressive variable names like (person) or (thing).

If the node is not relevant to your question, you can also use empty
parentheses ().

Relationships
-------------

Relationships are basically an arrow -->; between two nodes.
Additional information can be placed in square brackets inside of the
arrow.

-   relationship-types like -[:KNOWS|:LIKE]->;

-   a variable name -[rel:KNOWS]->; before the colon

-   additional properties -[{since:2010}]->;

-   For paths of variable length -[:KNOWS\*..4]->;
    

> MATCH (n1:Person)-[rel:Comment]->;(n2:Post)
>
> WHERE rel.date >; {value}
>
> RETURN rel, type(rel)



#####The above query describes the next pattern:

Find a Person - we will tag him as ‘*n1*’

\- Comments - has a relationship of type *Comment* tagged as *‘rel’*

> \- a Post – we will tag it as *‘n2’*


The relation tagged as ‘*rel’* must follow the constrains *date* > {some date value} 
meaning the person commenting on a post must have done it after some date.

This cypher example shows the simplicity of using such declarative traversing language and we will use it though our examples.

From logical to physical
------------------------

Once such a query is given we need to translate it to the physical layer
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


![alt text](https://mapr.com/blog/using-apache-spark-dataframes-processing-tabular-data/assets/blogimages/blog_SparkDataframes_image3.png)

The above picture presents the processing of a graph query into physical
execution plan in a fully blown query engine (Spark's Catalyst).

This specific pipeline involves:

1.  Parsing the Query text to an abstract syntax tree (AST)

2.  Validating and resolving the AST against the logical schema catalog

3.  Creating a logical execution plan based on the AST steps

4.  Creating a physical execution plan based on the logical plan
    and an efficiency planner

#### Schema mapping

Mapping the labels and properties against the indices and properties –
we know the exact index names of every label (entity) and which
properties the index contains, including the redundant properties.

Let’s take the next example:

> MATCH (n1:Person)-[rel:Comment]->;(n2:Post)
>
> WHERE rel.date >; 2010/01/01
>
> RETURN n1.name, rel.content, n2.title

In our domain we define the index-type-properties document that will
help us:

#### Person schema definition:
```javascript
{
 Label: "person",
    Type: "vertex"
    Indices: ["p2017_Oct", "p2017_Nov", "p2017_Sep"]
    Properties: {
        Id: "keyword",
        name: "string",
        age: "integer",
        gender: "enum[F,M]",
        birthDate: "date",
        status: "enum[S,M,D]",
        joined: "date",
        email: "string"
     }
}
```

In our example the indices are monthly time base, each index contains
entities created on that specific month.

#### POST schema definition:

```javascript
{
    Label: "POST",
    Type: "vertex"
    Indices: ["post2017_Oct", "post2017_Nov", "post2017_Sep"]
    Properties: {
        Id: "keyword",
        content: "string",
        language: "string"
        imageFile: "integer"
     }
}
```

#### Comment relation schema definition:
```javascript
{
  Label: "Comment",
  Type: "edge",
  Indices: ["comment2017_Oct", "comment2017_Nov", "comment2017_Sep"],
  sideATypes: ["person"],
  sideBTypes: ["post"],
  Properties: {
    Id: "keyword",
    sourceId: "keyword",
    targetId: "keyword",
    date: "date",
    content: "string"
    },

  SideAProperties: {
    name: "string",
    age: "integer",
    gender: "enum['F','M']",
    birthDate: "date",
    status: "enum['S','M','D']",
    joined: "date",
    email: "string"
    },
 
  SideBProperties: {
    content: "string",
    language: "string",
    imageFile: "integer"
   }
}
```

The pipeline mechanics
----------------------

The former cypher query would eventually produce the following elastic
queries:

1.  get all the people documents and fetch only the name field

2.  get all the comment relationship that

    1.  date value >; 2010/01/01

    2.  *sideA_Id* in set that returned from the first query 1.

3.  For all returned document in query b, take *sideB_Id* and fetch them
    from post indices

Pipelining the query 
---------------------

We see that each logical step translates to a physical step, the step return some results that can be input of the following step thous producing a pipeline.

Each middle step can accept as input a list of id’s (from the former step) it will use to push down to elastic as predicate.

The result of this traversal process is a set of graph elements path’s *([vertice-edge-vertice-edge…])* that fulfill the query.

We project the results as the query defined; each step is tagged (explicitly or implicitly) and when a property is projected we use the tag to find the path step to take the property from.

![](https://media.licdn.com/dms/image/C4E12AQGluIArnwB-aA/article-inline_image-shrink_1000_1488/0?e=1547683200&v=beta&t=tsEy4zxfkm4MlfATi203SlSB-65_6p3SLUBiA3AJljA)

Example results:
```javascript
[(n1:{name:Jon}),(comment:{date:2012/01/02,content:”BS…”}),                   (n2:{title:Inflation})]
[(n1:{name:Jon}),(comment:{date:2014/11/02, content:”and I can approve it “ }),(n2:{title:Inflation})]
[(n1:{name:Dana}),(comment:{date:2015/16/1}), content:”really ?can you …”}),(n2:{title:Pastrama})]
[(n1:{name:Abe}), (comment:{date:2018/13/01}), content:”just think…”}),     (n2:{title:Vine})]
[(n1:{name:Charles}),(comment:{date:2017/5/5}), content:”sowarm”}),          (n2:{title:Summer})]
```
Each row represents a different path along the sub-graph complying with
the query.

Elastic queries
---------------

Let’s review the elastic query for each step:

First step will fetch all entities of type Person:

-   The physical schema resolver will get the indices that hold the
    Person entity ([p2017_Oct, p2017_Nov, p2017_Sep]).

-   Next the projection part will be scanned to see which fields are
    needed

-   Any existing filter will be pushed down (if possible)
```javascript
GET p*/_search
{
"_source": ["name"],
    "query": {
    "match_all": {}
    }
}
```
Returns:


```javascript
'hits':[
 { "_index": "p2017_Sep ", "_type": "person", "_id": "p0001",…,"_source": { "name": "jon" } },
 { "_index": " p2017_Sep", "_type": "person", ", "_id": "p0002",…,"_source": { "name": "dana" } }, …
]
```
The next step will take the id’s that result from the returned hits and
push them down to elastic query

```javascript
GET comment*/_search
{
    "query":{
        "bool":{
        "filter":{
            "terms":{
                "entityAId":["p0001","p0002"…] 
                }
            },
            "must":{
                "range":{
                    “date”:{
                            "gte":”2010-01-01 00:00”,
                            "format": "yyyy-MM-dd HH:mm"
                            }
                    }
                }
            }
        }
}
```

The final step will take the side_B id’s that result from the former
step and push them down to elastic query

```javascript
GET post*/_search
{
    "query":{
        "bool":{
            "filter":{
                "terms":{
                        "_id":["p0001","p0002"…] 
                        }
                    }
                }
            }
}
```
All the results from the 3 steps are collected into a path object, each
path is unique in the chain of graph elements it has visitede.

Size of Path’s
---------------

It is important to remember that the amount of path’s returned from a traversal is the Cartesian product of the results each step returns; The nature of the path representation is to show all possible ways of traversing the graph.

In a graph of 20 people each having 50 friends will produce 20*50 paths. In some cases, the memory footprint of the resulting paths can be larger than the graph itself, for this reason, we would like to add a bulk mechanism that will limit the resulting paths and fetch them in pages.

Traversing strategies
---------------

In our example we will need to bulk the result to a size of 1000 path’s each. Currently the first step issues a complete scan of the person vertices – this can potentially be exhaustive and therefore we will add ‘from & limit’ to the query…


```javascript
GET p*/_search
{
    "_source": ["name"],
   "from": 0, "size": 1000,
    "query": {
        "match_all": {}
    }
}
```
This will allow us to scroll over the results in a bulk of 1000 elements. During the second step, we will also add the ‘from & limit’ to the query – that will result in an additional scroll over the second step and so on ...

Scroll is used as much the same way as you would use a cursor on a traditional database.

Step Buffering
---------------
In a middle step, having a filter can produce less results than the bulk size – due to the input vertices being filtered out .

In step 2 (get comments step with date predicate) we pushed-down the 1000 initial vertices from the first step, this new step may return less than 1000 comment edges (due to the date predicate).

In such cases we would like to continue fetching results from the datastore until the bulk size is reached.

For this reason a call to the former step is needed to fetch an additional bulk of 1000 elements (person vertices) and an additional fetch comments query until the limit of 1000 comments is reached.

Funneling
---------------
The process in which each step is buffering the result until it reaches the required bulk size is called ‘step buffering’.

This process is recursive in the sense that when a middle step needs more vertices from the former step it calls the former step which in turn can call its former step until possibly reaching the first step or exhausting the data (no more elements found matching the query).

![](https://media.licdn.com/dms/image/C4E12AQHwIKRIdmeyog/article-inline_image-shrink_1500_2232/0?e=1547683200&v=beta&t=Wm39J09DkIOb9mzG1YRagxiZeOiFxaGQIwza6eSmunA)

Each step maintains a state (scroll) over the data it already fetched (position) so it can continue bulking data when it is required.

In some sense each step is a funnel of graph elements, there may be cased where some steps will funnel out almost the entire graph.

![](https://media.licdn.com/dms/image/C4E12AQH_EY1iap7JWg/article-inline_image-shrink_1500_2232/0?e=1547683200&v=beta&t=xonaiyKWuFFdC5tU-PdF4qr2g0gD7M7t_0TxXzLTg0c)

One advantage of this bulking approach is that it is not required to fetch the entire neighborhood of each vertex before it returns.
This graph traversal approach is called DFS – depth first search.

Another advantage in the DB layer is that instead of huge queries running against the db (such in step 1 without the size limit)
we are running many bulked steps which ease the contention on the db and allow higher throughput.

The state of the traversal execution is also detachable from the DB and is stored on the traversing path itself.



**_Example:_**

*   Fetch 1000 people **[1]**
*   While (result < 1000) **[2]**
    *	Former-Results = call(former-step)
    *	For Former-Results fetch comments where date > predicate
    *	results += new results

*   While (result < 1000) **[3]**
    *	Former-Results = call(former-step)
    *	For Former-Results fetch posts
    *	results += new results

This pseudo code describes the compilation of the 3 steps into execution logic – the execution will start from the last 
step backwards until reaching a step that actually fetches data from the DB – (the first step).

**Estimated execution**

We can assume:

* The **Third step** will execute 2 queries against elastic until its buffer is filled
* The **Second step** will run 5 times until its buffer is reached and as a consequence
* The **First step** will run 12 times until its buffer is reached

Resulting in 19 separate calls to elasticsearch (in oppose to only 3 calls without bulking).


Conclusion
---------------
We started with discussing the purpose of graphs DB in today’s business use cases and reviewed different models for representing a graph. Understanding the fundamental logical building blocks that a potential graph DB should consist and discussed an existing NoSql candidate to fulfill the storage layer requirements.

Once we selected elasticsearch as the storage layer we took the LDBC Social Network Benchmark graph model and simplified it to be optimized in that specific storage. We discussed the actual storage schema with the redundant properties and reviewed cypher language to query the storage in an sql-like graph pattern language.

We continued to see the actual transformation of the cypher query into a physical execution query that will run by Elasticsearch.

In the last section we took a simple graph query and drilled down into the details of the execution strategies and the bulking mechanism.

Whats next ?
---------------

In the next post I will review what are the challenges that more complex queries may cause and how we can use a cost-based optimizer to select the best execution plan.

The simulation of the LDBC Social Network Benchmark over elasticsearch using the described graph layer will follow - stay tuned...

*   https://www.linkedin.com/in/lior-perry-62135314/ 
