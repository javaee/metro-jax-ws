/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.handler_singlepipe.client;

import junit.framework.TestCase;
import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.bind.JAXBContext;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import static fromwsdl.handler_singlepipe.common.TestConstants.*;
import testutil.ClientServerTestUtil;

public abstract class TestCaseBase extends TestCase{

    // Dispatch creation use
    static final QName serviceQName = new QName("urn:test", "TestService");
    static final QName testPortQName = new QName("urn:test", "TestServicePort");
    static final QName reportPortQName =
        new QName("urn:test", "ReportServicePort");
    static final String bindingIdString = SOAPBinding.SOAP11HTTP_BINDING;

    static String NEXT_1_1;
    static String NEXT_1_2;
    static String NONE;
    static String ULTIMATE_RECEIVER;



    public TestCaseBase(String name) {
        super(name);
        fromwsdl.handler_singlepipe.client.TestCaseBase.NEXT_1_1 = "http://schemas.xmlsoap.org/soap/actor/next";
        fromwsdl.handler_singlepipe.client.TestCaseBase.NEXT_1_2 = "http://www.w3.org/2003/05/soap-envelope/role/next";
        fromwsdl.handler_singlepipe.client.TestCaseBase.NONE = "http://www.w3.org/2003/05/soap-envelope/role/none";
        fromwsdl.handler_singlepipe.client.TestCaseBase.ULTIMATE_RECEIVER =
            "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver";
    }

    Hello_Service createService() throws Exception {
        return new Hello_Service();
    }

    // util method when the service isn't needed
    Hello createStub() throws Exception {
        return createStub(createService());
    }

    Hello12 create12Stub() throws Exception {
        return create12Stub(createService());
    }

    ReportService createReportStub() throws Exception {
        return createReportStub(createService());
    }
    Hello createStub(Hello_Service service) throws Exception {
        Hello stub = service.getHelloPort();
        ClientServerTestUtil.setTransport(stub);
        return stub;
    }

    Hello12 create12Stub(Hello_Service service) throws Exception {
        Hello12 stub = service.getHelloPort12();
        ClientServerTestUtil.setTransport(stub);
        return stub;
    }

    private String getEndpointAddress(String defaultAddress) {
        if(ClientServerTestUtil.useLocal())
            return "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/');
        else
            return defaultAddress;
    }

    ReportService createReportStub(Hello_Service service) throws Exception {
        ReportService stub = service.getReportServicePort();
        ClientServerTestUtil.setTransport(stub);
        ((BindingProvider) stub).getBinding().setHandlerChain(Collections.EMPTY_LIST);
        return stub;
    }
}
