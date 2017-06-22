usage ()
{
    echo Usage: start-fuse-service.bat [-flavour id] [-debug]
    echo    -flavour id: the flavour id for the engine. Available ids are:
    echo                1 - test.engine1.m1.public
    echo                2 - test.engine1.m1.private
    echo                3 - test.engine1.m1.private.private
    echo                4 - test.engine1.m1.dfs.public
    echo                5 - test.engine1.m1.dfs.private
    echo                6 - test.engine1.m1.dfs.private.private
    echo                7 - test.engine1.m1.smart.public
    echo                8 - test.engine1.m1.smart.private
    echo                9 - test.engine1.m1.smart.private.private
    echo
    echo    -debug: enable remote debugging on port 5005
}


flavour=$1
flavourId=$2
flavourName=""
debug=$3
debugParams=""
activeProfile="activeProfile"
logbackConfigurationFilename="config/logback.xml"

if [ "${flavour}" = "" ]; then
    usage
    exit
elif [ "${flavour}" = "-flavour" ]; then
    usage
    exit
elif [ "${flavourId}" = "" ]; then
    usage
    exit
fi

if   [ "${flavourId}" = "1" ]; then
    flavourName="test.engine1.m1.public"
elif [ "${flavourId}" = "2" ]; then
    flavourName="test.engine1.m1.private"
elif [ "${flavourId}" = "3" ]; then
    flavourName="test.engine1.m1.private.private"
elif [ "${flavourId}" = "4" ]; then
    flavourName="test.engine1.m1.dfs.public"
elif [ "${flavourId}" = "5" ]; then
    flavourName="test.engine1.m1.dfs.private"
elif [ "${flavourId}" = "6" ]; then
    flavourName="test.engine1.m1.dfs.private.private"
elif [ "${flavourId}" = "7" ]; then
    flavourName="test.engine1.m1.smart.public"
elif [ "${flavourId}" = "8" ]; then
    flavourName="test.engine1.m1.dmart.private"
elif [ "${flavourId}" = "9" ]; then
    flavourName="test.engine1.m1.smart.private.private"
fi

if [ "${flavourName}" = "" ]; then
    usage
    exit
fi

mainClass="com.kayhut.fuse.services.FuseRunner"
configFile="config/application.${flavourName}.conf"
vmArgs="-Xmx10g -Xms10g"
classPath=".:lib/*"

if [ "${debug}" = "-debug" ]; then
    debugParams="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
fi

java ${vmArgs} ${debugParams} -cp ${classPath} ${mainClass} ${configFile} ${activeProfile} ${logbackConfigurationFilename}