/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.client;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.SOAPBindingImpl;
import com.sun.xml.ws.binding.WebServiceFeatureList;
import com.sun.xml.ws.client.seiportinfo.Hello;
import com.sun.xml.ws.model.SOAPSEIModel;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import java.net.MalformedURLException;
import java.net.URL;

public class SEIPortInfoTest extends TestCase {

    static final URL WSDL_URL = SEIPortInfoTest.class.getResource("hello_literal.wsdl");

    static final QName SERVICE_NAME = new QName("urn:test", "Hello");
    static final QName PORT_NAME = new QName("urn:test", "HelloPort");
    static final QName EXTRA_HEADER = new QName("urn:test:types", "Extra");

    static final Class PORT_INTERFACE = Hello.class;

    public void testCreateBindingWSFList() throws MalformedURLException {
        SEIPortInfo seiPortInfo = createSEIPortInfo();
        BindingImpl b = seiPortInfo.createBinding(new WebServiceFeatureList(), PORT_INTERFACE);

        boolean understands = ((SOAPBindingImpl) b).understandsHeader(EXTRA_HEADER);
        assertTrue("header " + EXTRA_HEADER + " must be understood", understands);
    }

    public void testCreateBindingWSFArray() throws MalformedURLException {
        SEIPortInfo seiPortInfo = createSEIPortInfo();
        BindingImpl b = seiPortInfo.createBinding(new WebServiceFeature[]{}, PORT_INTERFACE);

        boolean understands = ((SOAPBindingImpl) b).understandsHeader(EXTRA_HEADER);
        assertTrue("header " + EXTRA_HEADER + " must be understood", understands);
    }

    private SEIPortInfo createSEIPortInfo() throws MalformedURLException {
        WSServiceDelegate delegate = (WSServiceDelegate) WSService.create(WSDL_URL, SERVICE_NAME);

        WSDLPort wsdlPort = delegate.getPortModel(delegate.getWsdlService(), PORT_NAME);
        SEIModel model = delegate.buildRuntimeModel(delegate.getServiceName(), PORT_NAME, PORT_INTERFACE, wsdlPort, new WebServiceFeatureList());

        return new SEIPortInfo(delegate, PORT_INTERFACE, (SOAPSEIModel) model, wsdlPort);
    }

}

