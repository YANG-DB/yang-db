fuseHeapSize=$1
debug=$2

debugParams=""
activeProfile="activeProfile"
logbackConfigurationFilename="config/logback.xml"

mainClass=com.kayhut.fuse.services.FuseRunner
configFile=config/application.conf
classPath=".:lib/*"

if [ "${fuseHeapSize}" = "" ]; then
    fuseHeapSize="2g"
fi

if [ "${debug}" = "-debug" ]; then
    debugParams="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
fi

echo java -Xmx${fuseHeapSize} -Xms${fuseHeapSize} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}
java -Xmx${fuseHeapSize} -Xms${fuseHeapSize} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}