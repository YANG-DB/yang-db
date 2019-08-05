#!/bin/bash


tagname=$1

cd $(dirname "$0")
echo "mvn clean install"
mvn clean install

echo "docker build . -t tom4tomato/yang.db:${tagname}"
docker build . -t tom4tomato/yang.db:${tagname}

echo "docker push tom4tomato/yang.db:${tagname}"
docker push tom4tomato/yang.db:${tagname}