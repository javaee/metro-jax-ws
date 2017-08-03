/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: NegativeTest.java,v 1.3 2008-08-15 21:18:08 ramapulavarthi Exp $
 */

/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package server.misc.client;

import java.lang.reflect.Proxy;
import java.io.*;
import junit.framework.*;
import testutil.ClientServerTestUtil;
import testutil.HTTPResponseInfo;
import javax.xml.soap.*;
import javax.xml.namespace.QName;


/**
 *
 * @author Jitendra Kotamraju
 */
public class NegativeTest extends TestCase {

    public NegativeTest(String name) throws Exception {
        super(name);
    }

    HelloPortType getStub() throws Exception {
        return new HelloService().getHelloPort();
    }


    /*
     * Sends a malformed message
     */
    public void testMalformedMessageWithoutHeaders() throws Exception {
        String message =
"<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns1='http://example.com/types'><soapenv:Body><ns1:echo><ns1:reqInfo>foo<ns1:reqInfo></ns1:echo></soapenv:Body></soapenv:Envelope>";

        HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(getStub(),message);
        assertEquals(500, rInfo.getResponseCode());
    }

    /*
     * Sends a malformed message
     */
    public void testMalformedMessage() throws Exception {
        String message =
"<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns1='http://example.com/types'><soapenv:Header><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo></ns1:echo><ns1:echo2><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo2></soapenv:Header><soapenv:Body><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo></soapenv:Body><soapenv:Envelope>";

        HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(getStub(), message);
        assertEquals(500, rInfo.getResponseCode());
    }


    public void testEcho() throws Exception {
        ObjectFactory of = new ObjectFactory();
        EchoType request = of.createEchoType();
        request.setReqInfo("foo");
        Echo2Type header2 = of.createEcho2Type();
        header2.setReqInfo("foo");
        EchoResponseType response = getStub().echo(request, request, header2);
        assertEquals("foofoofoo", (response.getRespInfo()));
    }

    /*
     * Sends a message with a duplicate header
     */
    public void testDuplicateHeader() throws Exception {
        String message =
"<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns1='http://example.com/types'><soapenv:Header><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo><ns1:echo><ns1:reqInfo>duplicate foo</ns1:reqInfo></ns1:echo><ns1:echo2><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo2></soapenv:Header><soapenv:Body><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo></soapenv:Body></soapenv:Envelope>";

        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( getStub(), message );
        assertEquals(500, rInfo.getResponseCode());
        String resp = rInfo.getResponseBody();
        //System.out.println("Resp="+resp);
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
            new ByteArrayInputStream(resp.getBytes()));
        SOAPBody body = soapMsg.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("Duplicate header doesn't generate a fault on server");
        }
		QName got = body.getFault().getFaultCodeAsQName();
		QName exp = new QName("http://schemas.xmlsoap.org/soap/envelope/",
            "Client");
        assertEquals(exp, got);
    }

    /*
     * Sends a message with wrong Content-Type
     */
    public void testMediaType() throws Exception {
        String message =
"<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ns1='http://example.com/types'><soapenv:Header><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo><ns1:echo2><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo2></soapenv:Header><soapenv:Body><ns1:echo><ns1:reqInfo>foo</ns1:reqInfo></ns1:echo></soapenv:Body></soapenv:Envelope>";

        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( getStub(), message, "a/b" );
        assertEquals(415, rInfo.getResponseCode());
    }

    /*
     * Sends a message with wrong operation QName
     */
    public void testWrongOperationName() throws Exception {
        String message =
"<?xml version='1.0' encoding='UTF-8'?><env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><env:Body><wrongname xmlns='http://example.com/types'><reqInfo>foo</reqInfo></echo></env:Body></env:Envelope>";

        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( getStub(), message );
        assertEquals(500, rInfo.getResponseCode());
        String resp = rInfo.getResponseBody();
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
            new ByteArrayInputStream(resp.getBytes()));
        SOAPBody body = soapMsg.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("Wrong operation QName doesn't generate a fault on server");
        }
	QName got = body.getFault().getFaultCodeAsQName();
	QName exp = new QName("http://schemas.xmlsoap.org/soap/envelope/",
            "Client");
        assertEquals(exp, got);
    }

    /*
     * Sends a message with wrong envelope namespace
     */
    public void testEnvelopeNS() throws Exception {
        String message =
"<?xml version='1.0' encoding='UTF-8'?><env:Envelope xmlns:env='http://wrongname.org' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><env:Body><wrongname xmlns='http://example.com/types'><reqInfo>foo</reqInfo></echo></env:Body></env:Envelope>";

        HTTPResponseInfo rInfo = ClientServerTestUtil.sendPOSTRequest(getStub(),message);
        assertEquals(500, rInfo.getResponseCode());
        String resp = rInfo.getResponseBody();
        MessageFactory messageFactory = MessageFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage soapMsg = messageFactory.createMessage(headers,
            new ByteArrayInputStream(resp.getBytes()));
        SOAPBody body = soapMsg.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("Wrong operation QName doesn't generate a fault on server");
        }
	QName got = body.getFault().getFaultCodeAsQName();
	QName exp = new QName("http://schemas.xmlsoap.org/soap/envelope/",
            "VersionMismatch");
        assertEquals(exp, got);
    }

    /*
     * Sends empty body: <env:body/>
    public void testEmptyBody() throws Exception {
        String message =
"<?xml version='1.0' encoding='UTF-8'?><env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'><env:Header><echo xmlns='http://example.com/types'><reqInfo>foo</reqInfo></echo><echo2 xmlns='http://example.com/types'><reqInfo>foo</reqInfo></echo2></env:Header><env:Body></env:Body></env:Envelope>";
        ByteArrayInputStream is = new ByteArrayInputStream(message.getBytes());
        SOAPMessage response =
            ClientServerTestUtil.makeSaajRequest((EndpointIFBase)getStub(), is);
        SOAPBody body = response.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("Empty body doesn't generate a fault on server");
        }
    }
     */
}
