# Catalog
The purpose of this document is to explain and discuss the need for a generalized metadata catalog .
 - It will discuss the general needs for a data-catalog representation and its usage during query execution and data discovery. 
 - It will review the existing solutions for maintaining a catalog repository in the open source domain.
 - It will define the common internal structure for such a catalog  including the knowledge domain and architecture. 

## Why is a Catalog needed ?

The modern data stores and engines experience an extreme growth in the volume, variety, and velocity of data.
Many of which are 'on demand' data-sets which are partially structured (or not structured at all).
While this type of systems tend to offer some data discovery method to navigate the large amount of the diverse data, 
they sometimes tend to neglect the common practice of generating on-the-fly data structures (projections and materializations) that are 
being used similar to the raw data sets. 

-------------

### Definition

One of the simplest definitions for a data catalog goes as follows:
“Simply put, a data catalog is an organized inventory of data assets in the organization. It uses metadata to help organizations manage their data.
It also helps data professionals collect, organize, access, and enrich metadata to support data discovery and governance.”

In a modern enterprise, though, we have a dazzling array of different types of assets that comprise the landscape:
tables in relational databases or in NoSQL stores, streams in your stream store, features in your AI system, metrics in your metrics platform, dashboards in your  visualization tool, etc. 
The modern data catalog is expected to contain an inventory of all these kinds of data assets. 

### Use Cases

An important question concerns the kind of metadata we want to store in the data catalog, because that directly influences the use cases we can enable.

Some common use cases:

 - Search and Discovery: Data schemas, fields, tags, usage information
 - Validation, Execution & Performance: fields, types, cardinality, selectivity, cost based planning
 - Access Control: Access control groups, users, policies
 - Data Lineage: Pipeline executions, queries, API logs, API schemas
 - Compliance: Taxonomy of data privacy/compliance annotation types
 - Data Management: Data source configuration, ingestion configuration, retention configuration, data purge policies, data export policies
 - Data Ops: Pipeline executions, data partitions processed, data statistics
 - Data Quality: Data quality rule definitions, rule execution results, data statistics


Every individual use case often brings in its own special metadata needs, and yet also requires connectivity to existing metadata brought in by other use cases. 

### In Practice

Metadata can be typically ingested using the following methods:

 - crawling based approach by connecting to sources of metadata like your database catalog, the Hive catalog, the Kafka schema registry, or your workflow orchestrator’s log files
and then writing this metadata into the primary store, with the portions that need indexing added into the search index .
   
This ingestion process is usually running once a day or so. During the ingestion, there is often some transformation of the raw metadata into the semantic metadata model,
because the data is rarely in the exact form that the catalog wants it. Typically, this transformation is embedded into the ingestion job directly. 

This is a pull oriented metadata collection method and the crawler runs in a different environment than the data source and its configuration needs to be managed separately.
So, one set of problems in these pipelines is operational hurdles like network connectivity (firewall rules) or credential sharing (passwords can change).
In addition, this type of metadata ingestion typically leads to workloads that are batchy (nightly refreshing from the source) and non-incremental this most likely effects
the (meta)data freshness quality.
-------------
 
- a (front-end) service that sits in front of the metadata storage database. 
The service offers an API that allows metadata to be written into the system using push mechanisms, and programs that need to read metadata programmatically can read the metadata using this API.

Providing a (push-based) schematic interface immediately creates good contracts between producers of metadata and the “central metadata”

### Domain Model
After Ingestion of the (meta)data - we need to be able to access the collected data in a common way which will enable the
different domain attributes of the (collected) data models to be reflected as knowledge.

For example - we may collect RDBMS metadata of some inventory which contains details of the different tables and views in the
schema, and in-addition we also collect metadata regarding the business process (pipeline) that is applied on-top of these
tables - such as executed queries and job schedules.

**Example**

Let's examine the next inventory schema:
![](../img/inventory-system-er-diagram.jpg)

Here we can see that every table can be represented as a domain entity with relations to other entities.

**Example**

TODO add another example for pipline domain schema

------

This metadata contains different type of domains - we need a common logical way to represent both of these concerns.

In General - we need a general logical abstraction that can represent any type of domain and this logical representation must be 
capable of evolving without causing any physical changes.

