@echo off

REM
REM  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM
REM  Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
REM
REM  The contents of this file are subject to the terms of either the GNU
REM  General Public License Version 2 only ("GPL") or the Common Development
REM  and Distribution License("CDDL") (collectively, the "License").  You
REM  may not use this file except in compliance with the License.  You can
REM  obtain a copy of the License at
REM  https://oss.oracle.com/licenses/CDDL+GPL-1.1
REM  or LICENSE.txt.  See the License for the specific
REM  language governing permissions and limitations under the License.
REM
REM  When distributing the software, include this License Header Notice in each
REM  file and include the License file at LICENSE.txt.
REM
REM  GPL Classpath Exception:
REM  Oracle designates this particular file as subject to the "Classpath"
REM  exception as provided by Oracle in the GPL Version 2 section of the License
REM  file that accompanied this code.
REM
REM  Modifications:
REM  If applicable, add the following below the License Header, with the fields
REM  enclosed by brackets [] replaced by your own identifying information:
REM  "Portions Copyright [year] [name of copyright owner]"
REM
REM  Contributor(s):
REM  If you wish your version of this file to be governed by only the CDDL or
REM  only the GPL Version 2, indicate your decision by adding "[Contributor]
REM  elects to include this software in this distribution under the [CDDL or GPL
REM  Version 2] license."  If you don't indicate a single choice of license, a
REM  recipient has the option to distribute your version of this file under
REM  either the CDDL, the GPL Version 2 or to extend the choice of license to
REM  its licensees as provided above.  However, if you add GPL Version 2 code
REM  and therefore, elected the GPL Version 2 license, then the option applies
REM  only if the new code is made subject to such option by the copyright
REM  holder.
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
%JAVA% %WSGEN_OPTS% -cp "%JAXWS_HOME%\lib\jaxws-tools.jar;%JAXWS_HOME%\lib\jaxws-rt.jar;%JAXWS_HOME%\lib\jaxb-xjc.jar;%JAXWS_HOME%\lib\jaxb-jxc.jar;%JAXWS_HOME%\lib\jaxb-api.jar;%JAXWS_HOME%\lib\jaxb-core.jar;%JAXWS_HOME%\lib\jaxb-impl.jar" com.sun.tools.ws.WsGen %*

:END
%COMSPEC% /C exit %ERRORLEVEL%
