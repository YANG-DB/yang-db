@echo off
setlocal enabledelayedexpansion

set mainClass=com.yangdb.fuse.services.FuseRunner
set configFile=config/application.conf
set classPath=".;lib/*"
set activeProfile=activeProfile
set logbackConfigurationFilename=config/logback.xml
set heapSize=1g

set argName=
for %%x in (%*) do (
	if [!argName!]==[] (
		if %%~x==--heapSize (
			set argName=heapSize
		) else if "%%~x"=="--opensearch.hosts" (
			set argName=elasticsearchHosts
		) else if "%%~x"=="--opensearch.cluster_name" (
		    set argName=elasticsearchClusterName
		) else if "%%~x"=="--config" (
            set argName=configFile
        ) else if "%%~x"=="--logConfig" (
            set argName=logbackConfigurationFilename
        ) else if "%%~x"=="--activeProfile" (
            set argName=activeProfile
        ) else if "%%~x"=="--debug" (
			set debugParams=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
		) else if "%%~x"=="--jmx" (
		    set jmxEnable=true
		    set jmxPort=6979
		)
	) else if not "!argName!"=="" (
		set !argName!=%%~x
		set argName=
	)
)

set systemProperties=
if not "!jmxEnable!"=="" (
    set systemProperties=!systemProperties! -Dcom.sun.management.jmxremote=!jmxEnable!
    set systemProperties=!systemProperties! -Dcom.sun.management.jmxremote.port=!jmxPort!
    set systemProperties=!systemProperties! -Dcom.sun.management.jmxremote.authenticate=false
    set systemProperties=!systemProperties! -Dcom.sun.management.jmxremote.ssl=false
)

if not "!elasticsearchHosts!"=="" (
	set systemProperties=!systemProperties! -Delasticsearch.hosts=!elasticsearchHosts!
)

if not "!elasticsearchClusterName!"=="" (
	set systemProperties=!systemProperties! -Delasticsearch.cluster_name=!elasticsearchClusterName!
)

echo java -Xmx!heapSize! -Xms!heapSize! !systemProperties! !debugParams! -cp %classPath% %mainClass% !configFile! !activeProfile! !logbackConfigurationFilename!
java -Xmx!heapSize! -Xms!heapSize! !systemProperties! !debugParams! -cp %classPath% %mainClass% !configFile! !activeProfile! !logbackConfigurationFilename!