optional_webxml sample demonstrates the simplified deployment of web services using JAX-WS RI deployment by not
requiring the configuration of web.xml.

The web service and client code is similar to fromwsdl sample. Notice that there is no file etc/web.xml and web.xml is
not packaged in the deployed war. JAX-WS runtime on deployment parses the sun-jaxws.xml and deploy
s the web service
endpoints. To support this feature, JAX-WS runtime relies on the Servlet 3.0 dynamic registration API and hence this
sample needs a Servlet 3.0 based Web container implementation (like Glassfish V3) for it to work. You can still package
the web.xml to specify security constraints or other configuration for the com.sun.xml.ws.transport.http.servlet.WSServlet
like the other samples use, but configuration of
    <listener>
        <listener-class>com.sun.xml.ws.transport.http.servlet.WSServletContextListener</listener-class>
    </listener>
is not required.


* It has two operations with different MEPs
    * in/out - addNumbers()
    * oneway - onewayInt()
* etc - configuration files
    * AddNumbers.wsdl wsdl file
    * custom-client.xml client customization file
    * custom-server.xml server customization file
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
    * This sample requires a Servlet 3.0 container to run.

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
