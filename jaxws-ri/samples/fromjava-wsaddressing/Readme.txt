fromjava-wsaddressing sample demonstrates the use of WS-Addressing in Web Services starting from Java.
Notice in AddNumbersImpl,
 @javax.xml.ws.soap.Addressing is used to enable addressing. With this annotation the generated WSDL for this service contains
 <wsaw:UsingAddressing/> in the binding section. At the minimal, this should be specified on an endpoint
 implementation to enable addressing starting from Java.

 Also annotations @Action and @FaultAction are used to specify explicit wsa:Action values and notice how
 these are reflected the in the portType definitions of the generated wsdl. Without any explicit action
 values, default values are used as per WS-Addressing specification. Observe the SOAP request and response
 messages to see how wsa:Action is derived from these associations.

Also notice how a System property "com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump" is used to
log SOAP messages.

* The service implementation class has one method:
    * addNumbers() that takes two integers and returns an integer and
      it throws the AddNumbersException as a checked exception
* etc - configuration files
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation
    * server/AddNumberException.java - server implementation

* apt ant task is run to compile AddNumbersImpl and create server
  objects used during deployment and runtime.

* To run in servlet container
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.

