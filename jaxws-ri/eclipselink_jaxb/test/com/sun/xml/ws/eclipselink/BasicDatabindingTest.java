package com.sun.xml.ws.eclipselink;

import javax.xml.ws.WebServiceFeature;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingModeFeature;

/**
 * 1. @WebService
 * 2. Default BindingID = BindingID.parse(endpointClass)
 * 3. Default from WebServiceFeatureList
 * 4. Default WSDLGeneratorExtension
 * 5. setInlineSchemas(true) -> WSDLGenerator line1025 result = new TXWResult
 * - EntityResolverWrapper InputSource for in-vm wsdl
 * @author scchen
 *
 */
public class BasicDatabindingTest extends WsDatabindingTestBase  {
	public void testHelloEcho() throws Exception {
		Class endpointClass = HelloImpl.class;
		Class proxySEIClass = HelloPort.class;
		DatabindingConfig srvConfig = new DatabindingConfig();
		srvConfig.setEndpointClass(endpointClass);
		srvConfig.setMetadataReader(new DummyAnnotations());
		DatabindingModeFeature dbf = new DatabindingModeFeature(DatabindingModeFeature.ECLIPSELINK_JAXB); 
		WebServiceFeature[] f = { dbf };
		srvConfig.setFeatures(f);	

        DatabindingConfig cliConfig = new DatabindingConfig();
		cliConfig.setMetadataReader(new DummyAnnotations());
		cliConfig.setContractClass(proxySEIClass);
		cliConfig.setFeatures(f);	
		
		HelloPort hp = createProxy(HelloPort.class, srvConfig, cliConfig, false);
		String req = "testInVM ECLIPSELINK_JAXB";
		String res = hp.echoS(req);
		assertEquals(req, res);
	}
}
