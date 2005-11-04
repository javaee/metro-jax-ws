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





2.0 Prerequisites

Here is the list of prerequisites that needs to be met before the 
sample can be invoked:

   1. Download Java 2 Standard Edition 5.0 Update 2 or later (J2SE 5.0) 
      from java.sun.com/j2se/1.5.0/download.jsp.  Set JAVA_HOME to the 
      J2SE 5.0 installation directory.
   2. Download Sun Java System Application Server Platform Edition 8.1 
      2005 Q1 UR2 (SJSAS PE 8.1 or Application Server) or later from 
      java.sun.com/j2ee/1.4/download.html. SJSAS PE 8.1 download page 
      has J2EE 1.4 SDK 2005Q1 UR2 available as All-In-One bundle and 
      SJSJAS PE 8.1 available as separate bundle. Make sure that you 
      download SJSAS PE 8.1 separate bundle only and use J2SE 5.0 as 
      the J2SE platform for it's installation. Although the download 
      page requires J2SE 1.4.2 SDK for SJSAS PE 8.1, J2SE 5.0 is required 
      for JAX-WS 2.0 binaries and must be used as the J2SE platform for 
      SJSAS PE 8.1 for the samples to work. Set AS_HOME to point to the 
      Application Server installation directory.
   3. Download Ant 1.6.2 or later from ant.apache.org and install it, 
      lets say this is installed in ANT_HOME. Please note that 
      AS_HOME/bin/asant cannot be used for building and invoking the 
      samples since JAX-RPC 1.1 implementation is baked into this version 
      of ant script. 
   4. Make sure that the Application Server is not running. Invoke 
      ANT_HOME/bin/ant install from the root directory of the JAX-WS 2.0 EA2 
      bundle. This will install JAX-WS 2.0 bundle on the Application Server 
      referred by AS_HOME environment variable.

3.0 Invoking the sample

The sample can be built, deployed and invoked using the ANT_HOME/bin/ant and 
build.xml ant script in the root directory of the sample. Each ant script 
supports the following set of targets:
server 	Builds and deploy the service endpoint WAR
client 	Builds the client
run 	Runs the client

It is essential for the service endpoint to be deployed on Application Server 
before clients can be built because clients use the WSDL exposed from the 
service endpoint deployed in the Application Server. So please make sure that 
your Application Server is either running before the server target is invoked 
or run it after the server target is invoked. You will have to wait a few 
minutes for the Application Server to deploy the service endpoint correctly 
before building the client.

 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.