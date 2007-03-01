#Builds nightly binary and source bundles. 
#Bundles will be pushed to java.net.

cd jax-ws-sources/jaxws-ri

ant -Dbuild.id=jaxws-21-nightly_$BUILD_ID -DDSTAMP=$BUILD_NUMBER clean generate-weekly-binary generate-weekly-source
