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

package com.sun.xml.ws.api.message;

import java.io.ByteArrayInputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.SOAPVersion;

import junit.framework.TestCase;

public class ReplaceAddressingHeaderTest extends TestCase {


  public static final String TEST_NS = "http://jaxws.dev.java.net/";
  private HeaderList testInstance;

  public ReplaceAddressingHeaderTest(String name) {
      super(name);
  }

  @Override
  protected void setUp() throws Exception {
      super.setUp();

      testInstance = new HeaderList(SOAPVersion.SOAP_11);
  }

  @Override
  protected void tearDown() throws Exception {
      super.tearDown();

      testInstance = null;
  }

  public void testReplaceBehavior() throws Exception {

      String reqMsgStr ="<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">" +
      "<env:Header>" +
      "<r:AckRequested xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:r=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\" xmlns:a=\"http://www.w3.org/2005/08/addressing\">" +
      "<r:Identifier>uuid:WLS2:store:WseeJaxwsFileStore:dece97a1d44772e7:-3fbed9f:13b4b7da0a6:-7fb6</r:Identifier>" +
      "</r:AckRequested>" +
      "<a:Action xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:r=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\" " +
      "xmlns:a=\"http://www.w3.org/2005/08/addressing\" s:mustUnderstand=\"1\">http://docs.oasis-open.org/ws-rx/wsrm/200702/AckRequested</a:Action>" +
      "<a:To xmlns:s=\"http://www.w3.org/2003/05/soap-envelop\" " +
      "xmlns:r=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\" xmlns:a=\"http://www.w3.org/2005/08/addressing\" " +
      "s:mustUnderstand=\"1\">http://10.245.29.191:9902/ReliableMessaging_Service_WSAddressing10_Indigo/OneWay.svc/Reliable_Addressable_Soap12_WSAddressing10_RM11</a:To>" +
      "</env:Header>" +
      "<env:Body/>" +
      "</env:Envelope>";

      String respMsgStr = "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">" +
      "<env:Header>" +
      "<Action xmlns=\"http://www.w3.org/2005/08/addressing\">http://docs.oasis-open.org/ws-rx/wsrm/200702/SequenceAcknowledgement</Action>" +
      "<wsrm11:SequenceAcknowledgement xmlns:wsrm11=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\">" +
      "<wsrm11:Identifier>uuid:WLS2:store:WseeJaxwsFileStore:dece97a1d44772e7:-3fbed9f:13b4b7da0a6:-7fb6</wsrm11:Identifier>" +
      "<wsrm11:AcknowledgementRange Lower=\"1\" Upper=\"3\"/>" +
      "</wsrm11:SequenceAcknowledgement>" +
      "<To xmlns=\"http://www.w3.org/2005/08/addressing\">" +
      "http://10.244.13.245:8000/bcabf5e4-d888-403c-a93e-99ed5e7f4a40/fe5c31e3-a8af-40ed-8066-e41c3ba9f742</To>" +
      "<ns0:ReplyTo xmlns:ns0=\"http://www.w3.org/2005/08/addressing\">" +
      "<ns0:Address>" +
      "http://10.245.29.191:9902/ReliableMessaging_Service_WSAddressing10_Indigo/OneWay.svc/Reliable_Addressable_Soap12_WSAddressing10_RM11" +
      "</ns0:Address>" +
      "<ns0:Metadata xmlns:ns1=\"http://www.w3.org/ns/wsdl-instance\" " +
      "ns1:wsdlLocation=\"http://tempuri.org/http://10.245.29.191:9902/ReliableMessaging_Service_WSAddressing10_Indigo/OneWay.svc/Reliable_Addressable_Soap12_WSAddressing10_RM11?wsdl\">" +
      "<wsam:InterfaceName xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:wsns=\"http://tempuri.org/\">" +
      "wsns:IPing</wsam:InterfaceName>" +
      "<wsam:ServiceName xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:wsns=\"http://tempuri.org/\" EndpointName=\"CustomBinding_IPing10\">" +
      "wsns:PingService</wsam:ServiceName>" +
      "</ns0:Metadata>" +
      "</ns0:ReplyTo>" +
      "</env:Header>" +
      "<env:Body/></env:Envelope>";

      AddressingVersion av = AddressingVersion.W3C;
      SOAPVersion sv = SOAPVersion.SOAP_12;
      String action = "http://docs.oasis-open.org/ws-rx/wsrm/200702/SequenceAcknowledgement";

      SAAJMessage reqMsg = new SAAJMessage(makeSOAPMessage(reqMsgStr));
      SAAJMessage respMsg = new SAAJMessage(makeSOAPMessage(respMsgStr));
      HeaderList hdrs = (HeaderList) respMsg.getMessageHeaders();
      String originToHeader = hdrs.getTo(av, sv);

      Packet responsePacket=null;
      try{
          responsePacket = new Packet(reqMsg).createServerResponse(respMsg, av, sv, action);
      } catch (Exception e) {
          e.printStackTrace();
      }

      //check toHeader
      String toHeaderAfterProcessed = responsePacket.getHeaderList().getTo(av, sv);
      assertTrue(toHeaderAfterProcessed.equals(originToHeader));
  }
  private SOAPMessage makeSOAPMessage(String msg) throws Exception {
      MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
      SOAPMessage message = factory.createMessage();
      Source src = new StreamSource(new ByteArrayInputStream(msg.getBytes()));
      message.getSOAPPart().setContent(src);
      return message;
  }

}
