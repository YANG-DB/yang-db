# Schema Implementation

## Overview:

In YangDB, property graphs follows a graph topology file (Ontology) - it is a set of rules where every edge or vertex
from the graph can be assigned at least one edge or vertex type. Vertices or edges of the same type share the same
property keys, having possibly different values for those keys. The file format supports mixed graphs (that are graphs
that can contain both undirected and directed edges) and parallel edges. Through the use of the ontology, it is possible
to map a GQL into the relational domain.

## Example:

An example of such ontology file is presented in figure below.

GQL - Graph Query Language

## Ontology Features

The ontology enables:

* <b>Multiple Models:</b> it describes how data stored in the underlying engine would be visualised as a property graph, classifying
  information as node types and edge types as long as it fits certain interpretation rules. In other words, it maps
  relational tuple sets into nodes and edge types.

The separation of a topology logical & physical files into two layers enables flexibility in mapping. The <strong>abstraction
layer</strong> (ontology file) describes the property graph model while the implementation one defines the mapping mechanisms between
domains.

This layer describes how the information described in the abstraction level is stored in the underlying store
system.

Nodes of the same type can be found on multiple tables and edges can actually represent multiple joins
between the source and destination nodes.

Nodes and edges of the same type can coexist in multiple tables:

Nodes of type Person could have been stored inside the tables <i><b>proletariat</b></i> and <i><b>bourgeoisie</b></i> in the relational system.

As long as both data tables were assigned the type Person on the implementation level, the GQL will be agnostic of this structure and refer to
the data of both of them by the type Person. The types model also allows Electricity Suppliers can to be also of type
Company.

Similarly, restrictions can be applied based on table data rules. It provides support to split a single table into
several different types of nodes/edges. It would make sense for a store system to have the information of Companies
and Electricity Suppliers in the same table.

An extra aspect that implementation level sums is the possibility of representing an edge as a multiple sequential join of tables.

## Getting started

Before start writing your own Ontology file, we should first model your E/S data structure as a property graph.

### Defining a Property Graph model to your relational schema

To start this task, you need to be aware of the schema.

As an example, assume the next indexes (tables) below. The content of these tables are available (
./docs/tutorial/sample/movies-imdb) .

<strong>Name Table</strong>

| id | name               | born |
|----|--------------------|------|
| 1  | Keanu Reeves       | 1964 |
| 2  | Carrie-Anne Moss   | 1967 |
| 3  | Laurence Fishburne | 1961 |
| 4  | Hugo Weaving       | 1960 |
| 5  | Andy Wachowski     | 1967 |
| 6  | Lana Wachowski     | 1965 |
| 7  | Joel Silver        | 1952 |
| 8  | Charlize Theron    | 1975 |
| 9  | Al Pacino          | 1940 |
| 10 | Taylor Hackford    | 1944 |

<strong>Title Table</strong>

|id|title|released|tagline|
|--- |--- |--- |--- |
|1|The Matrix|1999|Welcome to the Real World|
|2|The Devil's Advocate|1997|Evil has its winning ways|
|3|Monster|2003|The first female serial killer of America|

<strong>Principals (Acted In) table</strong>

|role|person_id|movie_id|
|--- |--- |--- |
|Neo|1|1|
|Trinity|2|1|
|Morpheus|3|1|
|Agent Smith|4|1|
|Kevin Lomax|1|2|
|Mary Ann Lomax|8|2|
|John Milton|9|2|
|Aileen|8|3|

<strong>Crew table</strong>

|person_id|movie_id|
|--- |--- |
|7|1|
|8|3|

<strong>Directed Table</strong>

|person_id|movie_id|
|--- |--- |
|5|1|
|6|1|
|10|2|

All these tables above can be modeled as the following property graph:

<p align="center">
  <img src="./docs/tutorial/sample/movies-imdb/movied-imdb-diagram.png" alt="actorMovieDiagram"/>
</p>

Someone a bit experienced in graph modeling would classify this model as a directed graph.


A single store system may be modeled in a veriaty of different property graph models. This is just
one of the many models that could have been done from the relational data above.

Now that you have a model, it's time to start writing the ontology file.

### Writing your own ontology file

Let's go for an example of the ontology - the former physical indexes (tables) can be translated into the next logical graph:

