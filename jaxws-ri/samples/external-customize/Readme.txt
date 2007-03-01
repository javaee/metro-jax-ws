external_customization sample demonstrates how to customize wsdl components, such as the package where the wsdl and schema artifacts will be generated, name of the generated SEI, Service and exception class, methods and parameters. you
will also see the customization to generate asynchronous methods and wrapper
style java method generation.

The endpint impleentation starts from java and a wsdl is generated dynamically. When a client imports the published wsdl the wsdl and schema customization defined in the custom-client.xml and custom-schema.xml is applied.

* etc - configuration files
    * custom-client.xml client wsdl customization file with customizations
    * custom-schema.xml client schema jaxb customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation
    * server/AddNumbersException - exception class

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to compile AddNumbersImpl.java and
      AddNumbersException.java, creates war file and deplys them.

    * ant clean client run - runs wsimport on the published wsdl by the deployed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