### Knowledge Topology
Domain-oriented metadata models enables extensible, strongly-typed metadata models and relationships to be defined collaboratively by the enterprise.
Strong typing is important, because without that, we get the least common denominator of generic property-bags being stored in the metadata store
which makes it impossible for programmatic consumers of metadata to process metadata with any guarantee of backwards compatibility.

In the metadata model graph, we will use a terminology of Entity Types, Relationships and Aspects.
The basic knowledge which is used to describe a graph with default types of high level entities: Datasets, Roles, and Groups.
Different aspects such as Ownership, Physical-Schema, Profile & so on can be attached to these entities for different concerns which results in relationships being created between these entity types. 

This topology is represented in practice using a property graph that is both generic to allow any general purpose domain semantic and can also evolve freely without (almost) any constraints.

The basic element of this topology are entities and relations which are labeled with some kind of type identification.
These elements also have properties that represent the fields of these domain elements.

This type of (generic) topology will also allow asking complex questions which will allow the asking side to declare a "schema on read" type
of questions.

This "schema on read" allows creating a query that will model the question & response in a specific way that matches the domain of the query sender.  

**Example** 

TODO add example for 'schema on read'

## Entities & Aspects

The purpose of Aspects is to give the entity a different capability to express additional features without changing the internal fields of that entity.

To allow a generic knowledge structure which can contain multiple business domains in the same place, we predefine 3 types of default high-level entities that are the heart
of the generic knowledge domain - roles, group, dataset.

These entity types are always present no matter which business domain they represent and they are  

On top of these entities we will create additional aspects that refer to different point of views for these elements:
 - Dataset entity will have a 'contains' relationship to a list of tables entities that represent the business domain of that dataset
 - Group will have a 'members' relationship to a list of users
 - Roles will have a different type of relationships to the dataset entities representing different access capabilities. 

Lets review some sample of aspects for a 'table' entity:
 - Physical aspect - here the columns and constrains is specified
 - Statistics aspect - here the selectivity and cardinality of the columns is specified
 - Access aspect - here the field level access metadata is specified

We can observe that each aspect may be of interest to different point of views and therefor it will be queried using a specific query to 
fetch the needed aspects:
    _Match (d:Dataset {name:inventory})-[:contains]-(t:Table {name:products})-[:aspect]-(a:Physical,Statistics)_

This query fetch the _inventory.products_ table including two connected aspects - physical schema details and the statistical details.

###  Validation, Execution & Performance
We will closely examine the query execution use case where the catalog will be used for the following tasks:
 - validate the query against the schema structure
 - plan an efficient execution using the schema tables statistics

Let take the 'products' table as an example; we have the next columns:
 - Id : String [PK]
 - Name : String
 - Description : String
 - Unit : String
 - Price : Float
 - Quantity : Int
 - Status : Int
 - Supplier_ID : int [FK]
 - Category_ID : int [FK]

**Validation:**
Given any query that contains this specific table we need to verify:
 - correct fields names
 - correct fields types (filters, aggregations)
 - correct join-by field names 

**Planning**

When an execution plan is created, one of its basic steps will be to give that plan a score which will allow the planner
to choose the most performant plan.

In a Heuristic planner, each step of the plan is estimated according to some statistics that are being kept as metadata.
These cost measurements include:
 - column cardinality
 - filter selectivity
 - join approximation

All these statistics are part of the entity's (table) Statistics Aspect.

### NoSQL Storage 

Some storage repository are not able to handle indexing as an internal matter like in RDBMS, in such (NoSQL)
cases we will need to manually create indices for improving the query performance and data retrieval efficiency.

The knowledge of these indices (or for that matter materialized views) will also be kept in the catalog and the execution planner
will also be able to take advantage of these indices to create a cost-effective plan.

_For example_ - is we are using open-search for storing the inventory tables information - there will be cases where we would like to join information
from two or more tables - for such cases we may create in advanced a materialized views for such joins.
The catalog must be aware of the existence of these views and supply this information to be used by external API - such as the query planner.

This information will be saved in the Physical Aspect:
 - related to the entity if it is an index of that table
 - related to the query (sub query) if it is a view (materialization) of more than one entity

## API
The catalog will support 2 type of APIs:
 - General purpose base graph query API
 - GraphQL based query API with a pre-build IDL schema