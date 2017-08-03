/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package client.soap_mu_header.server;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import java.io.ByteArrayInputStream;

@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello",
    portName="HelloPort")
@ServiceMode(value= Service.Mode.MESSAGE)
public class HelloImpl implements Provider<SOAPMessage> {

    public SOAPMessage invoke(SOAPMessage m) {
        try {
            //String outheader = m.getSOAPHeader().getChildNodes().item(0).getTextContent();
            String soapbody = m.getSOAPBody().getChildNodes().item(0).getTextContent();
            if(soapbody.equals("extraMU")) {
                return getMessage2();
            } else {
                return getMessage1();
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }
    
    private SOAPMessage getMessage1() throws SOAPException {
        String msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"+
            "<S:Header><ns1:Extra xmlns:ns1='urn:test:types' S:mustUnderstand=\"1\">outheader</ns1:Extra></S:Header>"+
            "<S:Body>"+
            "<ns1:HelloResponse xmlns:ns1='urn:test:types'>Hello Duke</ns1:HelloResponse>"+
	        "</S:Body>"+
            "</S:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(msg.getBytes()));
        message.getSOAPPart().setContent(src);
        return message;
    }
    private SOAPMessage getMessage2() throws SOAPException {
        String msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"+
            "<S:Header><ns1:Extra xmlns:ns1='urn:test:types' S:mustUnderstand=\"1\">outheader</ns1:Extra>"+
            "<ns1:ExtraExtra xmlns:ns1='urn:test:types' S:mustUnderstand=\"1\">outheader</ns1:ExtraExtra></S:Header>"+    
            "<S:Body>"+
            "<ns1:HelloResponse xmlns:ns1='urn:test:types'>Hello Duke</ns1:HelloResponse>"+
	        "</S:Body>"+
            "</S:Envelope>";

        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(msg.getBytes()));
        message.getSOAPPart().setContent(src);
        return message;
    }
}
