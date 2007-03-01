fromjava-saop12 sample demonstrates the Java->WSDL programming model using SOAP 1.2.

* The service implementation class has one method:
    * addNumbers() that takes two integers and returns an integer and
      it throws the AddNumbersException as a checked exception
* etc - configuration files
    * custom-client.xml client customization file
    * custom-schema.xml client customization file for schema objects
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file, you will notice that in this case the extension option is
      "true", this is because we use a non-standard SOAP 1.2 BindingID
      as explained below.  Setting extension to "true" allows us to use
      non-standard bindings.
    * sun-jaxws.xml deployment descriptor for web container,
      this particular DD uses the binding element to specify SOAP 1.2,
      Please read about the @BindingType annotation below to see why
      we don't use the standard SOAP 1.2 BindingID. 
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation, also contains to following
      annotation to specify SOAP 1.2
	@BindingType(value="http://java.sun.com/xml/ns/jaxws/2003/05/soap/bindings/HTTP/")
      You will notice that this is not the standard BindingID for SOAP 1.2.  That is 
      because there is no standard WSDL 1.1 binding for SOAP 1.2.  To rememdy this,
      the JAX-WS RI created it's own proprietary SOAP 1.2 binding that can be used
      with the RI.  The RI will generate WSDL 1.1 soap bindings but will use the
      standard SOAP 1.2 namespace instead.
    * server/AddNumberException.java - server implementation

* apt ant task is run to compile AddNumbersImpl and create server
  objects used during deployment and runtime.

* To specify use of SOAP 1.2, you can either specify it in the deployment descriptor
  or in the @BindingType annotation.  Since we are using a proprietary BindingID
  we turn on extension mode in build.properties so that wsimport/wsgen will not
  fail.

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
