package com.sun.xml.ws.eclipselink;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.test.BasicDatabindingTestBase;
import com.sun.xml.ws.test.CollectionMap;
import com.sun.xml.ws.test.CollectionMapImpl;

/**
 * @author scchen
 */
public class EclipselinkJAXBBasicTest extends BasicDatabindingTestBase  {
	
	protected DatabindingModeFeature databindingMode() {
		return new DatabindingModeFeature(DatabindingModeFeature.ECLIPSELINK_JAXB); 
	}
	
	public void testHelloEcho() throws Exception {
		_testHelloEcho();
	}

	public void testCollectionMap() throws Exception {
		Class<?> endpointClass = CollectionMapImpl.class;
		Class<?> proxySEIClass = CollectionMap.class;
		DatabindingConfig srvConfig = new DatabindingConfig();
		srvConfig.setEndpointClass(endpointClass);
		DatabindingModeFeature dbm = databindingMode();
		WebServiceFeature[] f = { dbm };
		srvConfig.setFeatures(f);	

        DatabindingConfig cliConfig = new DatabindingConfig();
		cliConfig.setContractClass(proxySEIClass);
		cliConfig.setFeatures(f);	
		
		CollectionMap p = createProxy(CollectionMap.class, srvConfig, cliConfig, false);
	    List<String> req = Arrays.asList("x", "Eclipselink", "parameterized", "List");
	    List<String> res = p.echoListOfString(req);
	    assertEqualList(req, res);
	}
}

