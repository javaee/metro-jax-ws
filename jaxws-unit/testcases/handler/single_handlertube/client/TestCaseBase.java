/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package handler.single_handlertube.client;

import junit.framework.TestCase;
import testutil.ClientServerTestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.util.Collections;

/**
 * @author Rama Pulavarthi
 */

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
        handler.single_handlertube.client.TestCaseBase.NEXT_1_1 = "http://schemas.xmlsoap.org/soap/actor/next";
        handler.single_handlertube.client.TestCaseBase.NEXT_1_2 = "http://www.w3.org/2003/05/soap-envelope/role/next";
        handler.single_handlertube.client.TestCaseBase.NONE = "http://www.w3.org/2003/05/soap-envelope/role/none";
        handler.single_handlertube.client.TestCaseBase.ULTIMATE_RECEIVER =
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
        return stub;
    }

    Hello12 create12Stub(Hello_Service service) throws Exception {
        Hello12 stub = service.getHelloPort12();
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
        ((BindingProvider) stub).getBinding().setHandlerChain(Collections.EMPTY_LIST);
        return stub;
    }
}