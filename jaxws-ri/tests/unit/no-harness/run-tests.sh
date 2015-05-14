#!/bin/sh

# export JAKE_HOME=~/java/jake-2015-05-06
if [ "$JAKE_HOME" = "" ]; then
    echo "JAKE_HOME not set. It must be set and point to Jdk9/Jigsaw home directory. Exiting."
    exit 1;
fi

# export WS_RI_SRC=~/dev/jaxws-ri/jaxws-ri
if [ "$WS_RI_SRC" = "" ]; then
    echo "WS_RI_SRC not set. It must be set and point to jaxws-ri sources directory.  Exiting."
    exit 1;
fi


export PATH=$JAKE_HOME/bin:$PATH

java -version

export NO_HARNESS=$WS_RI_SRC/tests/unit/no-harness

cd $NO_HARNESS

if [ -d "$1" ]; then
    D=`pwd`
    cd $1
    RUNALL_FAILED=all-failed.txt
    LOG_NAME=all-`date +%Y-%m-%d_%H%M`
    if [ -f "work/run" ]; then
        cd work
        . run
    else
        . runall | tee $LOG_NAME.txt
    fi
    cd $D
else
    . runall | tee $LOG_NAME.txt
fi

function rmp() {
    if [ -f $1 ]; then
        rm -rfv $1
    fi
}

cd ..

rmp no-harness/fromjava/default_pkg/work/log.txt
rmp no-harness/epr/w3cepr_6675760/work/log.txt
rmp no-harness/bugs/jaxws1050/work/log.txt

java -version
