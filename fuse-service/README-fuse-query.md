# Fuse

Fuse API:
| METHOD | URI | * | * | ACCESS
| ------ | ------ | ------ | ------ | ------ |
|  GET   | /                                                          |[*/*]|     [*/*]    |(/anonymous)
|  *     | /**                                                        |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse                                                      |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/health                                               |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/catalog/ontology/:id                                 |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/query                                                |[*/*]|     [*/*]    |(/anonymous)
|  POST  | /fuse/query                                                |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/query/:queryId                                       |[*/*]|     [*/*]    |(/anonymous)
| DELETE |/fuse/query/:queryId                                        |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/query/:queryId/plan                                  |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/query/:queryId/cursor                                |[*/*]|     [*/*]    |(/anonymous)
|  POST  | /fuse/query/:queryId/cursor                                |[*/*]|     [*/*]    |(/anonymous)
|  GET   | /fuse/query/:queryId/cursor/:cursorId                      |[*/*]|     [*/*]    |(/anonymous)
| DELETE | /fuse/query/:queryId/cursor/:cursorId                      |[*/*]|     [*/*]    |(/anonymous)
|  GET   |  /fuse/query/:queryId/cursor/:cursorId/page                |[*/*]|     [*/*]    |(/anonymous)
|  POST  |  /fuse/query/:queryId/cursor/:cursorId/page                |[*/*]|     [*/*]    |(/anonymous)
|  GET   |  /fuse/query/:queryId/cursor/:cursorId/page/:pageId        |[*/*]|     [*/*]    |(/anonymous)
|  GET   |  /fuse/query/:queryId/cursor/:cursorId/page/:pageId/data   |[*/*]|     [*/*]    |(/anonymous)
|  POST  |  /fuse/search                                              |[*/*]|     [*/*]    |(/anonymous)

# Query flow

##### POST: http/fuseHost:8080/fuse/query
  - Post V1 query to fuse engine  
```json
 {
   "ont": "Dragons",
   "name": "Q1",
       "elements": [
               {
                 "eNum": 0,
                 "type": "Start",
                 "next": 1
            },
    {
      "eNum": 1,
      "type": "EConcrete",
      "eTag": "A",
      "eID": "12345678",
      "eType": 1,
      "eName": "Brandon Stark",
      "next": 2
    },
    {
      "eNum": 2,
      "type": "Rel",
      "rType": 101,
      "dir": "R",
      "next": 3
    },
    {
      "eNum": 3,
      "type": "ETyped",
      "eTag": "B",
      "eType": 2
    }
  ]
}
```
  - Response: 201 (resource Created)
```json
{
    "id":"1",
    "data":
    {
        "resourceUrl":"http://192.168.1.40:8888/fuse/query/1",
        "resourceId":"1",
        "cursorStoreUrl":"http://192.168.1.40:8888/fuse/query/1/cursor"
    }
}
```
 - A new query resource was created 
##### GET: http/fuseHost:8080/fuse/query/1
 - Fetch query resource
```json
{
  "id": "09444df8-c8bb-4fd3-ac40-b3145cb0c4cb",
  "data": {
    "resourceUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0",
    "resourceId": "0",
    "cursorType": "graph",
    "pageStoreUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0/page"
  }
}
```
##### GET: http/fuseHost:8080/fuse/query/1/cursor/0/page
 - Fetch cursor resource (a child resource of query)
```json
{
  "id": "29f79532-bdf4-4378-b399-f8492e9d6138",
  "data": {
    "resourceUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0",
    "resourceId": "0",
    "cursorType": "graph",
    "pageStoreUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0/page"
  }
}
```
 - The Cursor resource returns with a first default page containing query result data
##### POST: http/fuseHost:8080/fuse/query/1/cursor/0/page
- Fetch page resource (a child resource of cursor)
```json
{
    "pageSize":100
}
```
 
 - Response 201  -  page fetched & data resource created
```json
{
  "id": "2fb92933-28e8-480c-8ced-3bd1ab368bc9",
  "data": {
    "resourceUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0/page/0",
    "resourceId": "0",
    "dataUrl": "http://192.168.1.40:8888/fuse/query/1/cursor/0/page/0/data",
    "requestedPageSize": 100,
    "actualPageSize": 0,
    "available": false
  }
}
```
##### GET: http/fuseHost:8080/fuse/query/1/cursor/0/page/data
- Fetch data 
```json
{
  "id": "3fdf4001-d755-4194-b02e-8ac4a96fe6e5",
  "data": {
    "pattern": {
      "ont": "Dragons",
      "name": "Q1",
      "elements": []
    },
    "assignments": [
      {
        "entities": [
          {
            "eTag": [
              "A",
              "C"
            ],
            "eID": "12345678",
            "eType": 1,
            "properties": [
              {
                "pType": 1,
                "agg": "raw",
                "value": "a"
              },
              {
                "pType": 3,
                "agg": "raw",
                "value": 5.35
              }
            ],
            "attachedProperties": [
              {
                "pName": "count(relationships)",
                "value": 53
              }
            ]
          }
        ],
        "relationships": [
          {
            "rID": "12345678",
            "agg": true,
            "rType": 2,
            "directional": true,
            "eID1": "12345678",
            "eID2": "12345679",
            "properties": [
              {
                "pType": 1,
                "agg": "max",
                "value": 76
              },
              {
                "pType": 1,
                "agg": "avg",
                "value": 34.56
              }
            ],
            "attachedProperties": [
              {
                "pName": "sum(duration)",
                "value": 124
              }
            ]
          }
        ]
      }
    ]
  }
}
```
