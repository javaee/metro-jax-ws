/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client;

import java.net.URL;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.developer.SerializationFeature;

import junit.framework.TestCase;

public class ClientFeatureTest extends TestCase {
    private static final QName SERVICE_NAME = new QName("http://performance.bea.com", "DocService");
    private static final QName PORT_NAME = new QName("http://performance.bea.com", "DocServicePortTypePort");
    private static final String ENDPOINT = "http://localhost:7001/DocService/DocService";
   
    private DocServicePortType createProxy(DocService service, String encoding) throws Exception {
        if (encoding==null) {
            return service.getDocServicePortTypePort();
        } else {
            return service.getDocServicePortTypePort(new SerializationFeature(encoding));
        }
    }
    
    private Dispatch<DataSource> createDispatch(Service service, String encoding) throws Exception {
        if (encoding==null) {
            return service.createDispatch(PORT_NAME, DataSource.class, Mode.MESSAGE);
        } else {
            return service.createDispatch(PORT_NAME, DataSource.class, Mode.MESSAGE, new SerializationFeature(encoding));
        }
    }
    
    public void testSameServiceDifferentDispatch() throws Exception {
        Service service = Service.create(SERVICE_NAME);
        service.addPort(PORT_NAME, HTTPBinding.HTTP_BINDING, ENDPOINT);
        
        String encoding = "UTF-16";
        Dispatch<DataSource> dispatch1 = createDispatch(service, encoding);
        validateFeatureList(dispatch1, encoding);
        
        encoding = null;
        Dispatch<DataSource> dispatch2 = createDispatch(service, encoding);
        validateFeatureList(dispatch2, encoding);
    }
    
    public void testSameServiceDifferentPort() throws Exception {
        URL wsdlUrl = getClass().getResource("service.wsdl");
        DocService service = new DocService(wsdlUrl, SERVICE_NAME);
        
        String encoding = "UTF-16";
        DocServicePortType proxy1 = createProxy(service, encoding);
        validateFeatureList(proxy1, encoding);
        
        encoding = null;
        DocServicePortType proxy2 = createProxy(service, encoding);
        validateFeatureList(proxy2, encoding);
    }
    
    private void validateFeatureList(Object bindingProvider, String expectedEncoding) throws Exception {
        Binding binding = ((BindingProvider)bindingProvider).getBinding();
        WSFeatureList list = (((WSBinding)binding).getFeatures());
        //System.out.println(list);
        SerializationFeature encoding = list.get(SerializationFeature.class);
        if (expectedEncoding==null) {
            assertNull("There should not be a SerializationFeature", encoding);
        } else {
            assertEquals("Mismatched encoding in SerializationFeature", expectedEncoding, encoding.getEncoding());
        }
    }
}
