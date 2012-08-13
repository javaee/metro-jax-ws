inline_customization sample demonstrates how to customize wsdl components, such
as the package where the wsdl and schema artifacts will be generated, name of
the generated SEI, Service and exception class, methods and parameters. you
will also see the customization to generate asynchronous methods and wrapper
style java method generation.

* etc - configuration files
    * AddNumbers.wsdl wsdl file with inlined customizations
    * custom-client.xml client customization file with package customization
    * custom-server.xml server customization file with package customization
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.

