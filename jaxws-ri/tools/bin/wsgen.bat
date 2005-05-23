@echo off

REM
REM $Id: wsgen.bat,v 1.1 2005-05-23 23:09:28 bbissett Exp $
REM

REM
REM Copyright 2005 Sun Microsystems, Inc. All rights reserved.
REM SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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