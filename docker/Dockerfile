# Dockerfile for YANG-DB graph database
# MAINTAINER Lior Perry<www.youngdb.org>
# Java
# Eclipse Temurin JDK 11 on Ubuntu: a maintained base with current JDK 11 updates, for linux/amd64 and linux/arm64
# (it replaced alpine:3.14, end of life, with OpenJDK 11.0.14 from 2022). A JDK rather than a JRE, like before, so
# jcmd and Java Flight Recorder are there for diagnostics (docker/bench/README.md).
FROM eclipse-temurin:11-jdk

# curl for the health check; bash runs the start script
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl tzdata bash \
  && rm -rf /var/lib/apt/lists/*

# Set the working directory to /opt/engine
WORKDIR /opt/engine

# this is a specific yang.db assembly used for dragons based ontology - a general purpose RDF schema
COPY ./dragon /opt/engine

RUN chmod 755 /opt/engine/start-fuse-service.sh

# clean
RUN rm -rf /var/tmp/*

# define health check api
HEALTHCHECK --interval=5m --timeout=3s \
  CMD curl -f http://0.0.0.0:8888/fuse/health || exit 1

# Run fuse
CMD ["/opt/engine/start-fuse-service.sh"]

EXPOSE 8888
EXPOSE 9200
EXPOSE 9300
