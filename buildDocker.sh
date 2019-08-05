#!/bin/bash

tagname=$1

echo "docker build . -t tom4tomato/yang.db:${tagname}"
cd $(dirname "$0")

docker build . -t tom4tomato/yang.db:${tagname}

echo "docker push tom4tomato/yang.db:${tagname}"
docker push tom4tomato/yang.db:${tagname}