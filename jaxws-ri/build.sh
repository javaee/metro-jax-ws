#! /bin/sh

#
# $Id: build.sh,v 1.1 2005/05/09 18:07:57 arung Exp $
#
# This script is optional and for convenience only.

#
# Copyright 2005 Sun Microsystems, Inc. All rights reserved.
# SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#

if [ -z "$JAVA_HOME" ]
then
echo "Cannot find JAVA_HOME. Please set your JAVA_HOME."
exit 1
fi

JAVACMD=$JAVA_HOME/bin/java

cp=$JAVA_HOME/lib/tools.jar:./lib/ant.jar:./lib/ant-junit.jar:./lib/ant-launcher.jar:./lib/ant-nodeps.jar:./lib/ant-trax.jar

$JAVACMD -Dfile.extension=sh -classpath $cp:$CLASSPATH org.apache.tools.ant.Main -emacs "$@"
