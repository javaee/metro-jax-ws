dual_binding sample demonstrates the SOAP/HTTP and XML/HTTP for the same
service class

* The service implementation class has one method:
    * addNumbers() that takes two integers and returns an integer
* etc - configuration files
    * custom-client.xml client customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* apt ant task is run to compile AddNumbersImpl and create server
  objects used during deployment and runtime.

* To run in servlet container
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts
      then executes it.
    * ant run - runs the client application
      ant run -Dlog=true shows what is going on the wire

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.


