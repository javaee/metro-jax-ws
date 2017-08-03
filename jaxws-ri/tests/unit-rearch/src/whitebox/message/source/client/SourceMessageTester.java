/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.message.source.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.message.source.PayloadSourceMessage;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.xml.XmlUtil;
import junit.framework.TestCase;
import org.w3c.dom.Node;
import whitebox.message.source.types.ValueType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Vivek Padey
 */
public class SourceMessageTester extends TestCase{
    private String soap11Msg = "<Body xmlns=\"urn:test:types\"><value>Its Body</value></Body>";
    private String soap12Msg = "<Body xmlns=\"urn:test:types\">Its Body</Body>";
    private String headers[]={"Header1", "Header2"};



    /**
     * Constructs a test case with the given name.
     */
    public SourceMessageTester(String name) {
        super(name);
    }

    private Message createPayloadSourceMessage() {
        return new PayloadSourceMessage(new StreamSource(new ByteArrayInputStream(soap11Msg.getBytes())), SOAPVersion.SOAP_11);
    }

    public void testSOAP11MessageAsJAXB(){
        try {
            Message msg = createPayloadSourceMessage();
            HeaderList hl = msg.getHeaders();
            JAXBContext ctxt=null;
            Unmarshaller unmarshaller = null;
            try {
                ctxt = JAXBContext.newInstance("whitebox.message.source.types");
                unmarshaller = ctxt.createUnmarshaller();

            } catch (JAXBException e) {
                e.printStackTrace();
                assertTrue(false);
            }
            int i = 0;
            //validate the headers form unmarshalled jaxb bean
            for(Header h:hl){
                assertTrue(validateHeader(h, unmarshaller, headers[i++], SOAPVersion.SOAP_11));
            }

            //validate body
            assertTrue(msg.getPayloadLocalPart().equals("Body") && msg.getPayloadNamespaceURI().equals("urn:test:types"));
            JAXBElement<ValueType> body = msg.readPayloadAsJAXB(unmarshaller);
            assertTrue(body.getValue().getValue().equals("Its Body"));
        } catch (JAXBException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testSOAP11MessageAsXMLStreamReader(){
        //check the body using XMLStreamReader
        Message msg = createPayloadSourceMessage();
        XMLStreamReader reader = null;
        try {
            reader = msg.readPayload();
        } catch (XMLStreamException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);

        QName envName = reader.getName();
        assertTrue(envName.equals(new QName("urn:test:types", "Body")));
        XMLStreamReaderUtil.nextElementContent(reader);

        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        assertTrue(reader.getName().equals(new QName("urn:test:types", "value")));
        XMLStreamReaderUtil.nextContent(reader);
        assertTrue(reader.getText().equals("Its Body"));
    }

    public void testWriteSOAP11MessagePayloadToXMLStreamWriter(){
        Message msg = createPayloadSourceMessage();
        ByteArrayBuffer baos = new ByteArrayBuffer();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
        try {
            msg.writePayloadTo(writer);
            writer.flush();
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(baos.newInputStream());
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            Node n = domResult.getNode().getFirstChild();
            assertTrue(n.getLocalName().equals("Body") && n.getNamespaceURI().equals("urn:test:types"));
            Node c = n.getFirstChild();
            assertTrue(c.getLocalName().equals("value") && c.getNamespaceURI().equals("urn:test:types"));
            assertTrue(c.getFirstChild().getNodeValue().equals("Its Body"));
        } catch (XMLStreamException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (TransformerException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testWriteSOAP11EnvelopeToXMLStreamWriter(){
        Message msg = createPayloadSourceMessage();
        ByteArrayBuffer baos = new ByteArrayBuffer();
        XMLStreamWriter writer = XMLStreamWriterFactory.create(baos);
        try {
            msg.writeTo(writer);
            writer.flush();
//            System.out.println("Envelope: "+new String(baos.toByteArray()));
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(baos.newInputStream());
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            Node n = domResult.getNode().getFirstChild();
            assertTrue(n.getLocalName().equals("Envelope") && n.getNamespaceURI().equals("http://schemas.xmlsoap.org/soap/envelope/"));
            Node c = n.getFirstChild();
            assertTrue(c.getLocalName().equals("Body") && c.getNamespaceURI().equals("http://schemas.xmlsoap.org/soap/envelope/"));
            c = c.getFirstChild();
            assertTrue(c.getLocalName().equals("Body") && c.getNamespaceURI().equals("urn:test:types"));
            c = c.getFirstChild();
            assertTrue(c.getLocalName().equals("value") && c.getNamespaceURI().equals("urn:test:types"));
            assertTrue(c.getFirstChild().getNodeValue().equals("Its Body"));
        } catch (XMLStreamException e) {
            e.printStackTrace();
            assertTrue(false);
        } catch (TransformerException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private boolean validateHeader(Header h, Unmarshaller unm, String localName, SOAPVersion version) {
        if(!h.getLocalPart().equals(localName) || !h.getNamespaceURI().equals("urn:test:types"))
            return false;
        if(!h.getRole(version).equals("http://schemas.xmlsoap.org/soap/actor/next"))
            return false;

        Set<String> roles = new HashSet<String>();
        roles.add("http://schemas.xmlsoap.org/soap/actor/next");
        if(h.getLocalPart().equals("Header2") && h.isIgnorable(version, roles))
            return false;

        if(h.getLocalPart().equals("Header1") && !h.isIgnorable(version, roles))
                return false;
        try {
            JAXBElement<ValueType> header1Value = h.readAsJAXB(unm);
            if(!header1Value.getName().equals(new QName("urn:test:types", localName)))
                return false;

            if(!header1Value.getValue().getValue().equals("Its "+localName))
                return false;
        } catch (JAXBException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
