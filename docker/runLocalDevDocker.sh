#!/bin/bash

clusterName=$1

echo "Starting elasticsearch clusterName:$clusterName"

docker network create elastic
docker pull docker.elastic.co/elasticsearch/elasticsearch:6.5.4
docker run -d --name elasticsearch --net elastic -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.5.4
echo " elasticsearch started"

echo " Starting Kibana"
docker pull docker.elastic.co/kibana/kibana:7.12.1
docker run -d --name kibana --net elastic -p 5601:5601 -e "ELASTICSEARCH_HOSTS=http://elasticsearch:9200" docker.elastic.co/kibana/kibana:6.5.4
echo " Kibana started"

echo " Starting Yang-DB"
echo "docker run --network host  -it  yangdb/yang.db:${clusterName}"

#run docker locally with port exposed - option 1
docker run  -d --network host  -it  yangdb/yang.db:${clusterName}
echo " Yang-DB started"
