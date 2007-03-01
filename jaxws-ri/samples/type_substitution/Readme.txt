This sample demonstrates type substitution and sending java types that are not directly referenced by the WSDL.

* The service implementation class has two methods:
    * Car getSedans(String carType);
      From the method signature it returns an abstract class "Car", the
      implementation of this method returns a derived class "Toyota".

    * Car tradeIn(Car car);
      This method takes a car and returns abstract class Car. the actual
      implementation returns the derived class.

* etc - configuration files
    * custom-client.xml client customization file
    * custom-schema.xml client customization file for schema objects
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container

* src source files
    * client/CarBuyerApp.java - client application
    * server/Vehicle - Interface
    * server/Car.java - Abstract class
    * server/Toyota - Derived class
    * server/CarDealer - Endpoint implementation class

* apt ant task is run to compile server side Java files and create server
  objects used during deployment and runtime.

* To run in servlet container
    * To run on Glassfish, set AS_HOME to the Glassfish installation
    * To run on tomcat, set CATALINA_HOME to tomcat installation directory
    * set JAXWS_HOME to the JAX-WS installation directory
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment on Glassfish (AS_HOME)
    * ant clean server -Dtomcat=true - runs apt to generate server side
      artifacts and does the deployment on Tomcat (CATALINA_HOME)
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

