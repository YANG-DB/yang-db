> **Start here:** the [architecture tutorial](../../../docs/tutorial/architecture/README.md) explains every module and follows one query through all its translations, using the current code. This page is older background material.

## YangDB Software Architecture

The graph db solution is a multi-modules software project build upon different architecture layers.

The fundamental idea behind this structure is the abstraction and compartmentalization of different software and functional
concerns.

The project is boundeled as a multi-modules maven project which includes core modules and specific modules (fuse-dv) dedicated 
to enable functionality over the OpenSearch storage layer.

-------------
 
The software building block used in this project are dominating open source libraries:

#### Software General purpose libraries
 - Apache guava  - https://github.com/google/guava
 - Apache guice  - https://github.com/google/guice
 - Apache commons - https://commons.apache.org/ 
 - javaslang -      https://github.com/vavr-io/vavr
 
#### Graph libraries
 - tinkerpop - https://github.com/apache/tinkerpop
 - rdf4j -  https://github.com/eclipse/rdf4j
 - owlapi - https://github.com/owlcs/owlapi
 - openCypher - https://github.com/opencypher/openCypher 

#### Indexing store libraries
 - Lucene   - https://github.com/apache/lucene
 - OpenSearch - https://github.com/opensearch-project/OpenSearch
 
#### MVC Web libraries
 - jooby  - https://github.com/jooby-project/jooby
 - netty  - https://github.com/netty/netty
 
 
Additional Libraries can be found in the project's pom.xml

-------------
#### Architecture Layers
The project structure is designed with the purpose of adding plugin as free as possible - hence the term open architecture.

For this to be possible a great effort was made to modularize the functional parts of the software to allow clear and direct extensibility by anyone 
with understanding of the structure and funtional flow of the execution.

##### Core modules
The core modules:
 - fuse-model :  representing the data model elements (classes) used to query, profile, analyze, process and project the data     
 - fuse-extension-api : the plugin contract (`YangDbExtension`, `QueryLanguages`), found with Java's `ServiceLoader`
 - fuse-asg :    containing the Abstract Syntax's Graph that is transformed from the query and is validated and rewritten according to rule based semantics.      
 - fuse-core  :  containing the core functionality common to all generic parts such as Query planning, Transformations, Schema provider, Drivers and more     
 - unipop-core  : tinkerpop drivers adaptation for execution of graph traversal over OpenSearch (forked from https://github.com/unipop-graph/unipop)     
 - fuse-opensearch-client : the REST (HTTP, port 9200) client the engine uses to talk to OpenSearch
 - fuse-service  :  containing the core services & controllers wrapping the Web (REST) endpoints and delivering deep traceability and logging      
 - fuse-geojson, fuse-test : geo-json support and the shared test framework

##### Domain Specific modules
These modules are the basic "plug-able" parts of the software allowing all the existing core functionality against OpenSearch - the default storage & indexing layer

-  **fuse-dv (Data Virtualization)**
    - fuse-dv-asg :   the specific ASG behavior that is OpenSearch storage aware 
    - fuse-dv-core :  the specific core behavior that is OpenSearch storage aware 
    - fuse-dv-epb :   execution plan builder based on OpenSearch strong indexing and (statistics) counting capabilities 
    - fuse-dv-gta :   graph traversal extender which translates the cost revised execution plan into a physical execution plan (based on tinkerpop graph traversal) 
    - fuse-dv-unipop :  tinkerpop drivers adaptation for graph traversal over OpenSearch with specific changes introduced for performance 
    - fuse-dv-stat :  statistical calculation module enabling the usage of dynamic execution planning and modeling based on pre-calculated cardinality statistics

- **extensions/graphql**: the GraphQL query language (language key `graphQL`) and GraphQL-schema-to-ontology
  translation, as a plugin
- **distribution**: assembles the runnable engine, with the configuration (ontology, index provider, application.conf)
  of the Dragons sample, into `distrib/dragon`. The Docker image is built from it (`-P dragons-assembly`).
- **examples** (sample domains, outside the default build)
    - examples/dragons/ext : the Dragons service extension (`DragonsModule`), loaded by the distribution's application.conf
    - examples/dragons/datagen : generator for the Dragons tutorial data (a fantasy world with dragons, kingdoms and more)
 
The usage of the dependency injection framework guice togather with jooby (a modern, performant and easy to use web framework) makes it easy to load the modules in the correct order to allow plug-able architecture.

##### Modules loading sequence
The next modules list is stated in the core configuration file of yangDb: **application.conf**

We can see here the code snippet taken from the Dragons distribution (`distribution/resources/assembly/Dragons/config`) with its unique specific modules: 

```
    modules.activeProfile = [
      "com.yangdb.fuse.services.modules.ServiceModule",
      "com.yangdb.fuse.services.modules.LoggingJacksonModule",
      "com.yangdb.fuse.dispatcher.modules.CoreDispatcherModule",
      "com.yangdb.fuse.dispatcher.query.rdf.SparqlModule",
      "com.yangdb.fuse.dispatcher.query.sql.SqlModule",
      "com.yangdb.fuse.dispatcher.modules.DescriptorsModule",
      "com.yangdb.fuse.asg.translator.cypher.AsgCypherModule",
      "com.yangdb.fuse.asg.translator.sparql.AsgSparqlModule",
      "com.yangdb.fuse.asg.M2AsgModule",
      "com.yangdb.fuse.epb.plan.modules.EpbDfsCountBasedRedundantModule",
      "com.yangdb.fuse.asg.AsgValidationModule",
      "com.yangdb.fuse.gta.module.GtaModule",
      "com.yangdb.fuse.executor.ExecutorModule",
      "com.yangdb.fuse.executor.modules.discrete.CursorsModule",
      "com.yangdb.dragons.DragonsModule"
    ]
```

Plugins (such as extensions/graphql) are not listed here: their Guice modules are found on the classpath with Java's
`ServiceLoader` and installed automatically.

  We can observe that many modules are functional specific that offer a distinct funtional behavior and can be added/removed to add/remove functionality as needed.
  
**Specific Funtional Modules**
  -   AsgCypherModule - enabling cypher ASG query translation & rewrite

  -   SparqlModule - enabling OWL schema to ontology translation (`/fuse/sparql/ontology/{id}`)
  -   AsgSparqlModule - enabling sparql ASG query translation & rewrite (language key `sparql`, alias `sparkQL`)

  -   SqlModule - enabling SQL DDL schema to ontology translation (`/fuse/sql/ontology/{id}`)
  -   DragonsModule - the Dragons sample's own service extension (examples/dragons/ext)
  -   GraphQLModule - enabling graphql query support; part of the extensions/graphql plugin, loaded through `ServiceLoader`