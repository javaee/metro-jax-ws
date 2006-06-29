@echo off

REM
REM The contents of this file are subject to the terms
REM of the Common Development and Distribution License
REM (the License).  You may not use this file except in
REM compliance with the License.
REM 
REM You can obtain a copy of the license at
REM https://glassfish.dev.java.net/public/CDDLv1.0.html.
REM See the License for the specific language governing
REM permissions and limitations under the License.
REM 
REM When distributing Covered Code, include this CDDL
REM Header Notice in each file and include the License file
REM at https://glassfish.dev.java.net/public/CDDLv1.0.html.
REM If applicable, add the following below the CDDL Header,
REM with the fields enclosed by brackets [] replaced by
REM you own identifying information:
REM "Portions Copyrighted [year] [name of copyright owner]"
REM 
REM Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
%JAVA% %WSGEN_OPTS% -cp "%JAVA_HOME%\lib\tools.jar;%JAXWS_HOME%\lib\jaxws-tools.jar" com.sun.tools.ws.WsGen %*

:END
%COMSPEC% /C exit %ERRORLEVEL%
