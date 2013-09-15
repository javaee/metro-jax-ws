@echo off

REM
REM $Id: wsgen.bat,v 1.15 2007-05-30 01:03:26 ofung Exp $
REM

REM
REM DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
REM 
REM Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
REM 
REM The contents of this file are subject to the terms of either the GNU
REM General Public License Version 2 only ("GPL") or the Common Development
REM and Distribution License("CDDL") (collectively, the "License").  You
REM may not use this file except in compliance with the License. You can obtain
REM a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
REM or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
REM language governing permissions and limitations under the License.
REM 
REM When distributing the software, include this License Header Notice in each
REM file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
REM Sun designates this particular file as subject to the "Classpath" exception
REM as provided by Sun in the GPL Version 2 section of the License file that
REM accompanied this code.  If applicable, add the following below the License
REM Header, with the fields enclosed by brackets [] replaced by your own
REM identifying information: "Portions Copyrighted [year]
REM [name of copyright owner]"
REM 
REM Contributor(s):
REM 
REM If you wish your version of this file to be governed by only the CDDL or
REM only the GPL Version 2, indicate your decision by adding "[Contributor]
REM elects to include this software in this distribution under the [CDDL or GPL
REM Version 2] license."  If you don't indicate a single choice of license, a
REM recipient has the option to distribute your version of this file under
REM either the CDDL, the GPL Version 2 or to extend the choice of license to
REM its licensees as provided above.  However, if you add GPL Version 2 code
REM and therefore, elected the GPL Version 2 license, then the option applies
REM only if the new code is made subject to such option by the copyright
REM holder.
REM

if defined JAVA_HOME goto CONTA
echo ERROR: Set JAVA_HOME to the path where the J2SE (JDK) is installed (e.g., D:\jdk1.4)
goto END
:CONTA

set PRG=%0
set WEBSERVICES_LIB=%PRG%\..\..\..

set JAVA_ENDORSED_DIRS=%JAVA_HOME%\lib;%WEBSERVICES_LIB%\jaxp\lib\endorsed

rem Get command line arguments and save them
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

setlocal

set CLASSPATH=%WEBSERVICES_LIB%\jaxrpc\lib\jaxrpc-impl.jar;%WEBSERVICES_LIB%\jaxrpc\lib\jaxrpc-api.jar;%WEBSERVICES_LIB%\jaxrpc\lib\jaxrpc-spi.jar;%WEBSERVICES_LIB%\saaj\lib\activation.jar;%WEBSERVICES_LIB%\saaj\lib\saaj-api.jar;%WEBSERVICES_LIB%\saaj\lib\saaj-impl.jar;%WEBSERVICES_LIB%\saaj\lib\mail.jar;%WEBSERVICES_LIB%\jaxp\lib\jaxp-api.jar;%WEBSERVICES_LIB%\jaxp\endorsed\lib\dom.jar;%WEBSERVICES_LIB%\jaxp\endorsed\lib\sax.jar;%WEBSERVICES_LIB%\jaxp\endorsed\lib\xalan.jar;%WEBSERVICES_LIB%\jaxp\endorsed\lib\xercesImpl.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\jax-qname.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\relaxngDatatype.jar;%WEBSERVICES_LIB%\jwsdp-shared\lib\xsdlib.jar;%JAVA_HOME%\lib\tools.jar

"%JAVA_HOME%\bin\java" -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -cp "%CLASSPATH%" com.sun.tools.ws.WsGen %CMD_LINE_ARGS%

endlocal

:END
