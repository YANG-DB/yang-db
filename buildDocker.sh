#!/bin/bash

tagname=$1

echo "docker login "
docker login

cd $(dirname "$0")
echo "mvn clean install"
mvn clean install

echo "docker build . -t yangdb/yang.db:${tagname}"
docker build . -t yangdb/yang.db:${tagname}

echo "docker push yangdb/yang.db:${tagname}"
docker push yangdb/yang.db:${tagname}

#run docker locally with port exposed
docker run -p 8888:8888 -it  yangdb/yang.db:v1_Oct2019

#run docker locally with host ports mapping
docker run --network host -it  yangdb/yang.db:v1_Oct2019