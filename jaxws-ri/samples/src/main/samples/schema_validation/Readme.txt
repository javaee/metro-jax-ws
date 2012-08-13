schema-validation sample demonstrates RI validation feature

* It has one operation
    * in/out - addNumbers()
* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
      Invokes web service with valid data once. Then invokes web services
      with invalid data
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      - ant clean server -Dtomcat=true - runs wsimport to compile AddNumbers.wsdl and generate server side artifacts and does the deployment to tomcat
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
