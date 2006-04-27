mime sample demonstrates the WSDL Mime binding for specifying attachments in the wsdl description and its mapping to Java types.One can use the following to describe attachments in a wsdl.
    mime:content a binding construct to indicate the message part is bound to an attachment  or
    wsiap:swaRef a schema construct defined by WS-I Attachment Profile to indicate a reference to an attachment in the message
JAXB defines mapping of MIME types and Java types, and wsiap:swaRef. JAX-WS defines mapping of mime:conent to Java types.

One can use customization to enable or disable default mime:content mapping rules by using jaxws:enableMIMEContent binding declaration.Notice that, custom-client.xml and custom-server.xml use this customization to enable/disable mime:content mapping.This sample also shows how one can use wsiap:swaRef to refer to an attachment in the message.

* It has the follwoing operations
    * echoData - enableMIMEContent is false
    * echoDataWithEnableMIMEContent - enableMIMEContent is false
    * detail - Mapping of XML schema construct to javax.xml.transform.Source
    * claimForm - wsiap:swaRef example
* etc - configuration files
    * hello.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/MimeApp.java - client application
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