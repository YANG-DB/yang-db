# Dockerfile for YANG-DB graph database
# MAINTAINER Lior Perry<www.youngdb.org>
# Java
FROM openjdk:8-alpine
# Cerebro
#FROM lmenezes/cerebro:0.8.3
RUN wget https://github.com/lmenezes/cerebro/releases/download/v0.8.4/cerebro-0.8.4.tgz -O /cerebro-0.8.4.tgz
RUN tar -xvf /cerebro-0.8.4.tgz -C /
RUN rm /cerebro-0.8.4.tgz

RUN apk add --no-cache bash
RUN mkdir -p /opt/engine
WORKDIR /opt/engine

COPY fuse-domain/fuse-domain-knowledge/fuse-domain-knowledge-assembly/target/assembly-fuse-knowledge /opt/engine

RUN chmod 755 /opt/engine/start-fuse-service.sh
# clean
RUN rm -rf /var/tmp/*

# Run cerebro
CMD ["/cerebro-0.8.4/bin/cerebro"]
# Run fuse
CMD ["/opt/engine/start-fuse-service.sh"]

EXPOSE 8888
EXPOSE 9000
EXPOSE 9200
EXPOSE 9300



