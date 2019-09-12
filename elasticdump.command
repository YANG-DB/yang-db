
elasticdump \
  --input=e0.json \
  --output=http://localhost:9200/e0 \
  --type=data


elasticdump \
  --input=http://localhost:9200/e0 \
  --output=out.json \
  --type=data

curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/e0/pge/_bulk?pretty' --data-binary @accounts.json