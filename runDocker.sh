#!/bin/bash


tagname=$1

echo "docker run --network host  -it  yangdb/yang.db:${tagname}"

#run docker locally with port exposed - option 1
docker run --network host  -it  yangdb/yang.db:${tagname}
