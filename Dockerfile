FROM java:8
ADD fuse-service/target/fuse-service-1.0-SNAPSHOT-fuse-service-package/fuse-service-1.0-SNAPSHOT .
CMD ["java", "-cp", "lib/*", "main.kayhut.fuse.services.FuseRunner"]
