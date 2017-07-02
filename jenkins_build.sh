#!/bin/bash
mvn clean package -DskipTests
docker login nexuss.westeurope.cloudapp.azure.com:5000 --username admin --password admin123
docker build -t nexuss.westeurope.cloudapp.azure.com:5000/fuse-engine:latest .
docker push nexuss.westeurope.cloudapp.azure.com:5000/fuse-engine:latest