```json
{
  "ont": "Dragons",
  "entityTypes": [
    {
      "eType": "title",
      "dbName": "title",
      "name": "Movie",
      "metadata": [
        "id"
      ],
      "properties": [
        "title",
        "released",
        "tagline"
      ]
    },
    {
      "eType": "name",
      "dbName": "name",
      "name": "Person",
      "metadata": [
        "id"
      ],
      "properties": [
        "name",
        "born"
      ]
    }
  ],
  "relationshipTypes": [
    {
      "rType": "principal",
      "name": "Directed",
      "directional": true,
      "ePairs": [
        {
          "eTypeA": "name",
          "eTypeB": "title"
        }
      ],
      "metadata": [
        "id"
      ],
      "properties": [
        "startDate",
        "endDate"
      ]
    },
    {
      "rType": "principal",
      "name": "ActedIn",
      "directional": true,
      "ePairs": [
        {
          "eTypeA": "name",
          "eTypeB": "title"
        }
      ],
      "metadata": [
        "id"
      ],
      "properties": [
        "role",
        "startDate",
        "endDate"
      ]
    }
  ],
  "properties": [
    {
      "pType": "id",
      "name": "id",
      "type": "string"
    },
    {
      "pType": "firstName",
      "name": "firstName",
      "type": "text"
    },
    {
      "pType": "lastName",
      "name": "lastName",
      "type": "text"
    },
    {
      "pType": "gender",
      "name": "gender",
      "type": "string"
    },
    {
      "pType": "birthDate",
      "name": "birthDate",
      "type": "date"
    },
    {
      "pType": "deathDate",
      "name": "deathDate",
      "type": "string"
    },
    {
      "pType": "role",
      "name": "role",
      "type": "text"
    },
    {
      "pType": "date",
      "name": "date",
      "type": "date"
    },
    {
      "pType": "startDate",
      "name": "startDate",
      "type": "date"
    },
    {
      "pType": "endDate",
      "name": "endDate",
      "type": "date"
    }
  ]
}

```

There are two nodes types - called <strong>movie</strong> and <strong>person</strong>.

The <strong>types</strong> labels a given node type - and the same physical ( Table / Index) type can have multiple
labels.

The attributes (on the property graph sense of the word) of a node type are also listed.

On the edge side, one can see that "acted_in" is a directed edge, with one attribute called "role". It also connects
Person nodes towards Movie nodes.

The properties section of the ontology states the logical type, name and the physical field type - it is with direct
correlation to the most common capabilities on the underlying storage engine and must be supported by that engine as
well.

### Implementation level - Schema Provider

The implementation level describes how the property graph is mapped into your storage system.

Technically speaking, on the Java coding side, the implementation level is actually an interface - that can be
implemented (in the Java meaning of it) by other classes.

The property-graph, serialized in the <strong>ontology level</strong> above, associated with the INDEX schema, has the
following <strong>implementation level</strong>:

