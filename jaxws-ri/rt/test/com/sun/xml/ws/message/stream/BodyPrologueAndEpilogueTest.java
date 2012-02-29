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
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.xml.ws.message.stream;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import junit.framework.TestCase;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Simple test for parsing white space bodyPrologue and bodyEpilogue; it is necessary to parse those and store separately
 * for message security purposes
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class BodyPrologueAndEpilogueTest extends TestCase {

    private static final String message1 = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'> <S:Header> <a> </a> <b> </b> </S:Header> <S:Body> \n <!-- Some Comment -->  \n <a> </a><b> </b> <c/> </S:Body> </S:Envelope>";
    private static final String message2 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:test=\"http://test.oracle.org/\">\n" +
            "   <soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsse:Security soapenv:mustUnderstand=\"1\" xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"><wsse:BinarySecurityToken EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\" wsu:Id=\"CertId-694295DF9FFCDDEDBF13195366779504\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">MIIDDzCCAnigAwIBAgIBAzANBgkqhkiG9w0BAQQFADBOMQswCQYDVQQGEwJBVTETMBEGA1UECBMKU29tZS1TdGF0ZTEMMAoGA1UEChMDU1VOMQwwCgYDVQQLEwNKV1MxDjAMBgNVBAMTBVNVTkNBMB4XDTA3MDMxMjEwMjQ0MFoXDTE3MDMwOTEwMjQ0MFowbzELMAkGA1UEBhMCQVUxEzARBgNVBAgTClNvbWUtU3RhdGUxITAfBgNVBAoTGEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEMMAoGA1UECxMDU1VOMRowGAYDVQQDExF4d3NzZWN1cml0eWNsaWVudDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAvYxVZKIzVdGMSBkW4bYnV80MV/RgQKV1bf/DoMTX8laMO45P6rlEarxQiOYrgzuYp+snzz2XM0S6o3JGQtXQuzDwcwPkH55bHFwHgtOMzxG4SQ653a5Dzh04nsmJvxvbncNH/XNaWfHaC0JHBEfNCMwRebYocxYM92pq/G5OGyECAwEAAaOB2zCB2DAJBgNVHRMEAjAAMCwGCWCGSAGG+EIBDQQfFh1PcGVuU1NMIEdlbmVyYXRlZCBDZXJ0aWZpY2F0ZTAdBgNVHQ4EFgQU/mItfvuFdS7A0GCysE71TFRxP2cwfgYDVR0jBHcwdYAUZ7plxs6VyOOOTSFyojDV0/YYjJWhUqRQME4xCzAJBgNVBAYTAkFVMRMwEQYDVQQIEwpTb21lLVN0YXRlMQwwCgYDVQQKEwNTVU4xDDAKBgNVBAsTA0pXUzEOMAwGA1UEAxMFU1VOQ0GCCQDbHkJaq6KijjANBgkqhkiG9w0BAQQFAAOBgQBEnRdcQeMyCYqOHw2jbPOPUlvu07bZe7sI3ly/Qz+4mkrFctqMSupghQtLv9dZcqDOUFLCGMse7+l5MG00VawzsoVe242iXzJB111ePzhhppIPOHXXtflj/JD2U4Qz75C/dfdd5AAZbqGSFtZh7pyE8Ot1vOq7R48/bHuvTsEVUQ==</wsse:BinarySecurityToken><ds:Signature Id=\"Signature-2\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
            "<ds:SignedInfo>\n" +
            "<ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\n" +
            "<ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>\n" +
            "<ds:Reference URI=\"#id-14\">\n" +
            "<ds:Transforms>\n" +
            "<ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>\n" +
            "</ds:Transforms>\n" +
            "<ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>\n" +
            "<ds:DigestValue>ofEikgnP5dyM+ABJTRqoHdX0iUk=</ds:DigestValue>\n" +
            "</ds:Reference>\n" +
            "</ds:SignedInfo>\n" +
            "<ds:SignatureValue>\n" +
            "KbAMNkeOZTmYts+rA5SLroisXdMiAwzDsi5+PQp0WSWkEb2UAHY3FtwEECt2/AskWSu29j8elcUk\n" +
            "dhqJI4OdkFJhYOq2+US1H1G4UkNz44nPt9L0XuzUHnG8kQy1mQROnrcy3vrkTp8zAnn7vxjAm301\n" +
            "DpYRJ1qEfypyWAqs6JU=\n" +
            "</ds:SignatureValue>\n" +
            "<ds:KeyInfo Id=\"KeyId-694295DF9FFCDDEDBF13195366779515\">\n" +
            "<wsse:SecurityTokenReference wsu:Id=\"STRId-694295DF9FFCDDEDBF13195366779516\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"><wsse:Reference URI=\"#CertId-694295DF9FFCDDEDBF13195366779504\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\"/></wsse:SecurityTokenReference>\n" +
            "</ds:KeyInfo>\n" +
            "</ds:Signature></wsse:Security>\n" +
            "        <To xmlns=\"http://www.w3.org/2005/08/addressing\">http://localhost:8080/UsernameService/UsernameServiceService</To>\n" +
            "        <ReplyTo xmlns=\"http://www.w3.org/2005/08/addressing\">\n" +
            "            <Address>http://www.w3.org/2005/08/addressing/anonymous</Address>\n" +
            "        </ReplyTo>\n" +
            "        <MessageID xmlns=\"http://www.w3.org/2005/08/addressing\">uuid:69477efc-0981-4967-aa7c-1d0d7fa549c1</MessageID>\n" +
            "   <wsa:Action soapenv:mustUnderstand=\"1\">http://test.oracle.org/UsernameService/greetingRequest</wsa:Action></soapenv:Header>\n" +
            "<soapenv:Body wsu:Id=\"id-14\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\"> <test:greeting><param>ABCD</param></test:greeting> </soapenv:Body></soapenv:Envelope>";

    private StreamMessage useStreamCodec(String msg) throws IOException {
        Codec codec = Codecs.createSOAPEnvelopeXmlCodec(SOAPVersion.SOAP_11);
        Packet packet = new Packet();
        ByteArrayInputStream in = new ByteArrayInputStream(msg.getBytes());
        codec.decode(in, "text/xml", packet);
        return (com.sun.xml.ws.message.stream.StreamMessage) packet.getInternalMessage();
    }

    private void logMessage(Message message) throws XMLStreamException {
        StreamMessage streamMessage;
        message.writeTo(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));

        System.out.flush();
        streamMessage = (StreamMessage) message;
        System.out.println("\n\nbodyPrologue = [" + streamMessage.getBodyPrologue() + "]");
        System.out.println("bodyEpilogue = [" + streamMessage.getBodyEpilogue() + "]");
        System.out.println("\n\n==================================================================================================\n\n");
    }

    public void testMessage1() throws IOException, XMLStreamException {
        StreamMessage message = useStreamCodec(message1);
        logMessage(message);
        assertEquals(message.getBodyPrologue(), " \n   \n ");
        assertEquals(message.getBodyEpilogue(), " ");
    }

    public void testMessage2() throws IOException, XMLStreamException {
        StreamMessage message = useStreamCodec(message2);
        logMessage(message);
        assertEquals(message.getBodyPrologue(), " ");
        assertEquals(message.getBodyEpilogue(), " ");
    }

    public void testMessage2Copied() throws IOException, XMLStreamException {
        StreamMessage message = useStreamCodec(message2);
        message = (StreamMessage) message.copy();
        logMessage(message);
        assertEquals(message.getBodyPrologue(), " ");
        assertEquals(message.getBodyEpilogue(), " ");
    }

}
