@echo off

REM
REM $Id: wsgen.bat,v 1.2 2005-09-10 19:49:25 kohsuke Exp $
REM

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

if defined JAVA_HOME goto CONTA
echo ERROR: Set JAVA_HOME to the path where the J2SE (JDK) is installed (e.g., D:\jdk1.3)
goto END
:CONTA

if defined JAXWS_HOME goto CONTB
echo ERROR: Set JAXWS_HOME to the root of a JAXWS-RI distribution (e.g., D:\ws\jaxws-ri\build)
goto END
:CONTB

rem Get command line arguments and save them
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

setlocal

set CLASSPATH=.;%JAXWS_HOME%\lib\jaxws-rt.jar;%JAXWS_HOME%\lib\jaxws-tools.jar;%JAXWS_HOME%\lib\jaxws-api.jar;%JAXWS_HOME%\lib\activation.jar;%JAXWS_HOME%\lib\saaj-api.jar;%JAXWS_HOME%\lib\saaj-impl.jar;%JAXWS_HOME%\lib\relaxngDatatype.jar;%JAXWS_HOME%\lib\jaxb-xjc.jar;%JAXWS_HOME%\lib\jsr173_api.jar;%JAXWS_HOME%\lib\sjsxp.jar;%JAXWS_HOME%\lib\jaxb-api.jar;%JAXWS_HOME%\lib\jaxb-impl.jar;%JAXWS_HOME%\lib\jaxb-libs.jar;%JAXWS_HOME%\lib\jsr181-api.jar;%JAVA_HOME%\lib\tools.jar

%JAVA_HOME%\bin\java -cp "%CLASSPATH%" com.sun.tools.ws.WsGen %CMD_LINE_ARGS%

endlocal

:END