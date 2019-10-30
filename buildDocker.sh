#!/bin/bash


tagname=$1

echo "docker login -u yangdb"
docker login -u yangdb

cd $(dirname "$0")
echo "mvn clean install"
mvn clean install

echo "docker build . -t yangdb/yang.db:${tagname}"
docker build . -t yangdb/yang.db:${tagname}

echo "docker push yangdb/yang.db:${tagname}"
docker push yangdb/yang.db:${tagname}

#run docker locally with port exposed - option 1
#docker run -p 8888:8888 -it  yangdb/yang.db:${tagname}

#run docker locally with host ports mapping - option 2
#docker run --network host -it  yangdb/yang.db:${tagname}