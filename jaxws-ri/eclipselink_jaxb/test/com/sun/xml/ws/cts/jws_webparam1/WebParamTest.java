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

package com.sun.xml.ws.cts.jws_webparam1;

import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.databinding.DatabindingModeFeature;

import com.sun.xml.ws.WsDatabindingTestBase;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.binding.WebServiceFeatureList;

public class WebParamTest extends WsDatabindingTestBase {
    boolean debug = false;
    
    public void testWebParam1_toplink() throws Exception {
        testWebParam1("eclipselink.jaxb");
    }
    
    //TODO How does jaxb-ri defaultNamespaceRemap work?
    public void TODOtestWebParam1_jaxbri() throws Exception {
        testWebParam1(DatabindingModeFeature.GLASSFISH_JAXB);
    }
    
    void testWebParam1(String dbmode) throws Exception {
        DatabindingConfig srvConfig = new DatabindingConfig();
        srvConfig.setEndpointClass(WebParamWebServiceImpl.class);
        srvConfig.getMappingInfo().setDefaultSchemaNamespaceSuffix("types");
        DatabindingModeFeature dbf = new DatabindingModeFeature(dbmode); 
        WebServiceFeatureList wsfeatures = new WebServiceFeatureList(WebParamWebServiceImpl.class);
        WebServiceFeature[] f = { dbf };
        srvConfig.setFeatures(f);   

        DatabindingConfig cliConfig = new DatabindingConfig();
        cliConfig.setContractClass(WebParamWebService.class);
        cliConfig.setFeatures(f);     
        WebParamWebService port = createProxy(WebParamWebService.class, srvConfig, cliConfig, debug);
        {
            javax.xml.ws.Holder<Employee> employeeHolder = new javax.xml.ws.Holder<Employee>();
            port.helloString4("jsr181", employeeHolder);
            Employee employee = (Employee) employeeHolder.value;
            Name output = employee.getName();
            assertEquals(output.getFirstName(), "jsr181");
            assertEquals(output.getLastName(),  "jaxws");
        }
        {
            javax.xml.ws.Holder<Employee> employeeHolder = new javax.xml.ws.Holder<Employee>();
            Name name = new Name();
            name.setFirstName("jsr181");
            name.setLastName("jsr109");
            port.helloString7("jsr181", name, employeeHolder);
            Employee employee = (Employee) employeeHolder.value;
            Name output = employee.getName();
            assertEquals(output.getFirstName(), "jsr181");
            assertEquals(output.getLastName(),  "jsr109");
        }
    }
}
