#!/bin/bash

mainClass=com.yangdb.fuse.services.FuseRunner
configFile=config/application.conf
activeProfile=activeProfile
logbackConfigurationFilename=config/logback.xml
heapSize=1g

elasticsearchEmbedded="${ELASTICSEARCH_EMBEDDED:-true}"
elasticsearchHosts="${ELASTICSEARCH_HOST:-localhost}"
elasticsearchClusterName="${ELASTICSEARCH_CLUSTER_NAME}"
elasticsearchTcpPort="${ELASTICSEARCH_TCP_PORT:-9300}"

#classPath=".:lib/*"

#explicit classpath as exported from the assembly
classPath3rd="./lib/slf4j-log4j12-1.7.30.jar:./lib/log4j-1.2.17.jar:./lib/slf4j-api-1.7.30.jar:./lib/commons-logging-1.2.jar:./lib/log4j-core-2.11.1.jar:./lib/log4j-api-2.11.1.jar:./lib/logback-elasticsearch-appender-1.6.jar:./lib/jackson-core-2.10.4.jar:./lib/scala-library-2.11.12.jar:./lib/jbool_expressions-1.13.jar:./lib/commons-lang-2.5.jar:./lib/guava-21.0.jar:./lib/antlr-3.5.2.jar:./lib/antlr-runtime-3.5.2.jar:./lib/ST4-4.0.8.jar:./lib/aviator-4.2.0.jar:./lib/commons-beanutils-1.9.3.jar:./lib/graphql-java-2020-01-17T02-41-10-578985f.jar:./lib/antlr4-runtime-4.7.2.jar:./lib/java-dataloader-2.2.3.jar:./lib/reactive-streams-1.0.2.jar:./lib/front-end-9.0-9.0.20181012.jar:./lib/util-9.0-9.0.20181012.jar:./lib/expressions-9.0-9.0.20181012.jar:./lib/rewriting-9.0-9.0.20181012.jar:./lib/ast-9.0-9.0.20181012.jar:./lib/parser-9.0-9.0.20181012.jar:./lib/scala-reflect-2.11.12.jar:./lib/scalatest_2.11-2.2.5.jar:./lib/scala-xml_2.11-1.0.2.jar:./lib/scalacheck_2.11-1.12.5.jar:./lib/test-interface-1.0.jar:./lib/parboiled-scala_2.11-1.1.7.jar:./lib/parboiled-core-1.1.7.jar:./lib/jooby-1.6.8.jar:./lib/config-1.3.3.jar:./lib/funzy-0.1.0.jar:./lib/guice-multibindings-4.1.0.jar:./lib/guice-4.1.0.jar:./lib/javax.inject-1.jar:./lib/aopalliance-1.0.jar:./lib/sql-open-distro-0.51-SNAPSHOT.jar:./lib/presto-matching-0.242.jar:./lib/spring-beans-5.2.10.RELEASE.jar:./lib/spring-core-5.2.10.RELEASE.jar:./lib/spring-jcl-5.2.10.RELEASE.jar:./lib/spring-context-5.2.10.RELEASE.jar:./lib/spring-aop-5.2.10.RELEASE.jar:./lib/spring-expression-5.2.10.RELEASE.jar:./lib/json-20200518.jar:./lib/gson-2.8.6.jar:./lib/resilience4j-retry-1.6.1.jar:./lib/vavr-0.10.2.jar:./lib/vavr-match-0.10.2.jar:./lib/resilience4j-core-1.6.1.jar:./lib/druid-1.0.15.jar:./lib/jconsole.jar:./lib/tools.jar:./lib/lucene-core-8.2.0.jar:./lib/spring-test-5.2.10.RELEASE.jar:./lib/gremlin-core-3.2.5.jar:./lib/gremlin-shaded-3.2.5.jar:./lib/snakeyaml-1.15.jar:./lib/javatuples-1.2.jar:./lib/hppc-0.7.1.jar:./lib/jcabi-manifests-1.1.jar:./lib/jcabi-log-0.14.jar:./lib/javapoet-1.8.0.jar:./lib/jcl-over-slf4j-1.7.21.jar:./lib/metrics-core-3.1.2.jar:./lib/jooq-3.13.4.jar:./lib/jaxb-api-2.3.1.jar:./lib/javax.activation-api-1.2.0.jar:./lib/ahocorasick-0.4.0.jar:./lib/jackson-dataformat-csv-2.10.4.jar:./lib/jackson-databind-2.10.4.jar:./lib/caffeine-2.6.2.jar:./lib/json-io-4.10.1.jar:./lib/transport-7.4.2.jar:./lib/transport-netty4-client-7.4.2.jar:./lib/netty-buffer-4.1.38.Final.jar:./lib/reindex-client-7.4.2.jar:./lib/elasticsearch-rest-client-7.4.2.jar:./lib/httpasyncclient-4.1.4.jar:./lib/httpcore-nio-4.4.11.jar:./lib/elasticsearch-ssl-config-7.4.2.jar:./lib/lang-mustache-client-7.4.2.jar:./lib/compiler-0.9.3.jar:./lib/percolator-client-7.4.2.jar:./lib/parent-join-client-7.4.2.jar:./lib/rank-eval-client-7.4.2.jar:./lib/reflections-0.9.11.jar:./lib/javassist-3.21.0-GA.jar:./lib/rdf4j-queryparser-sparql-3.3.1.jar:./lib/rdf4j-queryparser-api-3.3.1.jar:./lib/rdf4j-rio-trig-3.3.1.jar:./lib/rdf4j-rio-api-3.3.1.jar:./lib/jsonld-java-0.13.0.jar:./lib/httpclient-osgi-4.5.10.jar:./lib/httpclient-cache-4.5.10.jar:./lib/fluent-hc-4.5.10.jar:./lib/httpcore-osgi-4.4.12.jar:./lib/rdf4j-rio-turtle-3.3.1.jar:./lib/rdf4j-rio-datatypes-3.3.1.jar:./lib/rdf4j-rio-languages-3.3.1.jar:./lib/rdf4j-query-3.3.1.jar:./lib/rdf4j-queryalgebra-model-3.3.1.jar:./lib/rdf4j-model-3.3.1.jar:./lib/rdf4j-util-3.3.1.jar:./lib/rdf4j-queryparser-serql-3.3.1.jar:./lib/javax.annotation-api-1.3.2.jar:./lib/commons-lang3-3.5.jar:./lib/commons-collections4-4.1.jar:./lib/elasticsearch-7.4.2.jar:./lib/elasticsearch-core-7.4.2.jar:./lib/elasticsearch-secure-sm-7.4.2.jar:./lib/elasticsearch-x-content-7.4.2.jar:./lib/jackson-dataformat-smile-2.8.11.jar:./lib/jackson-dataformat-cbor-2.8.11.jar:./lib/elasticsearch-geo-7.4.2.jar:./lib/lucene-analyzers-common-8.2.0.jar:./lib/lucene-backward-codecs-8.2.0.jar:./lib/lucene-grouping-8.2.0.jar:./lib/lucene-highlighter-8.2.0.jar:./lib/lucene-join-8.2.0.jar:./lib/lucene-memory-8.2.0.jar:./lib/lucene-misc-8.2.0.jar:./lib/lucene-queries-8.2.0.jar:./lib/lucene-queryparser-8.2.0.jar:./lib/lucene-sandbox-8.2.0.jar:./lib/lucene-spatial-8.2.0.jar:./lib/lucene-spatial-extras-8.2.0.jar:./lib/lucene-spatial3d-8.2.0.jar:./lib/lucene-suggest-8.2.0.jar:./lib/elasticsearch-cli-7.4.2.jar:./lib/jopt-simple-5.0.2.jar:./lib/joda-time-2.10.3.jar:./lib/t-digest-3.2.jar:./lib/HdrHistogram-2.1.9.jar:./lib/jna-4.5.1.jar:./lib/opencsv-3.8.jar:./lib/commons-csv-1.7.jar:./lib/dateparser-1.0.6.jar:./lib/retree-1.0.4.jar:./lib/lombok-1.18.8.jar:./lib/commons-validator-1.6.jar:./lib/commons-digester-1.8.1.jar:./lib/commons-collections-3.2.2.jar:./lib/graph-ddl-0.2.3.jar:./lib/okapi-trees-0.2.3.jar:./lib/cats-core_2.11-1.0.1.jar:./lib/cats-macros_2.11-1.0.1.jar:./lib/cats-kernel_2.11-1.0.1.jar:./lib/machinist_2.11-0.6.2.jar:./lib/okapi-api-0.2.3.jar:./lib/upickle_2.11-0.6.6.jar:./lib/ujson_2.11-0.6.6.jar:./lib/fastparse_2.11-1.0.0.jar:./lib/fastparse-utils_2.11-1.0.0.jar:./lib/sourcecode_2.11-0.1.4.jar:./lib/scala-compiler-2.11.12.jar:./lib/scala-parser-combinators_2.11-1.0.4.jar:./lib/log4j-api-scala_2.11-11.0.jar:./lib/gremlin-groovy-3.2.5.jar:./lib/ivy-2.3.0.jar:./lib/groovy-2.4.11-indy.jar:./lib/groovy-groovysh-2.4.11-indy.jar:./lib/groovy-console-2.4.11.jar:./lib/groovy-templates-2.4.11.jar:./lib/groovy-swing-2.4.11.jar:./lib/jline-2.12.jar:./lib/groovy-json-2.4.11-indy.jar:./lib/groovy-jsr223-2.4.11-indy.jar:./lib/jbcrypt-0.4.jar:./lib/gremlin-test-3.2.5.jar:./lib/junit-benchmarks-0.7.2.jar:./lib/h2-1.3.171.jar:./lib/uuid-3.4.0.jar:./lib/grabbag-1.8.1.jar:./lib/uuid-3.2.jar:./lib/javaslang-2.0.4.jar:./lib/javaslang-match-2.0.4.jar:./lib/jool-0.9.14.jar:./lib/jts-core-1.16.0.jar:./lib/jooby-netty-1.6.8.jar:./lib/netty-transport-4.1.43.Final.jar:./lib/netty-resolver-4.1.43.Final.jar:./lib/netty-codec-4.1.43.Final.jar:./lib/netty-codec-http-4.1.43.Final.jar:./lib/netty-codec-http2-4.1.43.Final.jar:./lib/netty-common-4.1.43.Final.jar:./lib/netty-handler-4.1.43.Final.jar:./lib/netty-transport-native-epoll-4.1.43.Final.jar:./lib/netty-transport-native-unix-common-4.1.43.Final.jar:./lib/netty-transport-native-epoll-4.1.43.Final-linux-x86_64.jar:./lib/netty-tcnative-boringssl-static-2.0.25.Final-linux-x86_64.jar:./lib/jooby-scanner-1.1.3.jar:./lib/fast-classpath-scanner-2.0.7.jar:./lib/jooby-caffeine-1.6.8.jar:./lib/jooby-metrics-1.6.8.jar:./lib/metrics-healthchecks-4.0.2.jar:./lib/metrics-jvm-4.0.2.jar:./lib/jooby-apitool-1.6.0.jar:./lib/jackson-dataformat-yaml-2.9.8.jar:./lib/jackson-datatype-jdk8-2.9.8.jar:./lib/swagger-core-1.5.20.jar:./lib/swagger-models-1.5.20.jar:./lib/swagger-annotations-1.5.20.jar:./lib/validation-api-1.1.0.Final.jar:./lib/swagger-parser-1.0.36.jar:./lib/slf4j-ext-1.6.3.jar:./lib/cal10n-api-0.7.4.jar:./lib/swagger-ui-3.17.1.jar:./lib/swagger-ui-themes-3.0.0.jar:./lib/api-console-3.0.17.jar:./lib/jooby-jackson-1.6.8.jar:./lib/jackson-datatype-jsr310-2.10.1.jar:./lib/jackson-module-parameter-names-2.10.1.jar:./lib/jackson-module-afterburner-2.10.1.jar:./lib/jooby-quartz-1.6.8.jar:./lib/quartz-2.3.0.jar:./lib/c3p0-0.9.5.2.jar:./lib/mchange-commons-java-0.2.11.jar:./lib/HikariCP-java6-2.3.13.jar:./lib/logback-classic-1.2.3.jar:./lib/jansi-1.16.jar:./lib/analysis-common-7.4.2.jar:./lib/snowflake-1.0.1.jar:./lib/commons-io-2.5.jar:./lib/owlapi-api-4.0.2.jar:./lib/xz-1.5.jar:./lib/trove4j-3.0.3.jar:./lib/jsr305-2.0.1.jar:./lib/org.apache.commons.io-2.4.jar:./lib/owlapi-apibinding-4.0.2.jar:./lib/owlapi-impl-4.0.2.jar:./lib/owlapi-parsers-4.0.2.jar:./lib/owlapi-oboformat-4.0.2.jar:./lib/owlapi-tools-4.0.2.jar:./lib/owlapi-fixers-4.0.2.jar:./lib/owlapi-rio-4.0.2.jar:./lib/sesame-model-2.7.12.jar:./lib/sesame-util-2.7.12.jar:./lib/sesame-rio-api-2.7.12.jar:./lib/sesame-rio-languages-2.7.12.jar:./lib/sesame-rio-datatypes-2.7.12.jar:./lib/sesame-rio-binary-2.7.12.jar:./lib/sesame-rio-n3-2.7.12.jar:./lib/sesame-rio-nquads-2.7.12.jar:./lib/sesame-rio-ntriples-2.7.12.jar:./lib/sesame-rio-rdfjson-2.7.12.jar:./lib/sesame-rio-rdfxml-2.7.12.jar:./lib/sesame-rio-trix-2.7.12.jar:./lib/sesame-rio-turtle-2.7.12.jar:./lib/sesame-rio-trig-2.7.12.jar:./lib/jsonld-java-sesame-0.5.0.jar:./lib/semargl-sesame-0.6.1.jar:./lib/semargl-core-0.6.1.jar:./lib/semargl-rdfa-0.6.1.jar:./lib/semargl-rdf-0.6.1.jar:./lib/org.osgi.core-1.4.0.jar:./lib/commons-configuration-1.10.jar:./lib/jackson-annotations-2.10.4.jar:./lib/logback-core-1.2.3.jar:./lib/jna-3.0.9.jar:./lib/json-path-2.4.0.jar:./lib/json-smart-2.3.jar:./lib/accessors-smart-1.2.jar:./lib/asm-5.0.4.jar:./lib/jolokia-core-1.6.2.jar:./lib/json-simple-1.1.1.jar:./lib/jolokia-jvm-1.6.2-agent.jar:./lib/groovy-2.4.6.jar:./lib/groovy-xml-2.4.6.jar:./lib/httpclient-4.5.2.jar:./lib/httpcore-4.4.4.jar:./lib/commons-codec-1.9.jar:./lib/httpmime-4.5.1.jar"
classPathProj="./lib/fuse-core-0.51-SNAPSHOT.jar:./lib/fuse-dv-epb-0.51-SNAPSHOT.jar:./lib/fuse-dv-core-0.51-SNAPSHOT.jar:./lib/fuse-dv-unipop-0.51-SNAPSHOT.jar:./lib/fuse-asg-0.51-SNAPSHOT.jar:./lib/fuse-dv-unipop-0.51-SNAPSHOT.jar:./lib/unipop-core-0.51-SNAPSHOT.jar:./lib/fuse-dv-asg-0.51-SNAPSHOT.jar:./lib/fuse-dv-gta-0.51-SNAPSHOT.jar:./lib/fuse-service-0.51-SNAPSHOT.jar:./lib/fuse-dv-stat-0.51-SNAPSHOT.jar:./lib/fuse-geojson-0.51-SNAPSHOT.jar:./lib/fuse-domain-cyber-ext-0.51-SNAPSHOT.jar"
classPath=${classPath3rd}:${classPathProj}

