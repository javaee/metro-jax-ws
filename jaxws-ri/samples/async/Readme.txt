This sample will build, deploy an invoke a simple Web service.

This sample demonstrates the use of synchronous, asynchronous poll and
asynchronous callback invocations when running a Web service client. The Web
service has been developed from Java.

This sample will build, deploy and invoke a simple Web service.
* etc - configuration files
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
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deployed
      endpoint, compiles the generated artifacts and the client application
      then executes it.


We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
