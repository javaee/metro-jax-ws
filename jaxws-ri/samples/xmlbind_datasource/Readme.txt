xmlbind_datasource sample demonstrates a REST based webservices using XML/HTTP binding along with Provider/Dispatch. Client application sends an image to the service and service sends back the same image to the client.

* The service implementation 
    * just returns the DataSource back
* etc - configuration files
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * client/Dispatch.java - client application using Dispatch
    * server/AddNumberImpl.java - service implementation

* To run
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client run - compiles the Dispatch based client application
      and executes it

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.
