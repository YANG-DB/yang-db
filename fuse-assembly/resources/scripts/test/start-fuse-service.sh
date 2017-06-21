env=$1
debug=$2
debugParams=""
mainClass="com.kayhut.fuse.services.FuseRunner"
configFile="config/application.${env}.conf"
vmArgs="-Xmx10g"
classPath=".:lib/*"
if [ "${debug}" = "true" ]; then
    debugParams="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
fi
java ${vmArgs} ${debugParams} -cp ${classPath} ${mainClass} ${configFile}