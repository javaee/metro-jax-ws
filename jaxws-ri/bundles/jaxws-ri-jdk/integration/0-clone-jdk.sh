#!/bin/bash -ex

if [ x$1 == "x" ] ; then
  JDK_REPO=http://hg.openjdk.java.net/jdk9/dev
else
  JDK_REPO=$1
fi

hg clone $JDK_REPO jdk

cd jdk
sh get_source.sh
