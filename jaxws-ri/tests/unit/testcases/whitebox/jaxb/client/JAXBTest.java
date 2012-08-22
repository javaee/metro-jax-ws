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

package whitebox.jaxb.client;

import junit.framework.TestCase;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.ws.streaming.XMLStreamWriterUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.stream.buffer.XMLStreamBuffer;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.OutputStream;

/**
 * Tests for JAXB.
 *
 * @author Rama Pulavarthi
 */
public class JAXBTest extends TestCase {

    public void testJAXBElementMarshalling() throws Exception {
        JAXBRIContext jc = (JAXBRIContext) JAXBContext.newInstance(whitebox.jaxb.client.DetailType.class);
        DetailType dt = new DetailType();
        SOAPFault sf = createFault();
        dt.getDetails().add(sf.getDetail());


        Marshaller m = jc.createMarshaller();
        m.marshal(dt, System.out);
        XMLStreamBufferResult sbr = new XMLStreamBufferResult();
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);
        m.marshal(dt, sbr);
        XMLStreamBuffer infoset = sbr.getXMLStreamBuffer();
        XMLStreamReader reader = infoset.readAsXMLStreamReader();
        if (reader.getEventType() == START_DOCUMENT)
            XMLStreamReaderUtil.nextElementContent(reader);
        verifyDetail(reader);

    }

    private void verifyDetail(XMLStreamReader rdr) throws Exception {
        boolean detail = false;
        while (rdr.hasNext()) {
            int event = rdr.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (rdr.getName().getLocalPart().equals("detail") || rdr.getName().getLocalPart().equals("Detail")) {
                    detail = true;
                    XMLStreamReaderUtil.nextElementContent(rdr);    // <myFirstDetail>
                    assertEquals(DETAIL1_QNAME, rdr.getName());
                    XMLStreamReaderUtil.nextElementContent(rdr);    // </myFirstDetail>                
                }
            }
        }
        if (!detail) {
            fail("There is no detail element in the fault");
        }
    }

    private static final QName DETAIL1_QNAME = new QName("http://www.example1.com/faults", "myFirstDetail");

    private static SOAPFault createFault() throws Exception {
        SOAPFactory fac = SOAPFactory.newInstance();
        SOAPFault sf = fac.createFault("This is a fault.", new QName("http://schemas.xmlsoap.org/wsdl/soap/http", "Client"));
        Detail d = sf.addDetail();
        SOAPElement de = d.addChildElement(DETAIL1_QNAME);
        de.addAttribute(new QName("", "msg1"), "This is the first detail message.");
        return sf;
    }


}
