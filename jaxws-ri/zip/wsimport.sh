#!/bin/sh

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License. You can obtain
# a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
# or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
# 
# When distributing the software, include this License Header Notice in each
# file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
# Sun designates this particular file as subject to the "Classpath" exception
# as provided by Sun in the GPL Version 2 section of the License file that
# accompanied this code.  If applicable, add the following below the License
# Header, with the fields enclosed by brackets [] replaced by your own
# identifying information: "Portions Copyrighted [year]
# [name of copyright owner]"
# 
# Contributor(s):
# 
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

#
# Script to run WSDeploy

if [ -z "$JAVA_HOME" ]; then
	echo "ERROR: Set JAVA_HOME to the path where the J2SE (JDK) is installed (e.g., /usr/java/jdk1.4)"
	exit 1
fi

# Resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
WEBSERVICES_LIB=$PRGDIR/../..

# Set the default -Djava.endorsed.dirs argument
JAVA_ENDORSED_DIRS="$JAVA_HOME"/lib:"$WEBSERVICES_LIB"/jaxp/lib/endorsed

# Set CLASSPATH
CLASSPATH=.:$WEBSERVICES_LIB/jaxrpc/lib/jaxrpc-impl.jar:$WEBSERVICES_LIB/jaxrpc/lib/jaxrpc-api.jar:$WEBSERVICES_LIB/jaxrpc/lib/jaxrpc-spi.jar:$WEBSERVICES_LIB/saaj/lib/activation.jar:$WEBSERVICES_LIB/saaj/lib/saaj-api.jar:$WEBSERVICES_LIB/saaj/lib/saaj-impl.jar:$WEBSERVICES_LIB/saaj/lib/mail.jar:$WEBSERVICES_LIB/jaxp/lib/jaxp-api.jar:$WEBSERVICES_LIB/jaxp/endorsed/lib/dom.jar:$WEBSERVICES_LIB/jaxp/endorsed/lib/sax.jar:$WEBSERVICES_LIB/jaxp/endorsed/lib/xalan.jar:$WEBSERVICES_LIB/jaxp/endorsed/lib/xercesImpl.jar:$WEBSERVICES_LIB/jwsdp-shared/lib/jax-qname.jar:$WEBSERVICES_LIB/jwsdp-shared/lib/relaxngDatatype.jar:$WEBSERVICES_LIB/jwsdp-shared/lib/xsdlib.jar:$JAVA_HOME/lib/tools.jar


cygwin=false;
case "`uname`" in
    CYGWIN*) cygwin=true ;;
esac

if $cygwin; then
  JAVA_HOME=`cygpath --windows "$JAVA_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi

$JAVA_HOME/bin/java -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" com.sun.xml.tools.ws.WsImport "$@"
