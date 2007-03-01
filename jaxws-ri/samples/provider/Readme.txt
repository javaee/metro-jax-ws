provider sample demonstrates the endpoints based on Provider API 

* The service implementation class has two methods:
    * invoke() gets SOAPMessage payload as Source argument. Source is used
      to find the numbers and a new Source is created with the sum of those
      two numbers.
    * sendSource() is used to create the new Source with the sum.
* etc - configuration files
    * AddNumbers.wsdl wsdl for this endpoint
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - compiles server classes does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
