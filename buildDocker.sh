#!/bin/bash


tagname=$1

echo "docker login -u yangdb"
docker login -u yangdb

cd $(dirname "$0")
echo "mvn clean install"
#mvn clean install

echo "docker build . -t yangdb/yang.db:${tagname}"
docker build . -t yangdb/yang.db:${tagname}

echo "docker push yangdb/yang.db:${tagname}"
docker push yangdb/yang.db:${tagname}