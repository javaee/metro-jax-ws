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

package client.soap_mu_header.client;

import client.soap_mu_header.client.Hello;
import client.soap_mu_header.client.Hello_Service;
import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;

/**
 * @author Rama Pulavarthi
 */
public class SoapMuHeaderTest extends TestCase {
    public SoapMuHeaderTest(String name) throws Exception {
        super(name);
    }

    public void testMUHeaderInWSDL() {
        Hello_Service service = new Hello_Service();
        Hello port = service.getHelloPort();
        Holder<String> extra = new Holder<String>();
        port.hello("extra", extra);
    }

    public void testMUHeaderNotInWSDL() {
        Hello_Service service = new Hello_Service();
        Hello port = service.getHelloPort();
        Holder<String> extra = new Holder<String>();
        try {
            port.hello("extraMU", extra);
            fail("MU Exception expected");
        } catch (SOAPFaultException e) {
            assertTrue(e.getFault().getFaultCode().endsWith(":MustUnderstand"));

        }
    }

    /**
     * For Message mode, extra MU headers are ignored for MU processing to let client apps deal with it.
     */
    public void testMUHeaderInWSDLDispatchMessageMode() {
     Hello_Service service = new Hello_Service();
        Dispatch<SOAPMessage> dispatch =service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage in = null;
        try {
            in = getSOAPMessage(makeStreamSource(message1));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        SOAPMessage out = dispatch.invoke(in);

    }

    /**
     * For Message mode, extra MU headers are ignored for MU processing to let client apps deal with it.
     */
    public void testMUHeaderNotInWSDLDispatchMessageMode() {
     Hello_Service service = new Hello_Service();
        Dispatch<SOAPMessage> dispatch =service.createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage in = null;
        try {
            in = getSOAPMessage(makeStreamSource(message2));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        SOAPMessage out = dispatch.invoke(in);

    }

    /**
     * With Dispatch palyload mode, All MU headers (even described in WSDL binding) are not not understood by default.
     */
    public void testMUHeaderInWSDLDispatchPayloadMode() {
     Hello_Service service = new Hello_Service();
        Dispatch<Source> dispatch =service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
        Source in = null;
        try {
            in = makeStreamSource(payload1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
        Source out = dispatch.invoke(in);
        fail("MU Exception expected");
        } catch (SOAPFaultException e) {
            assertTrue(e.getFault().getFaultCode().endsWith(":MustUnderstand"));

        }

    }

    /**
     * With Dispatch palyload mode, All MU headers (even described in WSDL binding) are not not understood by default.
     */
    public void testMUHeaderNotInWSDLDispatchPayloadMode() {
     Hello_Service service = new Hello_Service();
        Dispatch<Source> dispatch =service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
        Source in = null;
        try {
            in = makeStreamSource(payload2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
        Source out = dispatch.invoke(in);
        fail("MU Exception expected");
        } catch (SOAPFaultException e) {
            assertTrue(e.getFault().getFaultCode().endsWith(":MustUnderstand"));

        }
    }

    private static final Source makeStreamSource(String msg) {
        byte[] bytes = msg.getBytes();
        ByteArrayInputStream sinputStream = new ByteArrayInputStream(bytes);
        return new StreamSource(sinputStream);
    }

    private static final SOAPMessage getSOAPMessage(Source msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent(msg);
        message.saveChanges();
        return message;
    }

    private static String message1 = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<S:Body><Hello xmlns=\"urn:test:types\">extra</Hello></S:Body>" +
            "</S:Envelope>";
    private static String message2 = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<S:Body><Hello xmlns=\"urn:test:types\">extraMU</Hello></S:Body>" +
            "</S:Envelope>";

    private static String payload1 = "<Hello xmlns=\"urn:test:types\">extra</Hello>";
    private static String payload2 = "<Hello xmlns=\"urn:test:types\">extraMU</Hello>";


    private static QName portQName = new QName("urn:test", "HelloPort");



}
