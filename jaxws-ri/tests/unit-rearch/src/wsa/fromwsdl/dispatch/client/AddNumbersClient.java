/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2013 Oracle and/or its affiliates. All rights reserved.
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

package wsa.fromwsdl.dispatch.client;

import com.sun.xml.ws.addressing.W3CAddressingConstants;
import org.custommonkey.xmlunit.XMLTestCase;
import testutil.ClientServerTestUtil;
import testutil.WsaUtils;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.MessageFactory;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.jws.HandlerChain;
import java.util.Set;
import java.util.List;

/**
 * @author Arun Gupta
 */
public class AddNumbersClient extends XMLTestCase {
    private static final QName SERVICE_QNAME = new QName("http://example.com/", "AddNumbersService");
    private static final QName PORT_QNAME = new QName("http://example.com/", "AddNumbersPort");
    private static final String ENDPOINT_ADDRESS = "http://localhost:/jaxrpc-wsa_fromwsdl_dispatch/hello";
    private static final String ADD_NUMBERS_ACTION = "http://example.com/AddNumbersPortType/addNumbers1Request";
    private static final String ADD_NUMBERS2_ACTION = "add2InAction";
    private static final String ADD_NUMBERS3_ACTION = "http://example.com/AddNumbersPortType/addNumbers3Request";
    private static final String ADD_NUMBERS4_ACTION = "http://example.com/AddNumbersPortType/addNumbers4Request";

    public AddNumbersClient(String name) {
        super(name);
    }

    private String getAddress() {
        if (ClientServerTestUtil.useLocal())
            return ClientServerTestUtil.getLocalAddress(PORT_QNAME);
        else
            return ENDPOINT_ADDRESS;
    }

    private Dispatch<SOAPMessage> createDispatchWithoutWSDL() throws Exception {
        AddNumbersService service  = new AddNumbersService();
        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME,
                                                                SOAPMessage.class,
                                                                Service.Mode.MESSAGE,
                                                                new AddressingFeature(false, false));
        return dispatch;
    }

    public void testActionMismatch() throws Exception {
        Dispatch<SOAPMessage> dispatch = createDispatchWithoutWSDL();
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"FakeSOAPAction");
        try {
            WsaUtils.invoke(dispatch,
                    WsaUtils.ACTION_DISPATCH_MESSAGE1,
                    WsaUtils.S11_NS,
                    WsaUtils.W3C_WSA_NS,
                    getAddress(),
                    W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                    ADD_NUMBERS_ACTION);
        } catch (SOAPFaultException sfe) {
            assertFault(sfe, W3CAddressingConstants.ACTION_MISMATCH);
        }
    }

    public void testAddNumbersDefaultActionDispatch() throws Exception {
        WsaUtils.invoke(createDispatchWithoutWSDL(),
                              WsaUtils.ACTION_DISPATCH_MESSAGE1,
                              WsaUtils.S11_NS,
                              WsaUtils.W3C_WSA_NS,
                              getAddress(),
            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                              ADD_NUMBERS_ACTION);
    }

    public void testAddNumbers2ActionDispatch() throws Exception {
        WsaUtils.invoke(createDispatchWithoutWSDL(),
                              WsaUtils.ACTION_DISPATCH_MESSAGE1,
                              WsaUtils.S11_NS,
                              WsaUtils.W3C_WSA_NS,
                              getAddress(),
            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                              ADD_NUMBERS2_ACTION);
    }

    public void testAddNumbers3ActionDispatch() throws Exception {
        WsaUtils.invoke(createDispatchWithoutWSDL(),
                              WsaUtils.ACTION_DISPATCH_MESSAGE1,
                              WsaUtils.S11_NS,
                              WsaUtils.W3C_WSA_NS,
                              getAddress(),
            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                              ADD_NUMBERS3_ACTION);
    }

    public void testAddNumbers4ActionDispatch() throws Exception {
        WsaUtils.invoke(createDispatchWithoutWSDL(),
                              WsaUtils.ACTION_DISPATCH_MESSAGE1,
                              WsaUtils.S11_NS,
                              WsaUtils.W3C_WSA_NS,
                              getAddress(),
            W3CAddressingConstants.WSA_ANONYMOUS_ADDRESS,
                              ADD_NUMBERS4_ACTION);
    }

    public void testAddressingWithNoWSDL() throws Exception {
        Service service = Service.create(SERVICE_QNAME);
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());
        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME,
                                                                SOAPMessage.class,
                                                                Service.Mode.MESSAGE,
                                                                new AddressingFeature());
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,ADD_NUMBERS_ACTION);
        String message = 	"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\">" +
             "<number1>10</number1>" +
            "<number2>10</number2>" +
            "</addNumbers>" +
            "</S:Body></S:Envelope>";
        WsaUtils.invoke(dispatch,message);
    }

    public void testAddressingWithNoWSDLwithHandler() throws Exception {
        Service service = Service.create(SERVICE_QNAME);
        service.addPort(PORT_QNAME, SOAPBinding.SOAP11HTTP_BINDING, getAddress());
        Dispatch<SOAPMessage> dispatch = service.createDispatch(PORT_QNAME,
                                                                SOAPMessage.class,
                                                                Service.Mode.MESSAGE,
                                                                new AddressingFeature());
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY,true);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,ADD_NUMBERS_ACTION);
        String message = 	"<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\"><S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\">" +
             "<number1>10</number1>" +
            "<number2>10</number2>" +
            "</addNumbers>" +
                "</S:Body></S:Envelope>";
        List<Handler> handlerChain = dispatch.getBinding().getHandlerChain();
        handlerChain.add(new MyHandler());
        dispatch.getBinding().setHandlerChain(handlerChain);
        WsaUtils.invoke(dispatch,message);
    }

    private void assertFault(SOAPFaultException sfe, QName expected) {
        assertNotNull("SOAPFaultException is null", sfe);
        assertNotNull("SOAPFault is null", sfe.getFault());
        assertEquals(expected, sfe.getFault().getFaultCodeAsQName());
    }

    private static class MyHandler implements SOAPHandler<SOAPMessageContext> {

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        SOAPMessage message = smc.getMessage();

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
    }
}

}
