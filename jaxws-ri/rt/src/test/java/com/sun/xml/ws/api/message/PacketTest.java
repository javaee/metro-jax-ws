/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.w3c.dom.Node;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.binding.BindingImpl;

import junit.framework.TestCase;

public class PacketTest extends TestCase {
	/**
	 * Tests that a server response Packet with MTOM feature, but
	 * decoded from an InputStream with a user specified non-MTOM
	 * content type, does NOT use MTOM when re-encoded
	 * @throws Exception
	 */
	public void testEncodeDecodedPacketMtom() throws Exception {
		String msg = "<?xml version='1.0' encoding='UTF-8'?>" +
	"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
	"<soapenv:Body><soapenv:Fault>" +
	"<faultcode>soapenv:Server</faultcode>" +
	"<faultstring>ABC-380001:Internal Server Error</faultstring>" +
	"<detail><con:fault xmlns:con=\"http://www.bea.com/wli/sb/context\">" +
	"<con:errorCode>ABC-380001</con:errorCode>" +
	"<con:reason>Internal Server Error</con:reason>" + 
	"<con:location><con:node>RouteNode1</con:node><con:path>response-pipeline</con:path></con:location>" +
	"</con:fault></detail>" +
	"</soapenv:Fault></soapenv:Body></soapenv:Envelope>";
		WebServiceFeature[] features = {new MTOMFeature(true, 0)};
		MessageContextFactory mcf = new MessageContextFactory(features);
		Packet fakeRequest = (Packet) mcf.createContext();
		Packet p = (Packet) mcf.createContext(new ByteArrayInputStream(msg.getBytes()), 
				"text/xml");
		
		fakeRequest.relateServerResponse(p, null, null, BindingImpl.create(BindingID.SOAP11_HTTP, features));

		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		p.writeTo(bos);
		String writtenMsg = new String(bos.toByteArray());
		System.out.println(writtenMsg);
		assertEquals("text/xml", p.getContentType().getContentType());

		//try reading the message as a soap message with text/xml - this should succeed
		//in parsing the message
		Packet reReadPacket = (Packet) mcf.createContext(new ByteArrayInputStream(writtenMsg.getBytes()), "text/xml");
		SOAPMessage soap = reReadPacket.getAsSOAPMessage();
		Node bodyChild = soap.getSOAPBody().getFirstChild();
		assertEquals("Fault", bodyChild.getLocalName());
	}
}
