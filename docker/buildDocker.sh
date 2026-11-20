#!/bin/bash
# Stop on the first failure: otherwise a failed Maven build still produces an image from a stale distrib/ folder.
set -euo pipefail

# Set the tag name or take the current date as the default tag
if [ -z "${1:-}" ]; then
  tagname=$(date +'%Y%m%d')
else
  tagname=$1
fi

# build the project for the dragons assembly distrib folder
cd ..
rm -rf distrib/dragon distrib/dragon.tar
echo "mvn clean install -DskipTests=true -P core,dragons-assembly"
mvn clean install -DskipTests=true -P core,dragons-assembly

# Copy Dockerfile to the distrib folder
cd docker
cp Dockerfile ../distrib/

# Build the Docker image in the distrib folder
cd ..
cd distrib
echo "docker build -t yangdb/yang.db:${tagname} ."
docker build -t yangdb/yang.db:${tagname} .