#!/bin/bash -x
#
# generate the architecture document and deploy that into the java.net CVS repository
#

ant architecture-document

cd build

if [ -e jaxws-architecture-document-www ]
then
  cd jaxws-architecture-document-www
  cvs update -Pd
  cd ..
else
  cvs "-d:pserver:kohsuke@kohsuke.sfbay:/cvs" -z9 co -d jaxws-architecture-document-www jax-ws-architecture-document/www
fi

cd jaxws-architecture-document-www

cp -R ../javadoc/* doc

# ignore everything under CVS, then
# ignore all files that are already in CVS, then
# add the rest of the files
find . -name CVS -prune -o -exec bash ../../tools/scripts/in-cvs.sh {} \; -o \( -print -a -exec cvs add {} \+ \)

# sometimes the first commit fails
cvs commit -m "commit 1 " || cvs commit -m "commit 2"
