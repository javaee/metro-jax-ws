mtom-soap12 sample demonstrates the use of MTOM/XOP for effciently serializing certain types of XML content.
This sample is similar to mtom sample, except that this sample uses SOAP 1.2 binding.

Normally, data of xml type xs:base64Binary or xs:hexBinary is inlined in the SOAP message. With MTOM/XOP encoding, such data is encoded and transmitted as optimized packages(sending the encoded binary data as attachments and stil making it part of XML Infoset by referencing them in the message). This encoding (if the binary data is inlined or sent as attachments) is transparent to the user.

Enabling MTOM in JAX-WS:
By default MTOM encoding is disabled. One can enable MTOM on client-side by setting setMTOMEnabled (true) on the binding.
One the server-side, MTOM is enabled by setting enable-mtom=true in the deployment descriptor (sun-jaxws.xml) or using MTOM binding identifiers defined by JAX-WS specification.
    For SOAP 1.1, use "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true" to enable MTOM or specify enable-mtom=true attribute in sun-jaxws.xml
    For SOAP 1.2, use "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true" to enable MTOM or specify enable-mtom=true attribute in sun-jaxws.xml
Observe the sun-jaxws.xml in this sample for usage.

Mapping XML binary data to Java types:
Normally XML binary types are mapped to Java byte[]. But using xmime:expectedContentTypes attribute, one can specify the content type associated with the binary data, that can be used for static type mapping by the data binding framework(JAXB in this case).


* It has the follwoing operations
    * detail - shows mapping of xs:base64Binary with xmime:expectedContentTypes="image/jpeg" to Java type java.awt.image
    * claimForm - wsiap:swaRef example (refer to mime sample docs for more wsiap:swaRef)
    * echoData - shows how xs:base64Binary is encoded using MTOM/XOP encoding instead of inlining in the SOAP message.
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
      and other classes representing the Java beans for the schema constructs used for request/response.

* To run
    * ant clean server - runs wsimport to compile hello.wsdl and generate
      server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deployed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 
