FROM java:8
COPY ./target/assembly-fuse-knowledge /opt/fuse-engine
WORKDIR /opt/fuse-engine
CMD ["java", "-cp", ".:lib/*", "com.kayhut.fuse.services.FuseRunner", "config/application.conf activeProfile config/logback.xml"]
EXPOSE 8888



