#!/bin/sh

if [ "$JAKE_HOME" = "" ]; then
    export JAKE_HOME=`pwd`/../../../../../jake
fi
echo "This is Jdk9/Jigsaw build to be tested:"
echo "  JAKE_HOME=$JAKE_HOME "
echo "If you want test other, export it's path as JAKE_HOME"

export PATH=$JAKE_HOME/bin:$PATH

java -version

export NO_HARNESS=`pwd`

if [ -d "$1" ]; then
    cd $1
    RUNALL_FAILED=all-failed.txt
    LOG_NAME=all-`date +%Y-%m-%d_%H%M`
    if [ -f "work/run" ]; then
        cd work
        . run
    else
        . runall | tee $LOG_NAME.txt
    fi
    cd $NO_HARNESS
else
    . runall | tee $LOG_NAME.txt
fi

function rmp() {
    if [ -f $1 ]; then
        rm -rfv $1
    fi
}

cd $NO_HARNESS

rmp no-harness/fromjava/default_pkg/work/log.txt
rmp no-harness/epr/w3cepr_6675760/work/log.txt
rmp no-harness/bugs/jaxws1050/work/log.txt

java -version
