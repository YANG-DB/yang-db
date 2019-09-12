## Sample dump tools & API used to import and backup indices

## ElasticDump  - a nodeJS based app
## https://www.npmjs.com/package/elasticdump
## usage: npm i elasticdump

## elasticdump works by sending an input to an output. Both can be either an elasticsearch URL or a File.

## Elasticsearch:
##     format: {protocol}://{host}:{port}/{index}
##     example: http://127.0.0.1:9200/my_index
## File:
##     format: {FilePath}
##     example: /Users/evantahler/Desktop/dump.json
## Stdio:
##     format: stdin / stdout
##     format: $

elasticdump \
  --input=e0.json \
  --output=http://localhost:9200/e0 \
  --type=data


elasticdump \
  --input=http://localhost:9200/e0 \
  --output=out.json \
  --type=data



## Bulk API -
##  The bulk API makes it possible to perform many index/delete operations in a single API call.
##  This can greatly increase the indexing speed.

# The REST API endpoint is /_bulk, and it expects the following newline delimited JSON (NDJSON) structure:
#            action_and_meta_data\n
#            optional_source\n
#            action_and_meta_data\n
#            optional_source\n
#            ....
#            action_and_meta_data\n
#            optional_source\n

#   NOTE: The final line of data must end with a newline character \n.

# https://kb.objectrocket.com/elasticsearch/how-to-bulk-import-into-elasticsearch-using-curl
# https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html

curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/e0/pge/_bulk?pretty' --data-binary @accounts.json