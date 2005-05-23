#!/bin/sh

#
# $Id: wsgen.sh,v 1.2 2005/05/20 00:14:36 vivekp Exp $
#

#
# Copyright 2005 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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