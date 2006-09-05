#!/bin/sh

#
# $Id: wsimport.sh,v 1.4 2006-09-05 17:36:24 jitu Exp $
#

#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the License).  You may not use this file except in
# compliance with the License.
# 
# You can obtain a copy of the license at
# https://glassfish.dev.java.net/public/CDDLv1.0.html.
# See the License for the specific language governing
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# Header Notice in each file and include the License file
# at https://glassfish.dev.java.net/public/CDDLv1.0.html.
# If applicable, add the following below the CDDL Header,
# with the fields enclosed by brackets [] replaced by
# you own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Copyright 2006 Sun Microsystems Inc. All Rights Reserved
#

if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: Set JAVA_HOME to the path where the J2SE (JDK) is installed (e.g., /usr/java/jdk1.3)"
    exit 1
fi

bin_dir=`dirname $0`
WEBSERVICES_LIB=`cd $bin_dir/../share/lib; pwd`

CLASSPATH=$JAVA_HOME/lib/tools.jar:$WEBSERVICES_LIB/activation.jar:$WEBSERVICES_LIB/../jaxb/lib/jaxb-xjc.jar:$WEBSERVICES_LIB/jaxws-tools.jar:$WEBSERVICES_LIB/../../private/share/lib/jsr250-api.jar


exec $JAVA_HOME/bin/java $WSIMPORT_OPTS -cp "$CLASSPATH" com.sun.tools.ws.WsImport "$@"
