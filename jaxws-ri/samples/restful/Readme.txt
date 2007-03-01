restful sample demonstrates the REST based webservices using XML/HTTP binding along with Provider/Dispatch. Client applications use HTTP GET to send numbers to the service and process the returned XML Source(which contains the sum). WSDL
is not published in this case.

* The service implementation 
    * Uses MessageContext.QUERY_STRING, MessageContext.PATH_INFO to access
      numbers. It creates XML Source by computing the sum of those numbers and
      sends it to the client.
* etc - configuration files
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/AddNumbersClient.java - client application using URLConnection
    * client/DispatchAddNumbersClient.java - client application using Dispatch
    * server/AddNumberImpl.java - server implementation

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client run - compiles the Dispatch based client application
      and executes it
    * ant clean client run-url - compiles the JDK's HttpURLConnection based
      client application and executes it

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.