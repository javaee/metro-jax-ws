#!/bin/bash -ex

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