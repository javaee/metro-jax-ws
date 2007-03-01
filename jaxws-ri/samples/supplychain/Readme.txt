supplychain sample demonstrates the Java->WSDL programming model. The service uses beans, exceptions etc

* The service implementation 
    * accesses different beans to implement submitPO method
* etc - configuration files
    * custom-client.xml client customization file
    * custom-schema.xml client customization file for schema objects
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container
* src source files
    * server/PurchaseOrder.java, server/ShipmentNotice.java, server/Item.java
      are beans to represent data
    * server/WarehouseImpl.java - web service implementation
    * server/InvalidPOException.java - a service exception
    * server/WarehouseLightWeight.java - to publish web service using Endpoint API
    * server/EndpointStopper.java - to stop service published using Endpoint API

    * client/RetailerClient.java - client application

* wsgen ant task is run to create server objects used during deployment and runtime.

* To run in servlet container
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - generates server side artifacts and does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* To run as j2se webservice endpoint
    * ant clean server-j2se - generates server side artifacts and does the deployment using Endpoint API
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.
    * ant server-j2se-stop - stops the service

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.