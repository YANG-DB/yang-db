# Projection & Count Cursors

In this tutorial we will review different type of cursors that enable us to materialize queries and count results

### Count
The purpose of this cursor is to supply a way to exactly count the amount of labels a given query is returning without actually fetching the entire documents into memory:

We should use this cursor in the case that we need to understand the amount of results (assignments) returning from a query.
Once such a query with the "count" cursor is submitted - the engine accumulates the resulting labels in each row - once no more results are available it returns the exact number of
labels that appeared in the response:

we use the following query (who knows whom), with cursor type `count`:
```
Match (p1:Person)-[k:Know]->(p2:Person) return *
```

![swagger-countCursor-results.png](img/swagger-countCursor-results.png)
(The screenshot shows the same call for an older query and data set.)

With the tutorial data loaded, the result assignments contain the following labels - one `Know` per row of
`KnowsPerson.csv`, and two `Person` labels (`p1`, `p2`) per row:

```
"assignments": [
                {
                  "id": 1449928957329970608,
                  "Know": 475,
                  "Person": 950
                }
               ]
```

The downside of this API is that it counts exactly the amount of the resulting assignments - it may take a while to receive an answer.

We would like to be able to estimate the amount of results according to some sampling technique that will be much faster and will give reasonable estimation errors
This is a new feature that is under development and will be available soon...


-----
### Projection (Materialization)

The purpose of this cursor is to provide a consistent and general way to project a (general) query results into a dedicated projection index
This index will be used to store the returning query-results assignments inside a single index, it will also be able to efficiently perform slice & dice over the resulting assignments using
the efficient OpenSearch aggregation DSL.

For additional details refer to [index-projection-details](../../../branch/indexProjection.md)


### Usage
Since we concluded that want to project the query results - lets actually run the query with the projection cursor and see what happens:

#### Swagger
The regular API can be called with the following query:


```
Match (p1:Person)-[k:Know]->(p2:Person {gender: FEMALE}) return *
```

The only difference would be the type of cursor that is selected - projection. With the tutorial data this writes 245
documents, one per result row, into the `projection` index (`curl 'localhost:9200/projection/_count'`; the
`docs.count` of `_cat/indices` is higher because it also counts each row's nested entity and relationship documents).
The index is kept until you delete it (`curl -XDELETE localhost:9200/projection`).

_Note:_ the projection cursor doesn't support count filters (`WHERE count(k) > n`) yet. Such a query projects
nothing.
The result will reflect the amount of elements ingested into the projection index - this API can is syncronized so the waiting time for the query to complete
depends on the amount of results the query returns.
![swagger-project-query](img/swagger-project-query.png)


If you want to get notified when the projection completes - this API will soon be available under notification...

### Consumption API
Once the query has started materialization of the results into the projection index - we can see the amount of results (assignments) mounting up
as shown here of the projection index aggregation of different documents per projected-query.

![projection-index-aggregation](img/projection-index-aggregation.png)


As mentioned before - each document in the projection index represents a assignment result (a single row).
It is possible to start consuming the results using a dedicated consume API.
The consumption API is similar to the standard _getData_ API in that it returns paginated data from the materialized query result index.
The difference is that the consume API is dedicated for stream like consumption from a source - in our case this source is the projection index.

