#!/bin/bash


tagname=$1

#run docker locally with port exposed - option 1
echo "docker run --network host -it  yangdb/yang.db:${tagname}"
docker run --network host -it  yangdb/yang.db:${tagname}
