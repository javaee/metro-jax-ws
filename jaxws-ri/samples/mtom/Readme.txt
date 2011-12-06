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
There are 2 operations. The abstract definition of schema element data is
annotated with xmime:expectedCotentTypes to demonstrates the corresponding
mapping of it in the generated SEI on the client side.
    * upload - this operation uploads an Image to the endpoint using MTOM
    * doanload - this operation downloads binary data using MTOM in streaming
      fashion

* etc - configuration files
    * mtomsample.wsdl wsdl file
    * deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container

* src source files
    * client/MtomApp.java - client application
    * server/MtomSampleImpl.java - server implementation

* wsimport ant task is run to compile etc/MtomSample.wsdl
    * generates
      SEI - MtomSample
      service class - MtomService
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
