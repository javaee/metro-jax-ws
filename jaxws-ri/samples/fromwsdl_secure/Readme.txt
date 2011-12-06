fromwsdl sample demonstrates the WSDL->Java programming model using an SSL HTTP
endpoint.

This sample only shows Transport Layer Security with no Mutual Authentication.
In particular, notice the web.xml how SSL is configured for the endpoint.
On the client-side, notice how the "javax.net.ssl.trustStore" and 
"javax.net.ssl.trustStorePassword" jvmargs are passed during wsimport and 
client execution.

For sample of TLS with Mutual auth with Glassfish, see  
http://java.sun.com/developer/EJTechTips/2006/tt0527.html#1
Refer to the guide 
(https://jax-ws.dev.java.net/guide/Configuring_SSL_and_Authorized_Users.html#ahidi) 
for more samples with SSL.

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
    * web.xml with security defined
    * deploy-targets.xml contains atgerts for deployment to Glassfish and 
      Tomcat.
      It also contains targets for setting up tomcat keystore and client 
      truststore.

* src source files
    * client/AddNumbersClient.java - client application
    * server/AddNumberImpl.java - server implementation

* wsimport ant task is run to compile etc/AddNumbers.wsdl
    * generates
      SEI - AddNumbersPortType
      service class - AddNumbersService
      and exception class - AddNumbersFault_Exception


* To run with Glassfish V2 installed in developer profile (default).
-----------------------------------------------------------------------------

1) set JAXWS_HOME to the JAX-WS installation directory.
   Set JAVA_HOME property.
   Set AS_HOME property

2) Change domain.name property in etc/deploy-targets.xml to your hostname.

3) By default https port is configured as 8181 for this sample, which is 
default https port in Glassfish.
   If it is different from 8181, change the property "https.port" in 
etc/deploy-targets.xml

4) This sample uses the default keystore in Glassfish and Glassfish is 
preconfigured with HTTPS. Thre is no extra SSL configuration with Glassfish.

5) do 'ant clean server", this runs wsimport to compile AddNumbers.wsdl and 
generate server side artifacts and does the deployment

6) run "ant client run"
    This creates a client truststore in etc/certs directory  by importing the 
server certificate and runs wsimport on the published wsdl by the deployed 
endpoint, by passing "javax.net.ssl.trustStore" and 
"javax.net.ssl.trustStorePassword" jvmargs. It then compiles the generated 
artifacts and the client application then executes it.


* To run with Tomcat 5.5 and later.
------------------------------------------------------------------------------
1) set JAXWS_HOME to the JAX-WS installation directory.
   Set JAVA_HOME property.
   Set CATALINA_HOME property to your Tomcat installation.
   
2) Change domain.name property in etc/deploy-targets.xml to your hostname.
   By default https port is configured as 8181 for this sample. If you are 
   using tomcat, the default https port is 8443.
   If it is different from 8181, change the property "https.port" in 
   etc/deploy-targets.xml

3) Tomcat does not have any keystore or trustStore by default. You also need to
   enable SSL.

   run "ant -Dtomcat=true setup-certs-tomcat", This creates a keystore 
   "tomcat.keystore" with a self-signed certificate for server and copies in 
   to ${CATALINA_HOME}/certs

4) Configure SSL in Tomcat
  configured with SSL HTTP Connector on port 8443  in 
$CATALINA_HOME/conf/server.xml

<!-- Define a SSL HTTP/1.1 Connector on port 8443 -->
    
   <Connector port="8443" maxHttpHeaderSize="8192"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS" 
               keystoreFile="certs/tomcat.keystore" 
               keystorePass="changeit" />
    
 Note: keystoreFile and keystorePass values

5) Restart Tomcat container.

6) do 'ant -Dtomcat=true clean server", this runs wsimport to compile 
   AddNumbers.wsdl and generate server side artifacts and does the deployment

7) run "ant -Dtomcat=true client run"
    This creates a client truststore in etc/certs directory  by importing the 
    server certificate and runs wsimport on the published wsdl by the deployed 
    endpoint, by passing "javax.net.ssl.trustStore" and
    "javax.net.ssl.trustStorePassword" jvmargs. It then compiles the generated 
    artifacts and the client application then executes it.
