FROM java:8
ADD fuse-service/target/fuse-service-1.0-SNAPSHOT-fuse-service-package/fuse-service-1.0-SNAPSHOT /opt/fuse-engine
WORKDIR /opt/fuse-engine
CMD ["java", "-cp", "lib/*", "com.kayhut.fuse.services.FuseRunner"]
