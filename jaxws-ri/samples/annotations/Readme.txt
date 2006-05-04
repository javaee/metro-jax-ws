This sample will build, deploy an invoke a simple Web service.

This sample demonstrates the use of annotations to customize the parameter 
name, operation name, targetNamespace, and other similar features when 
developing a Web service when starting from Java.  The annotations
are contained in the src/annotations/server/AddNumbersImpl.java and
src/annotations/server/AddNumbersIF.java.  The src/annotations/server/
AddNumbersImpl.java containes one @WebService annotation with an 
endpointInterface element pointing to the annotations.server.AddNumbersIF
class.

The annotations.server.AddNumbersIF class uses the following annotations:
The @WebService(targetNamespace = "http://duke.org", name="AddNumbers") -
annotation sets the targetNamespace for the WSDL that will contain the
wsdl:portType for this endpoint.  the "name" element specifies the name
of the wsdl:portType element to be 'AddNumbers'.

The @SOAPBinding(style=SOAPBinding.Style.RPC, use=SOAPBinding.Use.LITERAL)
annotatiion specifies that the endpoint should be be rpc/literal.

The @WebMethod(operationName="add", action="urn:addNumbers") annotation
customizes the wsdl:operationName to be 'add' and the soap:operation's 
soapAction attribute to be 'urn:addNumbers'.

The @WebResult(name="return") specifies that the localpart of the return
element should be 'return'.

The @WebParam(name="num1")int number1, 
    @WebParam(name="num2")int number2) annotations changes the wsdl:message
partName for the parameters to 'num1' and 'num2' instead of 'number1' and
'number2'.

* etc - configuration files
    * custom-client.xml client customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java,AddNumbersIF.java,AddNumbersException.java - server implementation

* apt ant task to compile the server code and generated JAX-WS artifacts

* wsimport ant task is run to compile the generated WSDL file
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
