#!/bin/sh -e

# pass as parametere what test should be run (all by default), i.e. fromjava/nosei
TESTS=$1

export WS_RI_SRC=`pwd`/../../../

sh -x prepare.sh

cd ..

export JAVA_HOME=$JAVA8_HOME
export PATH=$JAVA_HOME/bin:$PATH

#export DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
#-Dws.jvmOpts=$DEBUG \

export MAVEN_OPTS=
mvn -o clean test \
  -P jaxwsInJDK9 \
  -Dws.args=-generateTestSources \
  -Dws.test=testcases/$TESTS 2>&1 |tee no-harness/`date +%Y-%m-%d_%H%M`-harness-run.txt
