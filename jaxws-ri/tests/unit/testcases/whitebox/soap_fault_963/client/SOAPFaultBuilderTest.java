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

package whitebox.soap_fault_963.client;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import com.sun.xml.messaging.saaj.soap.ver1_1.Detail1_1Impl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import junit.framework.TestCase;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.StringWriter;
import java.lang.reflect.Field;


/**
 * @author Adam Lee
 */
public class SOAPFaultBuilderTest extends TestCase {

  private static final QName DETAIL1_QNAME = new QName("http://www.example1.com/faults", "myFirstDetail");
  private static final SOAPFault FAULT_11;

  static {
    SOAPFault fault11 = null;
    try {
      fault11 = createFault(SOAPVersion.SOAP_11);
    } catch (Exception e) {
      // falls through
    }
    FAULT_11 = fault11;
  }

  private static SOAPFault createFault(SOAPVersion soapVersion) throws Exception {
    SOAPFactory fac = soapVersion.saajSoapFactory;
    SOAPFault sf = fac.createFault("This is a fault.", soapVersion.faultCodeClient);
    Detail d = sf.addDetail();
    // inject null value to Detail element's namespace uri
    injectNullNamespaceURI(d);
    SOAPElement de = d.addChildElement(DETAIL1_QNAME);
    de.addAttribute(new QName("", "msg1"), "This is the first detail message.");
    return sf;
  }

  /**
   * Because current SAAJ have no any API to set namespace URI with null value.
   * It will convert null value to empty string for namespace URI while constructing or getting it.
   * So we have to inject it with reflection API
   * @param d detail element
   */
  private static void injectNullNamespaceURI(Detail d) {
    if (d instanceof ElementNSImpl) {
      ElementNSImpl detail = (ElementNSImpl) d;
      try {
        Field namespaceURIField = ElementNSImpl.class.getDeclaredField("namespaceURI");
        namespaceURIField.setAccessible(true);
        namespaceURIField.set(detail, null);
      } catch (NoSuchFieldException e) {
        // not continue to inject value
        e.printStackTrace();
        return;
      } catch (IllegalAccessException e) {
        // not continue to inject value
        e.printStackTrace();
        return;
      }
    }
  }

  public void testCreate11FaultFromSFE() throws Exception {
    SOAPFaultException sfe = new SOAPFaultException(FAULT_11);
    Message msg = SOAPFaultBuilder.createSOAPFaultMessage(SOAPVersion.SOAP_11, sfe, SOAPVersion.SOAP_11.faultCodeMustUnderstand);
  }

}
