asyncprovider sample demonstrates JAX-WS RI specific endpoint to achieve
server side asynchrony.

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

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
