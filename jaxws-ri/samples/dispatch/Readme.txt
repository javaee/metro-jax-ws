This sample demonstrates the use of Dispatch and the types of
invocations that a JAXB Dispatch client, a SOPAMessage Dispatch 
client and a Source Dispatch client can invoke. The service is 
generated from processing the wsdl file AddNumbers.wsdl.
The JAXB Dispatch client uses the JAXB Elements that are generated 
for the client. A more typical usage of a Dispatch client is that 
whose invocation parameter is a Source.

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
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile AddNumbers.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.


* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.