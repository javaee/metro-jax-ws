#!/bin/bash -ex

/net/prt-archiver.us.oracle.com/data/jprt/archive/west/dist/bin/jprt submit \
   -forest \
   -extratime 10m \
   -email miroslav.kos@oracle.com \
   -stree . \
   -noqa \
   -ot 'linux.* mac.*' \
   -testset all \
   -otests '.*jdk_other.*' \
   -nopostinstall