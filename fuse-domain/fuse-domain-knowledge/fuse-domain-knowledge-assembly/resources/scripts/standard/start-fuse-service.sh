#!/bin/bash
mainClass=com.kayhut.fuse.services.FuseRunner
configFile=config/application.conf
classPath=".:lib/*"
activeProfile=activeProfile
logbackConfigurationFilename=config/logback.xml
heapSize=1g

argName=
for var in "$@"
do
    if [ "${argName}" = "" ]; then
        if [ "${var}" = "--heapSize" ]; then
            argName=heapSize
        elif [ "${var}" = "--elasticsearch.hosts" ]; then
            argName=elasticsearchHosts
        elif [ "${var}" = "--elasticsearch.cluster_name" ]; then
            argName=elasticsearchClusterName
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

if [ "${elasticsearchHosts}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.hosts=${elasticsearchHosts}"
fi

if [ "${elasticsearchClusterName}" != "" ]; then
	systemProperties="${systemProperties} -Delasticsearch.cluster_name=${elasticsearchClusterName}"
fi

echo java -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
java -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}