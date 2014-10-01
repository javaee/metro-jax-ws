#!/bin/bash -ex

cd jaxws

backup() {
    if [ -f $1 ] ; then
        echo "backuping existing file: "
        mv -v $1 $1.bak.`date +%Y-%m-%d`
    fi
}


backup ../diff.patch
backup ../diff.status

echo "doing diff"
hg diff > ../diff.patch

echo "diff finished: "
ls -al ../diff.patch