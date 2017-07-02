@echo off
goto :main

:help
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
echo    -debug    : enable remote debugging on port 5005
goto :exit

:main
set flavour=%1
set flavourId=%2
set flavourName=
set debug=%3
set debugParams=
set activeProfile=activeProfile
set logbackConfigurationFilename=config/logback.xml

if "%flavour%"=="" (
    goto :help
)

if not "%flavour%"=="-flavour" (
    goto :help
)

if "%flavourId%"=="" (
    goto :help
)

if "%flavourId%"=="1" (
    set flavourName=test.engine1.m1.public
)
if "%flavourId%"=="2" (
    set flavourName=test.engine1.m1.private
)
if "%flavourId%"=="3" (
    set flavourName=test.engine1.m1.private.private
)
if "%flavourId%"=="4" (
    set flavourName=test.engine2.m1.dfs.public
)
if "%flavourId%"=="5" (
    set flavourName=test.engine2.m1.dfs.private
)
if "%flavourId%"=="6" (
    set flavourName=test.engine2.m1.dfs.private.private
)
if "%flavourId%"=="7" (
    set flavourName=test.engine2.m1.smart.public
)
if "%flavourId%"=="8" (
    set flavourName=test.engine2.m1.smart.private
)
if "%flavourId%"=="9" (
    set flavourName=test.engine2.m1.smart.private.private
)

if "%flavourName%"=="" (
    goto :help
)

set mainClass=com.kayhut.fuse.services.FuseRunner
set configFile=config/application.%flavourName%.conf
set classPath=".;lib/*"

if "%debug%"=="-debug" (
    set debugParams=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
)

echo java %debugParams% -cp %classPath% %mainClass% %configFile% %activeProfile% %logbackConfigurationFilename%
java %debugParams% -cp %classPath% %mainClass% %configFile% %activeProfile% %logbackConfigurationFilename%

:exit