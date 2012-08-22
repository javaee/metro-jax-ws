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

package wsa.w3c.fromwsdl.issue608.client;

import junit.framework.TestCase;

import javax.xml.ws.BindingProvider;

import testutil.HTTPResponseInfo;
import testutil.ClientServerTestUtil;

/**
 * Tests conformance of BP 1.2 conformace requirement R1144
 * (http://www.ws-i.org/Profiles/BasicProfile-1_2(WGAD).html#Valid_Range_of_Values_for_SOAPAction_When_WS-Addressing_is_Used)
 *
 * @author Rama Pulavarthi
 */
public class WsaActionTest extends TestCase {
    public WsaActionTest(String name) throws Exception {
        super(name);
    }

    AddNumbersPortType getStub() throws Exception {
        return new AddNumbersService().getAddNumbersPort();
    }

    private String getEndpointAddress() throws Exception{

        BindingProvider bp = ((BindingProvider) getStub());
        return
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
    }

    /**
     *   Useful for creating sample messages
     */
    public void testSimple() throws Exception {
        AddNumbersPortType proxy = getStub();
        assertEquals(4,proxy.addNumbers(1,3));
    }

    /**
     * SOAPAction HTTP header is not sent in HTTPRequest
     * @throws Exception
     */
    public void testNoSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", null );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction: "" is sent as HTTP header
     * @throws Exception
     */
    public void testEmptyStringSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"\"" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction:  (with no value) is sent as HTTP header
     * @throws Exception
     */
    public void testEmptySOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header same as SOAPAction defined in WSDL, but sent as unquoted String 
     * @throws Exception
     */
    public void testNonEmptyUnquotedSOAPactionSameAsWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "urn:com:example:action" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header same as SOAPAction defined in WSDL sent as quoted.
     * @throws Exception
     */
    public void testNonEmptyQuotedSOAPactionSameAsWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:example:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"urn:com:example:action\"" );

    assertEquals(200, rInfo.getResponseCode());
    }

    /**
     * SOAPAction HTTP header different from SOAPAction defined in WSDL sent as quoted.
     *
     * Issue jax-ws-608: JAX-WS Runtime should validate wsa:Action with SOAPAction HTTP header instead of
     * WSDL soapAction
     *
     * @throws Exception
     */

    public void testNonEmptyQuotedSOAPactionDiffFromWSDLSOAPAction() throws Exception {
    String address= getEndpointAddress();
    String message = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "<S:Header><To xmlns=\"http://www.w3.org/2005/08/addressing\">"+address+"</To>" +
            "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">urn:com:different:action</Action>" +
            "<ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">" +
            "    <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>" +
            "</ReplyTo>" +
            "<MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:4a7bee60-cd49-4345-a25b-35fb6a5f2e36</MessageID>" +
            "</S:Header>" +
            "<S:Body>" +
            "<addNumbers xmlns=\"http://example.com/\"><number1>1</number1><number2>3</number2></addNumbers></S:Body></S:Envelope>";

    HTTPResponseInfo rInfo =
        ClientServerTestUtil.sendPOSTRequest( address, message,"text/xml", "\"urn:com:different:action\"" );
    //Service throws wsa:ActionNotSupported fault.
    assertEquals(500, rInfo.getResponseCode());
    }

}
