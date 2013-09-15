/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package server.mu_header_fault.client;

import junit.framework.TestCase;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import testutil.HTTPResponseInfo;
import testutil.ClientServerTestUtil;

/**
 * @author Rama Pulavarthi
 */
public class MUTest extends TestCase {
    private static String s11_request = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n" +
            "          <S:Header>\n" +
            "              <wsa:Action S:mustUnderstand=\"1\">http://www.example.org/mustunderstand_action</wsa:Action>\n" +
            "          </S:Header>\n" +
            "          <S:Body>\n" +
            "              <ns2:echo xmlns:ns2=\"http://server.mu_header_fault.server/\">\n" +
            "                  <arg0>Hello</arg0>\n" +
            "              </ns2:echo>\n" +
            "          </S:Body>\n" +
            "      </S:Envelope>";
    private static String s12_request="<S:Envelope xmlns:S=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
            "<S:Header>\n" +
            "     <wsa:Action S:mustUnderstand=\"1\">http://www.example.org/mustunderstand_action</wsa:Action>\n" +
            "</S:Header>\n" +
            "<S:Body><ns2:echo xmlns:ns2=\"http://server.mu_header_fault.server/\"><arg0>Hello</arg0></ns2:echo>" +
            "</S:Body></S:Envelope>";
    

    public void testMU_SOAP11() throws Exception {
        QName portQName = new QName("http://server.mu_header_fault.server/", "TestEndpointPort");
        Dispatch<SOAPMessage> dispatch = new TestEndpointService().createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage message = getSOAPMessage(makeStreamSource(s11_request));
        try {
            Object result = dispatch.invoke(message);
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            NodeList nl = ((Element)sf).getChildNodes();
            int codeIndex = indexOf(new QName("","faultcode"),nl);
            int reasonIndex = indexOf(new QName("","faultstring"),nl);
            assertTrue("<faultcode> and <faultstring> are not in proper order",codeIndex < reasonIndex);

        }
    }

    public void testMU_SOAP11_direct() throws Exception {

        TestEndpoint port = new TestEndpointService().getTestEndpointPort();
        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( port, s11_request);
        assertEquals(500, rInfo.getResponseCode());
        String resp = rInfo.getResponseBody();
        SOAPMessage respMesg = getSOAPMessage(makeStreamSource(resp));
        SOAPBody body = respMesg.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("MU Fault not thrown");
        }
        SOAPFault sf = body.getFault();
        NodeList nl = ((Element)sf).getChildNodes();
        int codeIndex = indexOf(new QName("", "faultcode"), nl);
        int reasonIndex = indexOf(new QName("", "faultstring"), nl);
        assertTrue("<faultcode> and <faultstring> are not in proper order", codeIndex < reasonIndex);

    }

    public void testMU_SOAP12() throws Exception {
        QName portQName = new QName("http://server.mu_header_fault.server/", "TestEndpoint12Port");
        Dispatch<SOAPMessage> dispatch = new TestEndpoint12Service().createDispatch(portQName, SOAPMessage.class, Service.Mode.MESSAGE);
        SOAPMessage message = getSOAP12Message(makeStreamSource(s12_request));
        try {
            Object result = dispatch.invoke(message);
        } catch (SOAPFaultException e) {
            SOAPFault sf = e.getFault();
            NodeList nl = ((Element)sf).getChildNodes();
            int codeIndex = indexOf(new QName("http://www.w3.org/2003/05/soap-envelope","Code"),nl);
            int reasonIndex = indexOf(new QName("http://www.w3.org/2003/05/soap-envelope","Reason"),nl);
            assertTrue("<env:Code> and <env:Reason> are not in proper order",codeIndex < reasonIndex);

        }


    }

    public void testMU_SOAP12_direct() throws Exception {

        TestEndpoint12 port = new TestEndpoint12Service().getTestEndpoint12Port();
        HTTPResponseInfo rInfo =
            ClientServerTestUtil.sendPOSTRequest( port, s12_request,"application/soap+xml" );
        assertEquals(500, rInfo.getResponseCode());
        String resp = rInfo.getResponseBody();
        SOAPMessage respMesg = getSOAP12Message(makeStreamSource(resp));
        SOAPBody body = respMesg.getSOAPPart().getEnvelope().getBody();
        if (!body.hasFault()) {
            fail("MU Fault not thrown");
        }
        SOAPFault sf = body.getFault();
        NodeList nl = ((Element) sf).getChildNodes();
        int codeIndex = indexOf(new QName("http://www.w3.org/2003/05/soap-envelope", "Code"), nl);
        int reasonIndex = indexOf(new QName("http://www.w3.org/2003/05/soap-envelope", "Reason"), nl);
        assertTrue("<env:Code> and <env:Reason> are not in proper order",codeIndex < reasonIndex);

    }
    private int indexOf(QName q, NodeList nl) {
        for(int i=0;i<nl.getLength();i++) {
            Node n = nl.item(i);
            if(n.getLocalName().equals(q.getLocalPart()) && fixNull(n.getNamespaceURI()).equals(q.getNamespaceURI())) {
                return i;
            }
        }
        return -1;

    }

    private String fixNull(String s) {
        if (s== null)
            return "";
        else
            return s;
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

    private static final SOAPMessage getSOAP12Message(Source msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent(msg);
        message.saveChanges();
        return message;
    }
}
