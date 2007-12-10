efficient_handler sample demonstrates the use of com.sun.xml.ws.api.handler.MessageHandler.
SOAPHandler provides access to SOAPMessage based on DOM, effecting the performance of your application.
MessageHandler is similar to SOAPHandler except that it provides efficient api to work on the message.
Unlike SOAPHandler, Implementations of the MessageHandlers gets access to com.sun.xml.ws.api.message.Message through com.sun.xml.ws.api.handler.MessageHandlerContext. The api in Message class provide various ways to access/set information in the underlying SOAPMessage without converting the message to DOM. Using MessageHandler instead of SOAPHandler can increase the performance of your application. 

The programming model is same as the standard JAX-WS Handlers. This sample shows the usage of MessageHandler to log the outbound and inbound messages on server and client.
 

* The service class has one method:
    * addNumbers() that takes two integers and returns an integer
* etc - configuration files
    * custom-client.xml client customization file
    * custom-schema.xml client customization file for schema objects
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation
    * server/AddNumberException.java - server implementation
    * common/LoggingHandler.java - handler used to log messages

* apt ant task is run to compile AddNumbersImpl and create server
  objects used during deployment and runtime.

* For the server handler, the AddNumbersImpl class includes a HandlerChain
  annotation that points to a handler configuration file called handlers.xml.
  This file describes a handler chain containing one handler of type
  fromjavahandler.common.LoggingHandler. When the service is deployed,
  this handler is instantiated and added to the service.

* For the client handler, the customization file custom-client.xml
  includes an extra <bindings> element containing a handler chain
  description. The schema for the <handler-chains> element is the same
  for both handler chain files (on the server) and customization files.

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
