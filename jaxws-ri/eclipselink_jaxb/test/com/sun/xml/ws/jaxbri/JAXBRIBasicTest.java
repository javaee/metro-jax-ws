package com.sun.xml.ws.jaxbri;

import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.test.BasicDatabindingTestBase;

/**
 * @author scchen
 */
public class JAXBRIBasicTest extends BasicDatabindingTestBase  {
	
	protected DatabindingModeFeature databindingMode() {
		return new DatabindingModeFeature(DatabindingModeFeature.GLASSFISH_JAXB); 
	}
	
	public void testHelloEcho() throws Exception {
		_testHelloEcho();
	}
}

