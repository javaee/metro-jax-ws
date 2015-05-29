#!/bin/bash -e

export WS_HARNESS_SRC=`pwd`/../../../../../ws-harness

if [ ! -d "$WS_HARNESS_SRC" ]; then
    svn co https://svn.java.net/svn/ws-test-harness~svn/trunk $WS_HARNESS_SRC
fi;
cd $WS_HARNESS_SRC/test-harness
mvn clean install -DskipTests=true

cd $WS_HARNESS_SRC/harness-maven-plugin
mvn clean install -DskipTests=true
