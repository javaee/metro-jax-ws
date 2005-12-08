@echo off

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




rem
rem Infer JAXWS_HOME if not set
rem
if not "%JAXWS_HOME%" == "" goto CHECKJAVAHOME

rem Try to locate JAXWS_HOME
set JAXWS_HOME=%~dp0
set JAXWS_HOME=%JAXWS_HOME%\..
if exist %JAXWS_HOME%\lib\jaxws-tools.jar goto CHECKJAVAHOME

rem Unable to find it
echo JAXWS_HOME must be set before running this script
goto END

:CHECKJAVAHOME
if not "%JAVA_HOME%" == "" goto USE_JAVA_HOME

set JAVA=java
goto LAUNCH

:USE_JAVA_HOME
set JAVA="%JAVA_HOME%\bin\java"
goto LAUNCH

:LAUNCH
%JAVA% %WSIMPORT_OPTS% -cp %JAXWS_HOME%\lib\jaxws-tools.jar com.sun.tools.ws.WsImport %*

:END
%COMSPEC% /C exit %ERRORLEVEL%
