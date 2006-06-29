#! /bin/sh

#
# $Id: build.sh,v 1.1 2005/05/09 18:07:57 arung Exp $
#
# This script is optional and for convenience only.

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

if [ -z "$JAVA_HOME" ]
then
echo "Cannot find JAVA_HOME. Please set your JAVA_HOME."
exit 1
fi

JAVACMD=$JAVA_HOME/bin/java

cp=$JAVA_HOME/lib/tools.jar:./lib/ant.jar:./lib/ant-junit.jar:./lib/ant-launcher.jar:./lib/ant-nodeps.jar:./lib/ant-trax.jar

$JAVACMD -Dfile.extension=sh -classpath $cp:$CLASSPATH org.apache.tools.ant.Main -emacs "$@"
