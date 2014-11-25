#!/bin/bash -x
# just convenient script to log JAXB/JAX-WS changes from previous integration
# as a parameter provide hash or tag of the last integration i.e.
#  ./gitlog 2.2.11-b140528.1207
git log --pretty=format:'%ai %ae %s' $1..