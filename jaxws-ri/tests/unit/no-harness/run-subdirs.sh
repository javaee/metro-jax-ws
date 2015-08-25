#!/bin/bash -ex

START=$(date +%s)
GLOBAL_LOG_PREFIX=`pwd`/`date +%Y-%m-%d_%HH-%MM`

G_STATUS=0
wget http://127.0.0.1:8888/stop

echo "==================================================================== global variables ============"
echo "      G_STATUS=[$G_STATUS] ~ result so far of current testcase"
echo "      useagent=[$useagent] ~ if true then java and tools use aggent "
echo "         debug=[$debug] ~ silent / verbose mode"
echo "      failFast=[$failFast] ~ if true it won't continue with other tests for given webservice"
echo "     skipTests=[$skipTests] ~ run tests just up to deploy / all (inncluding client tests)"
echo "useNamedModule=[$useNamedModule] ~ run as unnamed module / named (including module-info.java)"
echo "=================================================================================================="

if [ "$useagent" = "true" ]; then
    export JAVA_OPTS=-javaagent:$NO_HARNESS/agent-0.1.jar
    export TOOL_JAVA_OPTS=-J-javaagent:$NO_HARNESS/agent-0.1.jar
else
    export JAVA_OPTS=
    export TOOL_JAVA_OPTS=
fi;

rm -rf runall-failed.bak
mv runall-failed runall-failed.bak
export RUNALL_FAILED=`pwd`/runall-failed

echo "JAVA_OPTS=$JAVA_OPTS"
echo "TOOL_JAVA_OPTS=$TOOL_JAVA_OPTS"

# set counters
export TOTAL_STEPS=0
export TOTAL_PASSED=0
export TOTAL_FAILED=0

export PASSED=0
export FAILED=0

echo "JAVA_OPTS=$JAVA_OPTS"

DIR=$1
if [ "$DIR" == "" ]; then
    DIR=.
fi

SCRIPTS=`find $DIR -name "run"`
for S in $SCRIPTS; do
    D=`dirname $S`
    pushd $D > /dev/null
    . run
    popd > /dev/null
done

echo $(date +%Y-%m-%d,%H:%M:%S)
echo "java version: "
java -version 2>&1

echo "=================================================================================================="
echo "TOTAL_STEPS = $TOTAL_STEPS , TOTAL_PASSED = $TOTAL_PASSED , TOTAL_FAILED = $TOTAL_FAILED"
echo "PASSED = $PASSED , FAILED = $FAILED , skipTests = $skipTests"

END=$(date +%s)
DIFF=$((($END - $START) / 60)):$((($END - $START) % 60))
echo "It took $DIFF minutes"
echo "=================================================================================================="
