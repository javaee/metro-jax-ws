/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.jaxbri;

import javax.xml.ws.WebServiceFeature;

import com.oracle.webservices.api.databinding.DatabindingModeFeature;

import com.sun.xml.ws.DummyAnnotations;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.test.BasicDatabindingTestBase;
import com.sun.xml.ws.test.HelloImpl;
import com.sun.xml.ws.test.HelloPort;

/**
 * JAXBRIBasicTest
 * 
 * @author shih-chang.chen@oracle.com
 */
public class JAXBRIBasicTest extends BasicDatabindingTestBase  {
	
	protected DatabindingModeFeature databindingMode() {
		return new DatabindingModeFeature(DatabindingModeFeature.GLASSFISH_JAXB); 
	}
	
	public void testHelloEcho() throws Exception {
	    String wrapperName = _testHelloEcho();
        assertTrue(wrapperName != null && wrapperName.endsWith("JAXBRIContextWrapper"));
	}
	
	public void testHelloEchoNoMode() throws Exception {
        Class endpointClass = HelloImpl.class;
        Class proxySEIClass = HelloPort.class;
        DatabindingConfig srvConfig = new DatabindingConfig();
        srvConfig.setEndpointClass(endpointClass);
        srvConfig.setMetadataReader(new DummyAnnotations());
        WebServiceFeature[] f = {  };
        srvConfig.setFeatures(f);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setMetadataReader(new DummyAnnotations());
        cliConfig.setContractClass(proxySEIClass);
        cliConfig.setFeatures(f);

        HelloPort hp = createProxy(HelloPort.class, srvConfig, cliConfig, false);
        String req = "testInVM " + databindingMode().getMode();
        String res = hp.echoS(req);
        assertEquals(req, res);
        String wrapperName = srvConfig.properties().get(
                BindingContext.class.getName()).getClass().getName();
        assertTrue(wrapperName != null && wrapperName.endsWith("JAXBRIContextWrapper"));
    }
	
	public void testHelloEchoInvalidDB() throws Exception {
        Class endpointClass = HelloImpl.class;
        Class proxySEIClass = HelloPort.class;
        DatabindingConfig srvConfig = new DatabindingConfig();
        srvConfig.setEndpointClass(endpointClass);
        srvConfig.setMetadataReader(new DummyAnnotations());
        WebServiceFeature[] f = { new DatabindingModeFeature("invalid.db") };
        srvConfig.setFeatures(f);

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setMetadataReader(new DummyAnnotations());
        cliConfig.setContractClass(proxySEIClass);
        cliConfig.setFeatures(f);
        
        try {
            HelloPort hp = createProxy(HelloPort.class, srvConfig, cliConfig, false);
            fail("Expected DatabindingException not thrown");
        } catch (DatabindingException e) {
            // expected exception.
        }
    }
}

