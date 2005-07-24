@echo off  

REM
REM $Id: build.bat,v 1.3 2005-07-24 01:34:53 kohlert Exp $
REM
REM This script is optional and for convenience only.

REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
REM

echo JAX-WS-RI Builder
echo -------------------

if "%JAVA_HOME%" == "" goto javaerror

set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar;.\lib\ant.jar;.\lib\ant-junit.jar;.\lib\ant-launcher.jar;.\lib\ant-nodeps.jar;.\lib\ant-trax.jar
set ANT_HOME=./lib

echo Building with classpath %LOCALCLASSPATH%

echo Starting Ant...

%JAVA_HOME%\bin\java.exe -Dant.home="%ANT_HOME%" -Dfile.extension=bat -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main -emacs %CMD_LINE_ARGS%

goto end

:javaerror

echo ERROR: JAVA_HOME not found in your environment.
echo Please, set the JAVA_HOME variable in your environment to match the
echo location of the Java Virtual Machine you want to use.

:end

set LOCALCLASSPATH=

