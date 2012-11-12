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

package com.sun.xml.ws.test;

import javax.xml.ws.WebServiceFeature;

import com.oracle.webservices.api.databinding.DatabindingModeFeature;

import com.sun.xml.ws.DummyAnnotations;
import com.sun.xml.ws.WsDatabindingTestBase;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.test.HelloImpl;
import com.sun.xml.ws.test.HelloPort;

/**
 * 1. @WebService
 * 2. Default BindingID = BindingID.parse(endpointClass)
 * 3. Default from WebServiceFeatureList
 * 4. Default WSDLGeneratorExtension
 * 5. setInlineSchemas(true) -> WSDLGenerator line1025 result = new TXWResult
 * - EntityResolverWrapper InputSource for in-vm wsdl
 * 
 *   @author shih-chang.chen@oracle.com
 */
public abstract class BasicDatabindingTestBase extends WsDatabindingTestBase  {
	abstract protected DatabindingModeFeature databindingMode();
	
	protected String _testHelloEcho() throws Exception {
		Class<?> endpointClass = HelloImpl.class;
		Class<?> proxySEIClass = HelloPort.class;
		DatabindingConfig srvConfig = new DatabindingConfig();
		srvConfig.setEndpointClass(endpointClass);
		srvConfig.setMetadataReader(new DummyAnnotations());
		DatabindingModeFeature dbm = databindingMode();

        DatabindingConfig cliConfig = new DatabindingConfig();
		cliConfig.setMetadataReader(new DummyAnnotations());
		cliConfig.setContractClass(proxySEIClass);
		
		// Honor system property if present, otherwise set feature.
		WebServiceFeature[] f = null;
        String dbProperty = System
                .getProperty(BindingContextFactory.JAXB_CONTEXT_FACTORY_PROPERTY);
        if (dbProperty == null)
            f = new WebServiceFeature[] { dbm };
        else
            f = new WebServiceFeature[0];
        srvConfig.setFeatures(f);
        cliConfig.setFeatures(f);
		
		HelloPort hp = createProxy(HelloPort.class, srvConfig, cliConfig, false);
		String req = "testInVM " + dbm.getMode();
		String res = hp.echoS(req);
		assertEquals(req, res);
        return srvConfig.properties().get(BindingContext.class.getName())
                .getClass().getName();
	}
    
    public void testWebParamHolder() throws Exception {
        DatabindingModeFeature dbm = databindingMode();
        WebServiceFeature[] f = { dbm };
        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setEndpointClass(WebParamHolderSEB.class);
        cliConfig.setFeatures(f);  
        Databinding db = (Databinding) factory.createRuntime(cliConfig);     
        assertNotNull(db);
    }
}
