/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.xml.ws.eclipselink;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.databinding.DatabindingModeFeature;

import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.test.BasicDatabindingTestBase;
import com.sun.xml.ws.test.CollectionMap;
import com.sun.xml.ws.test.CollectionMapImpl;

/**
 * EclipselinkJAXBBasicTest
 * 
 * @author shih-chang.chen@oracle.com
 */
public class EclipselinkJAXBBasicTest extends BasicDatabindingTestBase  {
	
	protected DatabindingModeFeature databindingMode() {
		return new DatabindingModeFeature(DatabindingModeFeature.ECLIPSELINK_JAXB); 
	}
	
	public void testHelloEcho() throws Exception {
	    String wrapperName = _testHelloEcho();
        assertTrue(wrapperName != null && wrapperName.endsWith("JAXBContextWrapper"));
	}
	
	public void testHelloEchoWithProperty() throws Exception {
	    String propName = BindingContextFactory.JAXB_CONTEXT_FACTORY_PROPERTY;
	    String oldProp = System.getProperty(propName);
	    try {
	        System.setProperty(propName, DatabindingModeFeature.ECLIPSELINK_JAXB);
	        String wrapperName = _testHelloEcho();
	        assertTrue(wrapperName != null && wrapperName.endsWith("JAXBContextWrapper"));
	    } finally {
	        if (oldProp != null)
	            System.setProperty(propName, oldProp);
	        else
	            System.clearProperty(propName);
	    }
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
		{
    	    List<String> req = Arrays.asList("x", "Eclipselink", "parameterized", "List");
    	    List<String> res = p.echoListOfString(req);
    	    assertEqualList(req, res);
		}
		{
    	    Integer[] num = {123, -456, 789, 0};
    	    Map<String, Integer> req = new HashMap<String, Integer>();
    	    for (Integer i : num) req.put(i.toString(), i);
            Map<Integer, String> res = p.echoMapOfString(req);
            Map<Integer, String> ans = new HashMap<Integer, String>();
            for (Integer i : num) ans.put(i, i.toString());
            assertTrue(equalsMap(ans, res));
		}
	}
}

