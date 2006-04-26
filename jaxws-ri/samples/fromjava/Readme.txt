fromjava sample demonstrates the Java->WSDL programming model.

* The service implementation class has one method:
    * addNumbers() that takes two integers and returns an integer and
      it throws the AddNumbersException as a checked exception
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

* apt ant task is run to compile AddNumbersImpl and create server
  objects used during deployment and runtime.

* To run
    * ant clean server - runs apt to generate server side artifacts and
      does the deployment
    * ant clean client run - runs wsimport on the published wsdl by the deplyed
      endpoint, compiles the generated artifacts and the client application
      then executes it.

* Prerequisite

Refer to the Prerequisites defined in samples/docs/index.html. 