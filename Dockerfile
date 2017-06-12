FROM java:8
ADD fuse-assembly/target/fuse-assembly-1.0-SNAPSHOT-fuse-service-package/fuse-service-test-private /opt/fuse-engine
WORKDIR /opt/fuse-engine
CMD ["java", "-cp", ".:lib/*", "com.kayhut.fuse.services.FuseRunner", "config/application.conf"]
