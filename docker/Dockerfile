# Dockerfile for YANG-DB graph database
# MAINTAINER Lior Perry<www.youngdb.org>
# Java
FROM alpine:3.14

# install jdk 11
RUN  apk update \
  && apk upgrade \
  && apk add --update openjdk11 tzdata curl unzip bash \
  && rm -rf /var/cache/apk/*

# Set the working directory to /opt/engine
WORKDIR /opt/engine

# Cerebro
#FROM lmenezes/cerebro:0.8.3
RUN wget https://github.com/lmenezes/cerebro/releases/download/v0.8.4/cerebro-0.8.4.tgz -O /cerebro-0.8.4.tgz
RUN tar -xvf /cerebro-0.8.4.tgz -C /
RUN rm /cerebro-0.8.4.tgz

RUN apk add --no-cache bash

# this is a specific yang.db assembly used for dragons based ontology - a general purpose RDF schema
COPY ./dragon /opt/engine

RUN chmod 755 /opt/engine/start-fuse-service.sh

# clean
RUN rm -rf /var/tmp/*

# define health check api
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://0.0.0.0:8888/fuse/health || exit 1

# Run cerebro
CMD ["/cerebro-0.8.4/bin/cerebro"]

# Run fuse
CMD ["/opt/engine/start-fuse-service.sh"]

EXPOSE 8888
EXPOSE 9000
EXPOSE 9200
EXPOSE 9300
