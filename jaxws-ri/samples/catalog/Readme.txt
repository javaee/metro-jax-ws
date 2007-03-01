Sometimes, it is convenient to refer to local resources instead of referring to 
resources over a network. For example, a WSDL may be accessible during client 
generation but not accessible when the client is run. We can address this using 
XML catalogs. JAX-WS runtime uses the XML catalogs for resolving Web Service 
document that is part of the description of a Web service, specifically WSDL 
and XML Schema documents. The catalog is assembled by taking into account all 
accessible resources whose name is META-INF/jax-ws-catalog.xml.

catalog sample demonstrates the use of XML catalog, that can be used by the 
JAX-WS runtime.

Notice that etc/jax-ws-catalog.xml is made available to the classpath, when the
client is run.

* etc - configuration files
    * META_INF/jax-ws-catalog.xml
         The catalog as shown below, makes th JAX-WS runtime to use 
"../AddNumbers.wsdl" when resolving 
"http://localhost:8080/jaxws-catalog/addnumbers?wsdl".
         <catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" 
prefer="system">
             <system 
systemId="http://localhost:8080/jaxws-catalog/addnumbers?wsdl"
                  uri="../AddNumbers.wsdl"/>
         </catalog>
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

Refer to the Prerequisites defined in samples/docs/index.html. For more 
information on XML catalog see - docs/catalog.html.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.