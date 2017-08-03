wsimport_clientjar sample demonstrates -clientjar option of wsimport.

This sample is similar to fromwsdl sample except that wsimport on client-side is invoked with clientjar option.
With clientjar option, wsimport fetches wsdl metadata required for web service invocation and packages all the
generated classes and metadata into an easily consumable jar file. Using the web service client from any application
is easier by putting the generated jar in the application classpath.

* It has two operations with different MEPs
    * in/out - addNumbers()
    * oneway - onewayInt()
* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * deploy-targets.xml ant script to deploy the endpoint war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates  AddNumbersServcieClient.jar with contents
      META-INF/wsdl/AddNumbersService.wsdl
      SEI - AddNumbersPortType
      service class - AddNumbersService
      exception class - AddNumbersFault_Exception

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to metro@javaee.groups.io.
