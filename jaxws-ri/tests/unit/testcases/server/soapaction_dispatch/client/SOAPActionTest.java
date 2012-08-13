/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package server.soapaction_dispatch.client;

import junit.framework.TestCase;
import testutil.HTTPResponseInfo;
import testutil.ClientServerTestUtil;

import javax.xml.ws.BindingProvider;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.MessageFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

import org.w3c.dom.Element;

/**
 * @author Rama Pulavarthi
 */
public class SOAPActionTest extends TestCase {
    private static String s11_request = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "          <S:Body>\n" +
            "              <ns2:input xmlns:ns2=\"http://server.soapaction_dispatch.server/\">\n" +
            "                  <arg0>Duke</arg0>\n" +
            "              </ns2:input>\n" +
            "          </S:Body>\n" +
            "      </S:Envelope>";

    public void testUnquotedSOAPAction1() throws Exception {
       TestEndpoint port = new TestEndpointService().getTestEndpointPort1();
        String address = (String) ((BindingProvider)port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( address, s11_request,"text/xml","http://example.com/action/echo");
        String resp = rInfo.getResponseBody();
        SOAPMessage respMesg = getSOAPMessage(makeStreamSource(resp));
        SOAPBody body = respMesg.getSOAPPart().getEnvelope().getBody();
        Element e = (Element)body.getElementsByTagName("return").item(0);
        //make sure it is dispatched to echo() using SoapAction
        assertEquals("Hello Duke", e.getTextContent());
    }

    public void testUnquotedSOAPAction2() throws Exception {
       TestEndpoint port = new TestEndpointService().getTestEndpointPort1();
        String address = (String) ((BindingProvider)port).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( address, s11_request,"text/xml","http://example.com/action/echo1");
        String resp = rInfo.getResponseBody();
        SOAPMessage respMesg = getSOAPMessage(makeStreamSource(resp));
        SOAPBody body = respMesg.getSOAPPart().getEnvelope().getBody();
        Element e = (Element)body.getElementsByTagName("return").item(0);
        //make sure it is dispatched to echo1() using SoapAction
        assertEquals("Hello1 Duke", e.getTextContent());
    }

    public void testSOAP12Action() {
        TestEndpoint port = new TestEndpointService().getTestEndpointPort2();
        Echo input = new Echo();
        input.setArg0("Duke");
        EchoResponse response = port.echo(input);
        assertEquals("Hello Duke",response.getReturn()); 

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

}
