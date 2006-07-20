fromwsdl sample demonstrates the WSDL->Java programming model
using an SSL HTTP endpoint.

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

* Prerequisites

Note: sample keystore and truststore files are contained in fromwsdl_secure/etc/certs directory.
You can try the sample with these files but it is best to generate your own using the following directions.

-install of JAX-WS on tomcat5.5

* Tomcat5.5
  configured with SSL HTTP Connector on port 8443  in $CATALINA_HOME/conf/server.xml

<!-- Define a SSL HTTP/1.1 Connector on port 8443 -->
    
   <Connector port="8443" maxHttpHeaderSize="8192"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS" 
               keystoreFile=%CATALINA_HOME%/certs/tomcat.keystore" 
               keystorePass="server" />
    
 Note: keystoreFile and keystorePass values

* Create self-signed certificate keystore/truststore for client and server.
cd $JAXWS_HOME/samples/fromwsdl_secure/etc/certs

--Generate a self-signed cert for tomcat server

keytool.exe -genkey -alias self -keyalg RSA -storepass server -keypass server -dname "cn=localhost" -keystore tomcat.keystore

Note: The use of localhost indicates that client and server are running on the same machine. If the server is on a remote machine
      the server fully qualified domain name will be used. 
 
--Now for the client export self-signed key from the server tomcat.keystore to give to the client
as a certificate to import.

keytool.exe -export -rfc -alias self -file tomcat.certificate -keystore tomcat.keystore -storepass server -keypass server

--On the client, import the tomcat.certificate into a client created keystore.

keytool.exe -import -noprompt -trustcacerts -alias self -file tomcat.certificate -keystore client.keystore -storepass client

Note: -keystore option creates the client.keystore file.


The keystore and certificates will be in the certs directory. Copy the certs directory to $CATALINA_HOME.
Note: Make sure the keystoreFile and the keystorePass are defined with the SSL HTTP/1.1 connector.
