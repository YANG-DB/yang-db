@echo off

set env=%1
set debug=%2
set debugParams=

set mainClass="com.kayhut.fuse.services.FuseRunner"
set configFile="config/application.%env%.conf"
set classPath=".;lib/*"

if "%debug%"=="true" (
    set debugParams="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
)

java %debugParams% -cp %classPath% %mainClass% %configFile%