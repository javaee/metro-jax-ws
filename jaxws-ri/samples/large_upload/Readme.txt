large-upload sample demonstrates uploading a large file to the server.

* The service implementation class has one method:
    * fileUpload() takes a filename and its content. It creates a file with the content,
      verifies the content and deletes the file.
* etc - configuration files
    * custom-client.xml client customization file
    * custom-schema.xml client customization file for schema objects
    * build.properties, deploy-targets.xml ant script to deploy the endpoint
      war file
    * sun-jaxws.xml deployment descriptor for web container,
* src source files
    * client/UploadClient.java - client application. The proxy is created using
      MTOM feature. Uses HTTP chunking mode for streaming the file content.
    * server/UploadImpl.java - server implementation
      It uses RI's StreamingDataHandler to move the content to a file. Uses MTOM
      feature so that the file content will be represented as an attachment on the wire.

* apt ant task is run to compile UploadImpl and create server
  objects used during deployment and runtime.

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
