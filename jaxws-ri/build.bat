@echo off  

REM
REM $Id: build.bat,v 1.4 2005-09-10 19:47:09 kohsuke Exp $
REM
REM This script is optional and for convenience only.

REM
REM The contents of this file are subject to the terms
REM of the Common Development and Distribution License
REM (the "License").  You may not use this file except
REM in compliance with the License.
REM 
REM You can obtain a copy of the license at
REM https://jwsdp.dev.java.net/CDDLv1.0.html
REM See the License for the specific language governing
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL
REM HEADER in each file and include the License file at
REM https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
REM add the following below this CDDL HEADER, with the
REM fields enclosed by brackets "[]" replaced with your
REM own identifying information: Portions Copyright [yyyy]
REM [name of copyright owner]
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

