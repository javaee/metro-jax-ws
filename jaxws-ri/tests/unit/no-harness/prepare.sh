#!/bin/bash -x

function initdir() {
    if [ -d "$1" ]; then
        rm -rf $1
    fi;

    mkdir -p $1
}

function initlib() {
    cp ../lib/ext/$1.jar SHARED/lib
    cp ../lib/ext/$1.jar SHARED/classes
    cd SHARED/classes
    jar -xvf $1.jar
    rm -rf $1.jar
    cd ../..
}

initdir SHARED/classes
initdir SHARED/lib

initlib jaxwsTestUtil
initlib xmlunit1.0

