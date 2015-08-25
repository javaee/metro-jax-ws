#!/bin/bash

if [ "$JAKE_HOME" = "" ]; then
    export JAKE_HOME=`pwd`/../../../../../jake
fi
#echo "This is Jdk9/Jigsaw build to be tested:"
#echo "  JAKE_HOME=$JAKE_HOME "
#echo "If you want test other, export it's path as JAKE_HOME"

export PATH=$JAKE_HOME/bin:$PATH

#java -version

# prepare
#  - unsupported case
rm -rfv $NO_HARNESS/fromjava/default_pkg
#  - customized test cases
cp -rfv $NO_HARNESS/SHARED/customized/* $NO_HARNESS/

export NO_HARNESS=`pwd`

if [ -d "$1" ]; then
    RUNALL_FAILED=all-failed.txt
    LOG_NAME=all-`date +%Y-%m-%d_%H%M`
    if [ -f "$1/work/run" ]; then
        pushd $1/work
        . run
        popd
    else
        . run-subdirs.sh $1 | tee $LOG_NAME.txt
    fi
    cd $NO_HARNESS
else
    . run-subdirs.sh | tee $LOG_NAME.txt
fi

cd $NO_HARNESS


