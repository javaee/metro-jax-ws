/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2013 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.handler.client;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;
import static javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;

import junit.framework.*;

import fromwsdl.handler.common.TestConstants;

import testutil.ClientServerTestUtil;

public abstract class TestCaseBase extends TestCase implements TestConstants {

    // check that the lists have the right number of handlers
    static final int SERVICE_HANDLERS = 2;
    static final int TEST_PORT_HANDLERS = 2;
    static final int REPORT_PORT_HANDLERS = 2;
    static final int PROTOCOL_HANDLERS = 2;
    static final int SERVER_SERVICE_HANDLERS = 2;
    static final int SERVER_TEST_PORT_HANDLERS = 2;
    static final int SERVER_REPORT_PORT_HANDLERS = 1;

    // test *may* want to change these
    int numTestHandlers;
    int numTotalHandlers;
    int numTestServerHandlers;
    int numTotalServerHandlers;
    
    // Dispatch creation use
    static final QName serviceQName = new QName("urn:test", "TestService");
    static final QName testPortQName = new QName("urn:test", "TestServicePort");
    static final QName reportPortQName =
        new QName("urn:test", "ReportServicePort");
    static final String endpointAddress;

    static final String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;
    
    static String NEXT_1_1;
    static String NEXT_1_2;
    static String NONE;
    static String ULTIMATE_RECEIVER;
    
    static {
        // we'll fix the test harness correctly later,
        // so that test code won't have to hard code any endpoint address nor transport,
        // but for now let's just support local and HTTP to make unit tests happier.
        // this is not a good code, but it's just a bandaid solutino that works for now.
        if(ClientServerTestUtil.useLocal())
            endpointAddress = "local://"+System.getProperty("tempdir");
        else
            endpointAddress = "http://localhost:8080/jaxrpc-fromwsdl_handler/test";
    }

    public TestCaseBase(String name) {
        super(name);
        
        numTestHandlers = SERVICE_HANDLERS + TEST_PORT_HANDLERS +
            PROTOCOL_HANDLERS;
        numTotalHandlers = numTestHandlers + REPORT_PORT_HANDLERS;
        numTestServerHandlers = SERVER_SERVICE_HANDLERS +
            SERVER_TEST_PORT_HANDLERS;
        numTotalServerHandlers = numTestServerHandlers +
            SERVER_REPORT_PORT_HANDLERS;
        NEXT_1_1 = "http://schemas.xmlsoap.org/soap/actor/next";
        NEXT_1_2 = "http://www.w3.org/2003/05/soap-envelope/role/next";
        NONE = "http://www.w3.org/2003/05/soap-envelope/role/none";
        ULTIMATE_RECEIVER =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";
    }

    TestService_Service getService() {
        TestService_Service service = new TestService_Service();
        return service;
    }

    TestService getTestStub(TestService_Service service) throws Exception {
        TestService stub = service.getTestServicePort();
        ClientServerTestUtil.setTransport(stub);
        return stub;
    }

    ReportService getReportStub(TestService_Service service) throws Exception {
        ReportService stub = service.getReportServicePort();
        ClientServerTestUtil.setTransport(stub);
        return stub;
    }

    // create service with just qname -- no handlers in that case
    Dispatch<Object> getDispatchJAXB(QName name) throws Exception {
        QName serviceQName = new QName("urn:test", "Hello");
        Service service = Service.create(serviceQName);
        service.addPort(name, bindingIdString, endpointAddress);
        JAXBContext jaxbContext =
            JAXBContext.newInstance(ObjectFactory.class);
        Dispatch<Object> dispatch = service.createDispatch(name,
            jaxbContext, Service.Mode.PAYLOAD);
        ClientServerTestUtil.setTransport(dispatch, null);
        return dispatch;
    }
    
    void clearHandlersInService(Service service) {
        service.setHandlerResolver(new HandlerResolver(){
            public List<Handler> getHandlerChain(PortInfo pi) {
                return new ArrayList<Handler>();
            }
        });
    }
    
}


