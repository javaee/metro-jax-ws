#!/bin/bash -ex
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
# or packager/legal/LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at packager/legal/LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
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

export JAXWS_HOME=`pwd`/../jaxws-ri
export JDK_HOME=`pwd`

cleanModule() {
    rm -rf jaxws/src/$1/share/classes/$2
}

# clean all local modifications
cd $JDK_HOME/jaxws
rm -rf src
hg revert -C .
cd ..

cleanModule java.annotations.common '*'
cleanModule java.xml.bind '*'
cleanModule java.xml.soap '*'
cleanModule java.xml.ws '*'
cleanModule jdk.xml.bind com
cleanModule jdk.xml.bind org
cleanModule jdk.xml.ws com

rm -rf work || true
mkdir -p work
cp $JAXWS_HOME/bundles/jaxws-ri-jdk/target/jax*sources.jar work
cd work
unzip jax*sources.jar
rm -rf jax*sources.jar META-INF

extractTo() {
    directory=`dirname $2`
    mkdir -p $JDK_HOME/jaxws/src/$1/share/classes/$directory
    mv $2 $JDK_HOME/jaxws/src/$1/share/classes/$directory
}

#java.annotations.common
extractTo java.annotations.common javax/annotation

#java.xml.soap
extractTo java.xml.soap javax/xml/soap
extractTo java.xml.soap com/sun/xml/internal/messaging/saaj

#jdk.xml.ws
extractTo jdk.xml.ws com/sun/tools/internal/ws

#java.xml.ws
extractTo java.xml.ws javax/xml/ws 
extractTo java.xml.ws javax/jws 
extractTo java.xml.ws com/oracle/webservices/internal 
extractTo java.xml.ws com/oracle/xmlns/internal/webservices/jaxws_databinding 
extractTo java.xml.ws com/sun/org/glassfish 
extractTo java.xml.ws com/sun/xml/internal/ws 
extractTo java.xml.ws com/sun/xml/internal/stream 


#jdk.xml.bind
extractTo jdk.xml.bind org/relaxng 
extractTo jdk.xml.bind com/sun/codemodel/internal 
extractTo jdk.xml.bind com/sun/istack/internal/tools 
extractTo jdk.xml.bind com/sun/tools/internal/jxc
extractTo jdk.xml.bind com/sun/tools/internal/xjc 
extractTo jdk.xml.bind com/sun/xml/internal/dtdparser
extractTo jdk.xml.bind com/sun/xml/internal/rngom 
extractTo jdk.xml.bind com/sun/xml/internal/xsom 

#java.xml.bind KEEP THIS LAST!!!
extractTo java.xml.bind javax/xml/bind
extractTo java.xml.bind com/sun/istack/internal
extractTo java.xml.bind com/sun/xml/internal

cd $JDK_HOME/jaxws
hg addremove src
hg status -a -r