##Query to Index Projection
###**Abstract** 

The purpose of this feature is to provide a consistent and general way to project a (general) query results into a dedicated projection index
This index will be used to store the returning query-results assignments inside a single index, it will also be able to efficiently perform slice & dice over the resulting assignments using
the efficient E/S aggregation DSL.


---

The generic projection Index.
This index is created in advanced and will populate the ontology specific schema structure with the following characteristics:
 * Document will contain the query name and time of execution
 * Document will contain all the nodes (vertices array) in the upper-most level of the document
 * Document will contain all the relations (edges array ) in second level of the document - as a nested document under each relevant vertex

Each vertex/edge will contain the following fields:
 * id
 * type (label)
 * properties (its own schematic fields)
 * tag name (according to query)
 
In addition each internal nested document (edge) will contain the additional fields:
 * sideB.type
 * sideB.id

This structure will simplify the creation of aggregations that will help further investigating the graph query results
with an efficient execution offloading to the E/S engine.

It will also allow the storage of the results for future consumption and for the capability to consume the data in a stream like manner
using the page based query results consumption capability


---

### Example

    /**
     *     Match (p:Person {gender:FEMALE})-[o:Own]-(d:Dragon {color :BLACK}),
     *             (p:Person)-[oh:Own]-(h:Horse),
     *             (d:Dragon)-[f:Fire]-(other:Dragon { gender:MALE}),
     *             (h:Horse)-[org:OriginatedIn]->(k:Kingdom )
     *
     *     will be translated to the following document
     *
     *     {
     *         person: [{
     *              "type":Person,
     *              "id": p1
     *              "tag: p
     *              "properties":{}
     *               own:[{
     *                  "id": 1
     *                  "tag":o
     *                  "type":Own
     *                   sideB.type = "Dragon"
     *                   sideB.id = d1
     *                   "properties":{}
     *                },
     *                {
     *                   "id": 2
     *                    "tag":oh
     *                    "type":Own
     *                     sideB.type = "Horse"
     *                     sideB.id = h1
     *                    "properties":{}
     *                }],
     *         }],
     *         dragon: [
     *           {
     *              "type":Dragon,
     *              "id": d1,
     *              "tag":d,
     *               fire :{
     *                  "id": 1
     *                   "tag":f
     *                   sideB.type = "Dragon"
     *                   sideB.id = d1
     *                  "type":Fire,
     *                  "properties":{}
     *                  },
     *             },
     *             {
     *              "type":Dragon,
     *              "tag":other,
     *              "id": d1,
     *              "properties":{}
     *         ],
     *         horse:{
     *               originated: [{
     *               
     *                 }]
     *         }],
     *        kingdom: [{
     *           }]
     *
     *     }
     *
     * @return
     */
