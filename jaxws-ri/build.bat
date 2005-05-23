@echo off  

REM
REM $Id: build.bat,v 1.1 2005-05-23 22:07:16 bbissett Exp $
REM
REM This script is optional and for convenience only.

REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

echo JAX-RPC-RI Builder
echo -------------------

if "%JAVA_HOME%" == "" goto javaerror

if "%JAXWS_HOME%" == "" goto jaxwserror

set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAXWS_HOME%\..\lib\ant.jar;%JAXWS_HOME%\..\lib\ant-junit.jar;%JAXWS_HOME%\..\lib\ant-launcher.jar;%JAXWS_HOME%\..\lib\ant-nodeps.jar;%JAXWS_HOME%\..\lib\ant-trax.jar;%ADDITIONALCLASSPATH%
set ANT_HOME=./lib

echo Building with classpath %LOCALCLASSPATH%

echo Starting Ant...

%JAVA_HOME%\bin\java.exe -Dant.home="%ANT_HOME%" -Dfile.extension=bat -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main -emacs %CMD_LINE_ARGS%

goto end

:javaerror

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

goto end

:jaxwserror

echo ERROR: JAXWS_HOME not found in your environment.
echo Please, set the JAXWS_HOME variable in your environment to match the
echo location of jaxws-ri/build directory you want to use.

:end

set LOCALCLASSPATH=

