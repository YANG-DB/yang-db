# Dockerfile for YANG-DB graph database
# MAINTAINER Lior Perry<www.youngdb.org>

# Java
FROM java:8
# Cerebro
FROM lmenezes/cerebro:0.8.3
# node
FROM node:8

#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY . /home/app
RUN mvn -f /home/app/pom.xml clean package -DskipTests

# https://www.npmjs.com/package/elasticsearch-tools
# node elasticsearch export/loader tool
#
# npm install -g elasticsearch-tools
# es-import-bulk --url http://localhost:9200 --file ~/dataFile.json
#
# Package stage
#
COPY /fuse-domain/fuse-domain-knowledge/fuse-domain-knowledge-assembly/target/assembly-fuse-knowledge /opt/engine
WORKDIR /opt/engine

# Remove the spurious windoews CR characters.
CMD ["sed -i -e 's/\r$//' start-fuse-service.sh"]

CMD ["./start-fuse-service.sh"]

EXPOSE 8888
EXPOSE 9000
EXPOSE 9200
EXPOSE 9300



