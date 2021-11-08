### Projection Index Patterns

#### Query
```
Match (p1:Person)-[o:Own]->(d2:Dragon )  return *
```

Elasticsearch query DSL that fetch the amount of (p1) persons in projected results
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
