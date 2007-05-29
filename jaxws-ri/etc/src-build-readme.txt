JAXWS RI Source Bundle README
----------------------------

This document describes the procedure for building the JAX-WS Reference Implementation.

Building the RI
---------------
   * unpack the src distribution
   * export JAXWS_SRC=<root of your jax-ws source bundle>
   * cd $JAXWS_SRC
   * $ANT_HOME/bin/ant
   Running the default target (main) will cause the entire source tree to be
   built and organized in the $JAXWS_SRC/build directory.
   export JAXWS_HOME=$JAXWS_SRC/build to use JAX-WS Tools in $JAXWS_SRC/build/bin

   * $ANT_HOME/bin/ant j2se-integration
   Running j2se-integration target prepares the sources for Java SE integration by renaming packages.