argName=
for var in "$@"
do
    if [ "${argName}" = "" ]; then
        if [ "${var}" = "--heapSize" ]; then
            argName=heapSize
        elif [ "${var}" = "--elasticsearch.embedded" ]; then
            argName=elasticsearchEmbedded
        elif [ "${var}" = "--elasticsearch.hosts" ]; then
            argName=elasticsearchHosts
        elif [ "${var}" = "--elasticsearch.cluster_name" ]; then
            argName=elasticsearchClusterName
        elif [ "${var}" = "--elasticsearch.port" ]; then
            argName=elasticsearchTcpPort
        elif [ "${var}" = "--config" ]; then
            argName=configFile
        elif [ "${var}" = "--logConfig" ]; then
            argName=logbackConfigurationFilename
        elif [ "${var}" = "--activeProfile" ]; then
            argName=activeProfile
        elif [ "${var}" = "--debug" ]; then
            debugParams=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
        elif [ "${var}" = "--jmx" ]; then
            jmxEnable=true
            jmxPort=6979
        fi
    elif [ "${argName}" != "" ]; then
        declare "${argName}=${var}"
        argName=
    fi
done

systemProperties=
if [ "${jmxEnabled}" != "" ]; then
    systemProperties="${systemProperties} -Dcom.sun.management.jmxremote=${jmxEnabled}"
    systemProperties="${systemProperties} -Dcom.sun.management.jmxremote.port=${jmxPort}"
    systemProperties="${systemProperties} -Dcom.sun.management.jmxremote.authenticate=false"
    systemProperties="${systemProperties} -Dcom.sun.management.jmxremote.ssl=false"
fi

if [ "${elasticsearchEmbedded}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.embedded=${elasticsearchEmbedded}"
	echo ElasticSearch embedded param: -Delasticsearch.embedded=${elasticsearchEmbedded}
fi
if [ "${elasticsearchHosts}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.hosts=${elasticsearchHosts}"
	echo ElasticSearch hosts param: -Delasticsearch.hosts=${elasticsearchHosts}
fi

if [ "${elasticsearchClusterName}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.cluster_name=${elasticsearchClusterName}"
	echo ElasticSearch cluster param: -Delasticsearch.cluster_name=${elasticsearchClusterName}
fi

if [ "${elasticsearchTcpPort}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.port=${elasticsearchTcpPort}"
	echo ElasticSearch TCP port param:  -Delasticsearch.port=${elasticsearchTcpPort}
fi

#jolokia = -javaagent:lib/jolokia-jvm-1.6.2-agent.jar=port=8088,host=localhost

echo java -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}

#java ${jolokia} -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
java -Dconfig.override_with_env_vars=true -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