```json
{
  "implementationLevel": {
    "graphMetadata": {
      "backendSystem": "INDEX"
    },
    "implementationNodes": [
      {
        "types": [
          "movie"
        ],
        "tableName": "title",
        "id": [
          {
            "columnName": "id",
            "datatype": "INTEGER",
            "concatenationPosition": 1
          }
        ],
        "attributes": [
          {
            "columnName": "id",
            "dataType": "INTEGER",
            "abstractionLevelName": "id"
          },
          {
            "columnName": "title",
            "dataType": "TEXT",
            "abstractionLevelName": "title"
          },
          {
            "columnName": "released",
            "dataType": "INTEGER",
            "abstractionLevelName": "released"
          },
          {
            "columnName": "tagline",
            "dataType": "TEXT",
            "abstractionLevelName": "tagline"
          }
        ],
        "restrictions": []
      },
      {
        "types": [
          "person"
        ],
        "tableName": "name",
        "id": [
          {
            "columnName": "id",
            "datatype": "INTEGER",
            "concatenationPosition": 1
          }
        ],
        "attributes": [
          {
            "columnName": "id",
            "dataType": "INTEGER",
            "abstractionLevelName": "id"
          },
          {
            "columnName": "name",
            "dataType": "TEXT",
            "abstractionLevelName": "name"
          },
          {
            "columnName": "born",
            "dataType": "INTEGER",
            "abstractionLevelName": "born"
          }
        ],
        "restrictions": []
      }
    ],
    "implementationEdges": [
      {
        "types": [
          "directed"
        ],
        "paths": [
          {
            "traversalHops": [
              {
                "sourceTableName": "person",
                "sourceTableColumn": "id",
                "joinTableSourceColumn": "person_id",
                "joinTableName": "principal",
                "joinTableDestinationColumn": "movie_id",
                "destinationTableColumn": "id",
                "destinationTableName": "movie",
                "attributes": []
              }
            ]
          }
        ]
      },
      {
        "types": [
          "actedIn"
        ],
        "paths": [
          {
            "traversalHops": [
              {
                "sourceTableName": "person",
                "sourceTableColumn": "id",
                "joinTableSourceColumn": "person_id",
                "joinTableName": "principal",
                "joinTableDestinationColumn": "movie_id",
                "destinationTableColumn": "id",
                "destinationTableName": "movie",
                "attributes": [
                  {
                    "columnName": "role",
                    "dataType": "TEXT",
                    "abstractionLevelName": "role"
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

#### Graph Metadata

The GraphMetaData contains information describing the data stored in the backend system. The field <strong>
backendSystem</strong> defines which type of system the abstraction level is going to be mapped to.

#### Implementation Node

The mapping of every node is done in the <strong>Implementation Node</strong> attribute. 

let's use the example below:

```json
     {
  "types": [
    "person"
  ],
  "tableName": "name",
  "id": [
    {
      "columnName": "id",
      "datatype": "INTEGER",
      "concatenationPosition": 1
    }
  ],
  "attributes": [
    {
      "columnName": "id",
      "dataType": "INTEGER",
      "abstractionLevelName": "id"
    },
    {
      "columnName": "name",
      "dataType": "TEXT",
      "abstractionLevelName": "name"
    },
    {
      "columnName": "born",
      "dataType": "INTEGER",
      "abstractionLevelName": "born"
    }
  ],
  "restrictions": []
}
 ```

ImplementationNode attribute defines how to extract nodes of type <strong>"Person"</strong> from a designated Store engine.

There may be many sources of nodes of type "Person" in the system. The GQL compiler should take care of
retrieving all the nodes from the relational system.

In this example, nodes of type "person" are stored in the index also called "person". We assumes that every node
will have an id. In this scenario, there is an id of type "INTEGER", from the index column
called "id".

In some special cases, UIDs may come from multiple columns, this is solved by the "concatenationPosition"
that concatenates multiple columns together in order to generate an ID. An example would be to concatenate the strings
from the column "Name" and "Born", in order to generate a UID. In this case, that section would look like this:

```json
{
  "id": [
    {
      "columnName": "name",
      "datatype": "TEXT",
      "concatenationPosition": 1
    },
    {
      "columnName": "born",
      "datatype": "INTEGER",
      "concatenationPosition": 2
    }
  ]
}
```

Nodes of type person have three attributes, named in the relational schema as "id", "name" and "born". In the
abstraction level, these properties were also called with those names. "Name" is a "TEXT" and the other two
attributes are Integers.

On the edge implementation side, there's the following example:

```json
{
  "types": [
    "acted_in"
  ],
  "paths": [
    {
      "traversalHops": [
        {
          "sourceTableName": "person",
          "sourceTableColumn": "id",
          "joinTableSourceColumn": "person_id",
          "joinTableName": "principal",
          "joinTableDestinationColumn": "movie_id",
          "destinationTableColumn": "id",
          "destinationTableName": "movie",
          "attributes": [
            {
              "columnName": "role",
              "dataType": "TEXT",
              "abstractionLevelName": "role"
            }
          ]
        }
      ]
    }
  ]
}
```

Although the edge "acted_in" has only one path, the mapping allows for a multitude of paths during the traversal.

This means that the same edge leading one node type to another can be represented by different sequences of joins indexes
- e.g. the rows for the same edge can come from two different tables.

In parallel to that, each path can actually by a long sequence of joins, not only a single JOIN-index. This is the idea
behind the <strong>traversalHops</strong> attribute.

Graph traversals, on the relation domain, are represented by Joins, edges are classified on two
categories:

* <strong>Edges without join index:</strong> Usually happens between nodes of the same type, because this means that
  the edge information is contained in the same index document as the node information. An example would be an index
  represents a company personal.
  Source could be the current docment & the destination would be id of the manager, that
  would be another document in the same index.

* <strong>Edge with one or more join tables:</strong> In order to generalize the property graph edge modeling, an edge
  may actually represent more than two join operations - e.g. the edge is actually many indexes that should be joined
  together in the process. This is the reason why there is a <strong>TraversalHop</strong>.
  
The compiler should traverse this traversal hops sequentially in order to represent a traversal of a given edge.

In both scenarios, the <strong>SourceTableColumn</strong> and <strong>DestinationTableColumn</strong> are the columns
that will be used in the Join operation in order to represent a graph traversal.

Finally, some edges may contain attributes. In this example, the edge "acted_in" has the
attribute role, that is a "TEXT", stored in the column "role" of the index called "acted_in".
