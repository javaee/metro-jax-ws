/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.handlers.client;

import java.io.File;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import junit.framework.TestCase;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.ws.handler.PortInfoImpl;
import com.sun.xml.ws.api.BindingID;

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
public class HandlerChainTester extends TestCase{
    TestService_Service service;
    private final static URL WSDL_LOCATION;
    private final static QName TESTSERVICE = new QName("urn:test", "TestService");
    private final static QName TESTSERVICEPORT = new QName("urn:test", "TestServicePort");
    private final static QName REPORTSERVICEPORT = new QName("urn:test", "ReportServicePort");
    private final QName UNKNOWNPORT = new QName("urn:test", "UnknownPort");
    String endpointAddress = "http://fakeaddress.com/boo";
    static {
        URL url = null;
        try {
            File f = new File("src/whitebox/handlers/config/service.wsdl");
            url = f.toURL();
        } catch(Exception e){
            e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }
    public HandlerChainTester(String s) {
        super(s);
    }

    public TestService_Service getService() {
        if(service == null)
            service = new TestService_Service();
        return service;
    }

    public void testHandlersOnTestPort(){
        TestService_Service service = getService();
        TestService testStub = service.getTestServicePort();
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(3,chain.size());
    }

    public void testHandlersOnTestPort1(){
        Service service = Service.create(WSDL_LOCATION,TESTSERVICE);
        TestService testStub = service.getPort(TestService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(0,chain.size());
    }

    public void testHandlersOnTestPort2(){
        TestService_Service service = getService();
        TestService testStub = service.getPort(TestService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(3,chain.size());
    }

    public void testHandlersOnTestPort3(){
        Service service = Service.create(WSDL_LOCATION, TESTSERVICE);
        TestService testStub = service.getPort(TESTSERVICEPORT, TestService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(0,chain.size());
    }

    public void testHandlersOnReportPort(){
        TestService_Service service = getService();
        ReportService testStub = service.getReportServicePort();
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(2,chain.size());
    }

    public void testHandlersOnReportPort1(){
        Service service = Service.create(WSDL_LOCATION, TESTSERVICE);
        ReportService testStub = service.getPort(ReportService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(0,chain.size());
    }

    public void testHandlersOnReportPort2(){
        TestService_Service service = getService();
        ReportService testStub = service.getPort(ReportService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(2,chain.size());
    }

    public void testHandlersOnReportPort3(){
        Service service = Service.create(WSDL_LOCATION, TESTSERVICE);
        ReportService testStub = service.getPort(REPORTSERVICEPORT, ReportService.class);
        Binding testBinding = ((BindingProvider) testStub).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(0,chain.size());
    }
    public void testHandlersOnTestPortDispatch(){
        TestService_Service service = getService();
        Dispatch<Source> dispatch = service.createDispatch(TESTSERVICEPORT, Source.class,Service.Mode.PAYLOAD);
        Binding testBinding = ((BindingProvider) dispatch).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(3,chain.size());
    }

    public void testHandlersOnTestPortDispatch1(){
        Service service = Service.create(TESTSERVICE);
        String bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        service.addPort(TESTSERVICEPORT, bindingId, endpointAddress);
        Dispatch<Source> dispatch = service.createDispatch(TESTSERVICEPORT, Source.class,Service.Mode.PAYLOAD);
        Binding testBinding = ((BindingProvider) dispatch).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(0,chain.size());
    }
    public void testHandlersOnReportPortDispatch(){
        TestService_Service service = getService();
        Dispatch<Source> dispatch = service.createDispatch(REPORTSERVICEPORT, Source.class,Service.Mode.PAYLOAD);
        Binding testBinding = ((BindingProvider) dispatch).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(2,chain.size());
    }
    /*
    Commenting this test as this no longer is useful.
    In 2.1, When we try to add a invalid Port to a service with wsdl, now we throw WebServiceException,
    during addPort() itself saying Port name is not a valid port in the wsdl.

    public void testHandlersOnUnknownPortDispatch(){
        TestService_Service service = getService();
        String bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        service.addPort(UNKNOWNPORT, bindingId, endpointAddress);
        Dispatch<Source> dispatch = service.createDispatch(UNKNOWNPORT, Source.class,Service.Mode.PAYLOAD);
        Binding testBinding = ((BindingProvider) dispatch).getBinding();
        List<Handler> chain = testBinding.getHandlerChain();
        //System.out.println(chain.size());
        assertEquals(2,chain.size());
    }
    */
    public void testPortInfoImpl() {
        PortInfo portInfo = new PortInfoImpl(BindingID.SOAP11_HTTP, new QName("http://example.com/", "EchoPort"),
new QName("http://example.com/", "EchoService"));
        assertTrue(portInfo.equals(portInfo));
    }
}
