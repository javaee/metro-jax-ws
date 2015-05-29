#!/bin/bash -xe

if [ $# = 0 ]; then
   # for convinience default bundle is mac version to run it locally
   export BUNDLE=http://prg10044.cz.oracle.com/hudson/view/JIGSAW/job/no-harness-JAKE-install/ws/macosx_x64_10.9-fastdebug.zip
   echo "No jdk bundle provided, trying to download bundle from hudson:\n       $BUNDLE..."
else
   export BUNDLE=$1
fi;

export JAKE_HOME=`pwd`/../../../../../jake

if [ -d "$JAKE_HOME" ]; then
    mv $JAKE_HOME $JAKE_HOME-`date +%Y-%m-%d_%H-%M-%S`
fi

mkdir -p "$JAKE_HOME"
cd "$JAKE_HOME"
wget $BUNDLE
unzip `basename $BUNDLE`
echo "ok, java unzipped to [$JAKE_HOME]. Java version:"
$JAKE_HOME/bin/java -version 2>&1