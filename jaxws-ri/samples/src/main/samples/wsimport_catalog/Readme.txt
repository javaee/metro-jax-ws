wsimport has the "-catalog" option which turns on the use of catalog-based
entity resolver. This will allow you to essentially "redirect" references
to your local copies of resources without touching the schema files.

This applies to the WSDL and the schemas that the wsdl imports.

This sample provides wsdlcatalog.xml which provides mapping of wsdl URL to the 
local wsdl file. the wsimport ant task defined in the build.xml file uses ant 
built in type <xmlcatalog> to define entity references for the imported schema 
by the wsdl. Alternatively you can specify the schema entity reference in the 
wsdlcatalog.xml.

To run this sample, just type ant, the default target 'runclient' runs wsimport 
- it generates the artifacts in the build directory of this sample and compiles 
and then it runs WsimportCatalogTester.

We appreciate your feedback, please send it to users@jax-ws.dev.java.net.