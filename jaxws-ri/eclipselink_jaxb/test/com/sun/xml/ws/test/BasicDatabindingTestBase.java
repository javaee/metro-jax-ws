package com.sun.xml.ws.test;

import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.DummyAnnotations;
import com.sun.xml.ws.WsDatabindingTestBase;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.test.HelloImpl;
import com.sun.xml.ws.test.HelloPort;

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
public abstract class BasicDatabindingTestBase extends WsDatabindingTestBase  {
	abstract protected DatabindingModeFeature databindingMode();
	
	protected void _testHelloEcho() throws Exception {
		Class endpointClass = HelloImpl.class;
		Class proxySEIClass = HelloPort.class;
		DatabindingConfig srvConfig = new DatabindingConfig();
		srvConfig.setEndpointClass(endpointClass);
		srvConfig.setMetadataReader(new DummyAnnotations());
		DatabindingModeFeature dbm = databindingMode();
		WebServiceFeature[] f = { dbm };
		srvConfig.setFeatures(f);	

        DatabindingConfig cliConfig = new DatabindingConfig();
		cliConfig.setMetadataReader(new DummyAnnotations());
		cliConfig.setContractClass(proxySEIClass);
		cliConfig.setFeatures(f);	
		
		HelloPort hp = createProxy(HelloPort.class, srvConfig, cliConfig, false);
		String req = "testInVM " + dbm.getMode();
		String res = hp.echoS(req);
		assertEquals(req, res);
	}
}