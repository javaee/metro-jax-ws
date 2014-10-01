#!/bin/bash -ex

echo "changes before filtering: "
hg status |wc -l

groovy filter.groovy diff.patch|tee diff.status

chmod a+x diff*revert.sh
cd jaxws && ../diff*revert.sh

echo "changes after filtering: "
hg status |wc -l
