fromwsdl-wsaddressing-policy sample demonstrates the use of WS-Addressing in Web Services starting from wsdl using
WS-Policy. This samples requires the support of WS-Policy 1.5 which was introdueced in JAX-WS 2.2 RI. 

In etc/AddNumbers.wsdl, Notice the use of <wsp:PolicyReference URI="#AddNumbersBinding_WSAddressing_policy"/>
 in the binding section, JAX-WS 2.2 supports the WS-Policy 1.5 Frameworka nd WS-Policy 1.5 Attachment.

The attached policy in the wsdl specifies the requirement for the use of WS-Addressing through the policy defined as
<wsp:Policy wsu:Id="AddNumbersBinding_WSAddressing_policy">
        <wsp:ExactlyOne>
            <wsp:All>
                <wsam:Addressing>
                    <wsp:Policy/>
                </wsam:Addressing>
            </wsp:All>
        </wsp:ExactlyOne>
    </wsp:Policy>

This is the standard way of defining the use of WS-Addressing as defined in Web Services Addressing 1.0 - Metadata.
If you want to just advertise the service's capability of WS-Addressing support, and not manadate its use(may be to work
with for older clients that do not speak WS-Addressing), you can specify it with <wsam:Addressing wsp:Optional="true">.
JAX-WS Runtime processes the requirements of the service and takes care putting WS-Addressing headers in the SOAP
messages as per WSDL definitions automatically .

In the portType definitions of the wsdl,
    * <operation name="addNumbers"> shows usage of addressing without any explicit wsa:Action values
    or explicit message names
    * <operation name="addNumbers2"> shows usage of explicit message names
    * <operation name="addNumbers2"> shows usage of explicit wsam:Action values.
Observe the SOAP request and response messages to see how wsa:Action is derived from these associations.

Also notice how a System property "com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump" is used to
log SOAP messages.

* etc - configuration files
    * AddNumbers.wsdl wsdl file
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

Refer to the Prerequisites defined in samples/docs/index.html.

We appreciate your feedback, please send it to metro@javaee.groups.io.
