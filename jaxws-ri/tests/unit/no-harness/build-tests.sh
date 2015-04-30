#!/bin/sh

export WS_RI_SRC=~/dev/jaxws-ri/jaxws-ri

sh -x prepare.sh

# git clone git@orahub.oraclecorp.com:fmw-infra-metro/jaxws-ri.git $WS_RI_SRC

cd $WS_RI_SRC/tests/unit
git checkout jigsaw

export JAVA_HOME=$JAVA8_HOME
export PATH=$JAVA_HOME/bin:$PATH

#export DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
#-Dws.jvmOpts=$DEBUG \

export MAVEN_OPTS=
mvn -o clean test \
  -P jaxwsInJDK9 \
  -Dws.args=-generateTestSources \
  -Dws.test=testcases 2>&1 |tee no-harness/`date +%Y-%m-%d_%H%M`-harness-run.txt
