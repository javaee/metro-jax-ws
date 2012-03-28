 /* Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.message.saaj;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.api.message.saaj.SAAJMessageHeaders;
import com.sun.xml.ws.message.StringHeader;
import com.sun.xml.ws.message.stream.StreamHeader11;

import junit.framework.TestCase;

public class SAAJMessageHeadersTest extends TestCase {
    private static final String ACTION_HDR_VALUE = "http://example.com/addNumbers";
    final String ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    final String MESSAGE  =   "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
    "<S:Header>" +
    "<wsa:Action xmlns:wsa=\"" + ADDRESSING_NS + "\">" + ACTION_HDR_VALUE + "</wsa:Action>" +
    "</S:Header>" +
    "<S:Body>" +
    "<addNumbers xmlns=\"http://example.com/\">" +
    "<number1>10</number1>" +
    "<number2>10</number2>" +
    "</addNumbers>" +
    "</S:Body></S:Envelope>";
    
    public void testGetHeader() throws Exception {
//        SAAJMessage saajMsg = new SAAJMessage(makeSOAPMessage());
        MessageHeaders hdrs = new SAAJMessageHeaders(makeSOAPMessage(MESSAGE), SOAPVersion.SOAP_11);
        Header actionHdr = hdrs.get(new QName(ADDRESSING_NS, "Action"), false);
        assertNotNull(actionHdr);
        assertTrue(actionHdr instanceof SAAJHeader);
        SOAPHeaderElement hdrElem = ((SAAJHeader) actionHdr).getWrappedNode();
        assertNotNull(hdrElem);
        assertEquals(ACTION_HDR_VALUE, hdrElem.getFirstChild().getNodeValue());
        
        Iterator<Header> iter = hdrs.getHeaders(ADDRESSING_NS, false);
        assertTrue(iter.hasNext());
        assertEquals(actionHdr, iter.next());
        
        hdrs.remove(ADDRESSING_NS, "Action");
        
        //should be gone after remove
        iter = hdrs.getHeaders(ADDRESSING_NS, false);
        assertFalse(iter.hasNext());
    }
    
    public void testMustUnderstand() throws Exception {
        SOAPMessage sm = makeSOAPMessage(MESSAGE);
        
//        SAAJMessage saajMsg = new SAAJMessage(sm);
        MessageHeaders hdrs = new SAAJMessageHeaders(sm, SOAPVersion.SOAP_11);
        
        //new must understand header
        Header newHdr = new StringHeader(
                new QName("myNs", "stringHeader1"), 
                "stringHeaderValue1",
                SOAPVersion.SOAP_11,
                true);
        
        hdrs.add(newHdr);
        
        Set<QName> notUnderstoods = hdrs.getNotUnderstoodHeaders(null, null, null);
        assertNotNull(notUnderstoods);
        assertEquals(1, notUnderstoods.size());
        
        //now "understand" the header
        hdrs.understood(newHdr);
        notUnderstoods = hdrs.getNotUnderstoodHeaders(null, null, null);
        assertNotNull(notUnderstoods);
        assertEquals(0, notUnderstoods.size());        
    }
    
    /**
     * Tests mustUnderstand using the getHeaders call with boolean rather than the "understood(..)" call
     * @throws Exception
     */
    public void testMustUnderstand2() throws Exception {
        SOAPMessage sm = makeSOAPMessage(MESSAGE);
        
//      SAAJMessage saajMsg = new SAAJMessage(sm);
      MessageHeaders hdrs = new SAAJMessageHeaders(sm, SOAPVersion.SOAP_11);
      
      //new must understand header
      Header newHdr = new StringHeader(
              new QName("myNs", "stringHeader1"), 
              "stringHeaderValue1",
              SOAPVersion.SOAP_11,
              true);
      
      hdrs.add(newHdr);
      
      String hdrTxt = "<streamHeader1 S:mustUnderstand=\"true\" xmlns=\"myNs\" xmlns:S=\"" + 
      SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE + "\">streamHeaderValue1</streamHeader1>";
      ByteArrayInputStream bis = new ByteArrayInputStream(hdrTxt.getBytes());
      XMLStreamReader rdr = XMLInputFactory.newInstance().createXMLStreamReader(bis);
      while (rdr.getEventType() != XMLStreamReader.START_ELEMENT) rdr.next();
      newHdr = new StreamHeader11(rdr);
      
      hdrs.add(newHdr);
      
      hdrTxt = "<streamHeader2 S:mustUnderstand=\"true\" xmlns=\"mySecondNs\" xmlns:S=\"" + 
      SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE + "\">streamHeaderValue2</streamHeader2>";
      bis = new ByteArrayInputStream(hdrTxt.getBytes());
      rdr = XMLInputFactory.newInstance().createXMLStreamReader(bis);
      while (rdr.getEventType() != XMLStreamReader.START_ELEMENT) rdr.next();
      newHdr = new StreamHeader11(rdr);
      
      hdrs.add(newHdr);
      
      //now add a non-must understand string header
      newHdr = new StringHeader(
              new QName("myNs", "stringHeader2"), 
              "stringHeaderValue2",
              SOAPVersion.SOAP_11,
              false);
      
      hdrs.add(newHdr);
      
      Set<QName> notUnderstoods = hdrs.getNotUnderstoodHeaders(null, null, null);
      assertNotNull(notUnderstoods);
      assertEquals(3, notUnderstoods.size());
      
      //mark hte headers in "myNs" namespace as understood when retrieving them
      Iterator<Header> myNsHeaders = hdrs.getHeaders("myNs", true);
      notUnderstoods = hdrs.getNotUnderstoodHeaders(null, null, null);
      assertNotNull(notUnderstoods);
      //should be only one header left that is not understood, and it should be the
      //mySecondNs header
      assertEquals(1, notUnderstoods.size());
      QName q = notUnderstoods.iterator().next();
      assertEquals(new QName("mySecondNs", "streamHeader2"), q);
      
      //Now examine the headers we marked as understood and make sure they are the right ones
      int myNsCount = 0;
      while(myNsHeaders.hasNext()) {
          Header h = myNsHeaders.next();
          assertNotNull(h);
          assertTrue(h instanceof SAAJHeader);
          assertEquals("myNs", h.getNamespaceURI());
          assertTrue("Unexpected header local name: " + h.getLocalPart() + 
                  " - must be streamHeader1 or stringHeader1 or stringHeader2",
                  "streamHeader1".equals(h.getLocalPart()) || "stringHeader1".equals(h.getLocalPart())
                          || "stringHeader2".equals(h.getLocalPart()));
          myNsCount++;
      }
      assertEquals(3, myNsCount);
      
      Header h = hdrs.get("myNs", "stringHeader2", false);
      //this should be the non-must understand header, still there
      assertNotNull(h);
    }
    
    public void testGetAllHeaders() throws Exception {
        String soapMsgStr = "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<env:Header>" +
        "<wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"></wsa:Action>" +
        "<wsa:MessageID xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">uuid:40a19d86-071d-4d3f-8b1b-8c8b5245b1de</wsa:MessageID>" +
        "<wsa:RelatesTo xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">uuid:bd2cf21b-d2ad-4dc6-b0ec-2928736b5ae2</wsa:RelatesTo>" +
        "<wsa:To xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">http://www.w3.org/2005/08/addressing/anonymous</wsa:To>" +
        "</env:Header>" +
        "<env:Body xmlns:wsrm11=\"http://docs.oasis-open.org/ws-rx/wsrm/200702\">" +
        "<wsrm11:CreateSequenceResponse>" +
        "<wsrm11:Identifier>35599b13-3672-462a-a51a-31e1820ef236</wsrm11:Identifier>" +
        "<wsrm11:Expires>P1D</wsrm11:Expires>" +
        "<wsrm11:IncompleteSequenceBehavior>NoDiscard</wsrm11:IncompleteSequenceBehavior>" +
        "</wsrm11:CreateSequenceResponse>" +
        "</env:Body></env:Envelope>";
        
        SOAPMessage msg = makeSOAPMessage(soapMsgStr);
        int numHdrs = 0;
        MessageHeaders mh = new SAAJMessageHeaders(msg, SOAPVersion.SOAP_11);
//        assertTrue(mh instanceof SAAJMessageHeaders);
        Iterator<Header> iter = mh.getHeaders();
        while (iter.hasNext()) {
            iter.next();
            numHdrs++;
        }
        assertEquals(4, numHdrs);
    }
    private SOAPMessage makeSOAPMessage(String msg) throws Exception {
        MessageFactory factory = MessageFactory.newInstance();
        SOAPMessage message = factory.createMessage();
        Source src = new StreamSource(new ByteArrayInputStream(msg.getBytes()));
        message.getSOAPPart().setContent(src);
        return message;
//        return new SAAJMessage(message);
    }

}
