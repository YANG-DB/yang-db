#!/bin/bash
docker run \
  --name kibana \
  --publish 5601:5601 \
  --network host \
  --env "ELASTICSEARCH_HOSTS=http://127.0.0.1:9200" \
  docker.elastic.co/kibana/kibana:6.5.4