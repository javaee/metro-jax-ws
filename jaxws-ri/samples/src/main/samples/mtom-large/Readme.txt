mtom sample demonstrates the use of MTOM/XOP for effciently serializing binary 
XML content.

Normally, data of xml type xs:base64Binary or xs:hexBinary is inlined in the 
SOAP message. With MTOM/XOP encoding, such data is encoded and transmitted as 
optimized packages(sending the encoded binary data as attachments and still 
making it part of XML infoset by referencing them in the message). This 
encoding (if the binary data is inlined or sent as attachments) is transparent 
to the user.

Enabling MTOM in JAX-WS:
By default MTOM encoding is disabled. One can enable MTOM on client-side by
passing javax.xml.ws.MTOMFeature while creating the proxy.
See javax.xml.ws.Service#getPort(Class<T> serviceEndpointInterface,WebServiceFeature... features)
for more information on enabling/disabling Web Service features.
Similarly, one can pass MTOMFeature while creating Dispatch client to enable MTOM.

One the server-side, MTOM is enabled by using @javax.xml.ws.soap.MTOM on
the endpoint implementation class.

Mapping XML binary data to Java types:
Normally XML binary types are mapped to Java byte[]. But using 
xmime:expectedContentTypes attribute, one can specify the content type 
associated with the binary data, that can be used for static type mapping by 
the data binding framework(JAXB in this case).


* It has the follwoing operations
    * detail - shows mapping of xs:base64Binary with  
      xmime:expectedContentTypes="image/jpeg" to Java type java.awt.image
    * echoData - shows how xs:base64Binary is encoded using MTOM/XOP encoding 
      instead of inlining in the SOAP message.

* etc - configuration files
    * hello.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container

* src source files
    * client/MtomApp.java - client application
    * client/AttachmentHelper - utility class for handling attachments.
    * server/HelloImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - Hello
      service class - HelloService
      and other classes representing the Java beans for the schema constructs 
      used for request/response.

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile hello.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deployed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
