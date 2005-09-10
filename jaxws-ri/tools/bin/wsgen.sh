#!/bin/sh

#
# $Id: wsgen.sh,v 1.2 2005/05/20 00:14:36 vivekp Exp $
#

#
# The contents of this file are subject to the terms
# of the Common Development and Distribution License
# (the "License").  You may not use this file except
# in compliance with the License.
# 
# You can obtain a copy of the license at
# https://jwsdp.dev.java.net/CDDLv1.0.html
# See the License for the specific language governing
# permissions and limitations under the License.
# 
# When distributing Covered Code, include this CDDL
# HEADER in each file and include the License file at
# https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
# add the following below this CDDL HEADER, with the
# fields enclosed by brackets "[]" replaced with your
# own identifying information: Portions Copyright [yyyy]
# [name of copyright owner]
#

if [ -z "$JAVA_HOME" ]; then
    echo "ERROR: Set JAVA_HOME to the path where the J2SE (JDK) is installed (e.g., /usr/java/jdk1.3)"
    exit 1
fi

if [ -z "$JAXWS_HOME" ]; then
    echo "ERROR: Set JAXWS_HOME to the root of a JAXWS-RI distribution (e.g., /usr/bin/jaxws-ri/build)"
    exit 1
fi


CLASSPATH=.:$JAXWS_HOME/lib/jaxws-rt.jar:$JAXWS_HOME/lib/jaxws-tools.jar:$JAXWS_HOME/lib/jaxws-api.jar:$JAXWS_HOME/lib/activation.jar:$JAXWS_HOME/lib/saaj-api.jar:$JAXWS_HOME/lib/saaj-impl.jar:$JAXWS_HOME/lib/relaxngDatatype.jar:$JAXWS_HOME/lib/jaxb-xjc.jar:$JAXWS_HOME/lib/jsr173_api.jar:$JAXWS_HOME/lib/sjsxp.jar:$JAXWS_HOME/lib/jaxb-api.jar:$JAXWS_HOME/lib/jaxb-impl.jar:$JAXWS_HOME/lib/jaxb-libs.jar:$JAXWS_HOME/lib/jsr181-api.jar:$JAVA_HOME/lib/tools.jar

$JAVA_HOME/bin/java -cp "$CLASSPATH" com.sun.tools.ws.WsGen "$@"
