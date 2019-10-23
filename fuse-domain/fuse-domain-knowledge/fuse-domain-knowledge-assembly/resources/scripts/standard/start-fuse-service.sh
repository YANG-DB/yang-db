#!/bin/bash

mainClass=com.yangdb.fuse.services.FuseRunner
configFile=config/application.conf
classPath=".:lib/*"
activeProfile=activeProfile
logbackConfigurationFilename=config/logback.xml
heapSize=1g

elasticsearchEmbedded="${ELASTICSEARCH_EMBEDDED:-true}"
elasticsearchHosts="${ELASTICSEARCH_HOST:-localhost}"
elasticsearchClusterName="${ELASTICSEARCH_CLUSTER_NAME}"
elasticsearchTcpPort="${ELASTICSEARCH_TCP_PORT:-9300}"

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
