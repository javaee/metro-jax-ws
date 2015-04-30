#!/bin/sh

export WS_HARNESS_SRC=~/dev/ws-harness

#svn co https://svn.java.net/svn/ws-test-harness~svn/trunk $WS_HARNESS_SRC
cd $WS_HARNESS_SRC/test-harness
mvn clean install -DskipTests=true

cd $WS_HARNESS_SRC//harness-maven-plugin
mvn clean install -DskipTests=true
