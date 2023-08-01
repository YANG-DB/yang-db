#!/bin/bash

# Set the tag name or take the current date as the default tag
if [ -z "$1" ]; then
  tagname=$(date +'%Y%m%d')
else
  tagname=$1
fi

# Login to docker using the yandb Account
echo "docker login -u yangdb"
docker login -u yangdb

# build the project for the dragons assembly distrib folder
cd ..
echo "mvn clean install -DskipTests=true install -P core,dragons-assembly"
#mvn clean install -DskipTests=true install -P core,dragons-assembly

# Copy Dockerfile to the distrib folder
cd docker
cp Dockerfile ../distrib/

# Build the Docker image in the distrib folder
cd ..
cd distrib
echo "docker build -t yangdb/yang.db:${tagname} ."
docker build -t yangdb/yang.db:${tagname} .

# push the docker image to the docker hub
echo "docker push yangdb/yang.db:${tagname}"
docker push yangdb/yang.db:${tagname}

#run docker locally with port exposed - option 1
#docker run -p 8888:8888 -it  yangdb/yang.db:${tagname}

#run docker locally with host ports mapping - option 2
#docker run --network host -it  yangdb/yang.db:${tagname}