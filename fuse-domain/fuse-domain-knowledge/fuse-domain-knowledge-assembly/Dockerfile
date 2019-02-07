FROM java:8
COPY ./target/assembly-fuse-knowledge /opt/fuse-engine
WORKDIR /opt/fuse-engine
EXPOSE 8888
CMD ["java", "-cp", ".:lib/*", "com.kayhut.fuse.services.FuseRunner", "config/application.conf",  "activeProfile", "config/logback.xml"]
