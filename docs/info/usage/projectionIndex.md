### Projection Index Patterns

A query run with the `projection` cursor writes its results into the `projection` index (one document per
assignment, one nested field per entity type, tagged with the query's `queryId`). See
[Projection & Count Cursors](../../tutorial/sample/dragons/projection-and-count.md) for how to run one, and
[the design note](../../branch/indexProjection.md) for the document structure.

#### Query
```
Match (p1:Person)-[o:Own]->(d2:Dragon )  return *
```

OpenSearch query DSL (run on the `projection` index) that fetch the amount of (p1) persons in projected results;
`queryId` is the id YANG-DB returned for the query (a UUID - `"1"` below is a placeholder)
```
{
  "query": {
    "bool": {
      "filter": [
        {
          "nested": {
            "path": "Person",
            "query": {
              "bool": {
                "must": [
                  {
                    "match": {
                      "Person.tag": "p1"
                    }
                  }
                ]
              }
            }
          }
        },
        {
          "term": {
            "queryId": "1"
          }
        }
      ]
    }
  },
  "aggs": {
    "uniquePeople": {
      "nested": {
        "path": "Person"
      },
      "aggs": {
        "terms": {
          "terms": {
           "size": 1000,
            "field": "Person.id"
          }
        }
      }
    }
  }
}
```