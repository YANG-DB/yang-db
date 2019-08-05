#!/bin/bash

tagname=$1

echo "docker build -f Dockerfile -t tom4tomato/yang.db:${tagname}"
cd $(dirname "$0")

docker build -f Dockerfile -t tom4tomato/yang.db:${tagname}

echo "docker push tom4tomato/yang.db:${tagname}"
docker push tom4tomato/yang.db:${tagname}