#!/bin/bash
sudo docker login nexuss.westeurope.cloudapp.azure.com:5000 --username admin --password admin123
sudo docker pull nexuss.westeurope.cloudapp.azure.com:5000/fuse-engine:latest

COUNT=`sudo docker ps | grep fuse-engine | wc -l`
if [ $COUNT = 1 ]; then
	sudo docker rm -f fuse-engine
fi

sudo docker run -d -p 8888:8888 --name fuse-engine --restart always nexuss.westeurope.cloudapp.azure.com:5000/fuse-engine:latest

COUNT=`sudo docker images | grep none | awk '{ print $3 }'`
if [ ! -z "$COUNT" ]; then
  if [ "$COUNT" -gt 0 ]; then
    sudo docker rmi -f $(sudo docker images | grep none | awk '{ print $3 }')
  fi
fi
