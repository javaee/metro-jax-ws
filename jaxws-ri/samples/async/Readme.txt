This sample will build, deploy an invoke a simple Web service.

This sample demonstrates the use of synchronous, 
asynchronous poll and asynchronous callback invocations 
when running a Web service client. The Web service 
has been developed from Java.  


2.0 Prerequisites

Here is the list of prerequisites that needs to be met before any of the samples can be invoked:

   1. Download Java 2 Standard Edition 5.0 Update 2 or later (J2SE 5.0) from java.sun.com/j2se/1.5.0/download.jsp. Set JAVA_HOME to the J2SE 5.0 installation directory.
   2. Download Sun Java System Application Server Platform Edition 8.1 2005 Q1 UR2 (SJSAS PE 8.1 or Application Server) or later from java.sun.com/j2ee/1.4/download.html. SJSAS PE 8.1 download page has J2EE 1.4 SDK 2005Q1 UR2 available as All-In-One bundle and SJSJAS PE 8.1 available as separate bundle. Make sure that you download SJSAS PE 8.1 separate bundle only and use J2SE 5.0 as the J2SE platform for it's installation. Although the download page requires J2SE 1.4.2 SDK for SJSAS PE 8.1, J2SE 5.0 is required for JAX-WS 2.0 binaries and must be used as the J2SE platform for SJSAS PE 8.1 for the samples to work. Set AS_HOME to point to the Application Server installation directory.
   3. Download Ant 1.6.2 or later from ant.apache.org and install it, lets say this is installed in ANT_HOME. Please note that AS_HOME/bin/asant cannot be used for building and invoking the samples since JAX-RPC 1.1 implementation is baked into this version of ant script. 
   4. Make sure that the Application Server is not running. Invoke ANT_HOME/bin/ant install from the root directory of the JAX-WS 2.0 EA2 bundle. This will install JAX-WS 2.0 bundle on the Application Server referred by AS_HOME environment variable.

3.0 Invoking the sample

Each sample can be built, deployed and invoked using the ANT_HOME/bin/ant and build.xml ant script in the root directory of the sample. Each ant script supports the following set of targets:
server 	Builds and deploy the service endpoint WAR
client 	Builds the client
run 	Runs the client

It is essential for the service endpoint to be deployed on Application Server before clients can be built because clients use the WSDL exposed from the service endpoint deployed in the Application Server. So please make sure that your Application Server is either running before the server target is invoked or run it after the server target is invoked. You will have to wait a few minutes for the Application Server to deploy the service endpoint correctly before building the client.

This sample will build, deploy an invoke a simple Web service.
* etc - configuration files
    * custom-client.xml client customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* apt ant task to compile the server code and generated JAX-WS artifacts

* wsimport ant task is run to compile the generated WSDL file
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService

* To run
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.


We appreciate your feedback, please send it to users@jax-ws.dev.java.net.