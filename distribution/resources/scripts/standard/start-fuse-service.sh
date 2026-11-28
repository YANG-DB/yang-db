#!/bin/bash
mainClass=com.yangdb.fuse.services.FuseRunner
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
        elif [ "${var}" = "--opensearch.hosts" ]; then
            argName=elasticsearchHosts
        elif [ "${var}" = "--opensearch.cluster_name" ]; then
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

#jolokia = -javaagent:lib/jolokia-jvm-1.6.2-agent.jar=port=8088,host=localhost

echo java ${jvmOpens} -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}

#java ${jolokia} -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
# the packages older libraries (Guice's cglib, netty, collections) reach into by reflection: JDK 11 only warned, JDK 17
# and 21 refuse, and the engine doesn't start (J1). The same set the tests get (test.jvm.opens in the root pom); JDK 11
# accepts these flags too. (--illegal-access, used before, was removed in JDK 17.)
jvmOpens="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.regex=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/sun.nio.ch=ALL-UNNAMED"
java ${jvmOpens} -Xmx${heapSize} -Xms${heapSize} ${systemProperties} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
