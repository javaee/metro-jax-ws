#!/bin/bash

export JAKE_HOME=`pwd`/../../../../../jake
export WS_RI_SRC=`pwd`/../../../
export NO_HARNESS=`pwd`

#. build-harness.sh

cd $NO_HARNESS
. build-tests.sh $1

cd $NO_HARNESS
. run-tests.sh $1
