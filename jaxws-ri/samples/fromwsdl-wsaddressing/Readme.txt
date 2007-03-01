fromwsdl-wsaddressing sample demonstrates the use of WS-Addressing in Web Services starting from wsdl.
In etc/AddNumbers.wsdl, Notice <wsaw:UsingAddressing wsdl:required="false" /> to specify use of addressing
in wsdl in the binding section. JAX-WS Runtime takes care putting WS-Addressing headers in the SOAP messages
as per WSDL definitions.

In the portType definitions of the wsdl,
    * <operation name="addNumbers"> shows usage of addressing without any explicit wsa:Action values
    or explicit message names
    * <operation name="addNumbers2"> shows usage of explicit message names
    * <operation name="addNumbers2"> shows usage of explicit wsa:Action values.
Observe the SOAP request and response messages to see how wsa:Action is derived from these associations.

Also notice how a System property "com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump" is used to
log SOAP messages.

* etc - configuration files
    * AddNumbers.wsdl wsdl file
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
