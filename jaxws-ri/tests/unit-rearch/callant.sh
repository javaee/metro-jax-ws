#!/bin/bash -e
 
ant $@
 
antReturnCode=$? < /dev/null
 
echo "ANT: Return code is: \""$antReturnCode"\""