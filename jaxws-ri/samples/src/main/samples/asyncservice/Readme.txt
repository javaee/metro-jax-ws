asyncservice sample demonstrates JAX-WS RI specific endpoint to achieve
server side asynchrony. This sample requires Servlet 3.0 API supported container like Glassfish 3.0.

This endpint implementation is similar to that of the asyncprovider sample. Notice the use of
 servlet 3.0 feature in web.xml, The servlet com.sun.xml.ws.transport.http.servlet.WSServlet is declared as async
 supported through <async-supported>true</async-supported>. With this the web service can take advantage of the async
 capabilities by releasing the request thread and sending response when ready. Unlike in the asyncprovider sample, no
 container thread is blocked for the response to finish, there by effectively uses server resources to process more requests.

* etc - configuration files
    * hello_literal.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AsyncClient.java - client application
    * server/AsyncHelloImpl.java - server implementation

* wsimport ant task is run to compile etc/hello_literal.wsdl
    * generates
      SEI - Hello
      service class - Hello_Service

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs wsimport to compile hello_literal.wsdl and
      generate server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deployed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite
    * Requires Servlet 3.0 API supported container.

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to metro@javaee.groups.io.
