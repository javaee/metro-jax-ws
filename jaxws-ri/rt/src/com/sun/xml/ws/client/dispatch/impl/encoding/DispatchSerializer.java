/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

/**
 *
 * @author WS Development Team
 */
public final class DispatchSerializer {

    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());

    private final QName bodyTagName;

    /**
     * For SOAP 1.0.
     */
    public static final DispatchSerializer SOAP_1_0 = new DispatchSerializer(SOAPConstants.QNAME_SOAP_BODY);

    /**
     * For SOAP 1.2.
     */
    public static final DispatchSerializer SOAP_1_2 = new DispatchSerializer(SOAP12Constants.QNAME_SOAP_BODY);


    private DispatchSerializer(QName soapBodyTagName) {
        bodyTagName = soapBodyTagName;
    }

    public void serialize(Object obj, XMLStreamWriter writer, JAXBContext context) {
        if (obj instanceof Source)
            serializeSource(obj, writer);
        else if (obj instanceof JAXBBeanInfo) {
            ((JAXBBeanInfo)obj).writeTo(writer);
        } else
            throw new WebServiceException("Unable to serialize object type " + obj.getClass().getName());
        //should not happen
    }

    // TODO: is this method still in use?
    public Source deserializeSource(XMLStreamReader reader) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

        try {
            while (reader.hasNext()) {
                int state = reader.getEventType();
                switch (state) {
                    case START_ELEMENT:
                        QName name = reader.getName();
                        writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                        //fix bug 6285034- namespace getting written 2x- it is now just handled below
                        //with attributes
                        //writer.writeNamespace(name.getPrefix(), name.getNamespaceURI());
                        Attributes atts = XMLStreamReaderUtil.getAttributes(reader);
                        writer.flush();
                        for (int i = 0; i < atts.getLength(); i++) {
                            if (atts.isNamespaceDeclaration(i)) {
                                String value = atts.getValue(i);
                                String localName = atts.getName(i).getLocalPart();
                                writer.setPrefix(localName, value);
                                writer.writeNamespace(localName, value);
                            } else {
                                writer.writeAttribute(atts.getPrefix(i), atts.getURI(i), atts.getLocalName(i),
                                    atts.getValue(i));
                            }
                        }
                        break;
                    case END_ELEMENT:
                        writer.writeEndElement();
                        break;
                    case CHARACTERS:
                        writer.writeCharacters(reader.getText());
                }
                state = XMLStreamReaderUtil.next(reader);
                if ((reader.getEventType() == END_ELEMENT) && (reader.getName().equals(bodyTagName)))
                    break;
            }
            writer.flush();
            writer.close();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }

        ByteArrayInputStream istream =
            new ByteArrayInputStream(baos.toByteArray());
        return new StreamSource(istream);
    }

    void serializeSource(Object source, XMLStreamWriter writer) {
        try {
            XMLStreamReader reader = SourceReaderFactory.createSourceReader((Source) source, true);

            int state;
            do {
                state = XMLStreamReaderUtil.next(reader);
                switch (state) {
                    case START_ELEMENT:
                        QName elementName = reader.getName();
                        String localPart = elementName.getLocalPart();
                        String namespaceURI = elementName.getNamespaceURI();
                        String prefix = elementName.getPrefix();

                        writer.writeStartElement(prefix, localPart, namespaceURI);

                        Attributes atts = XMLStreamReaderUtil.getAttributes(reader);
                        writer.flush();
                        for (int i = 0; i < atts.getLength(); i++) {
                            if (atts.isNamespaceDeclaration(i)) {
                                String value = atts.getValue(i);
                                String localName = atts.getName(i).getLocalPart();
                                writer.setPrefix(localName, value);
                                writer.writeNamespace(localName, value);
                            } else {
                                writer.writeAttribute(atts.getPrefix(i), atts.getURI(i),
                                    atts.getLocalName(i), atts.getValue(i));
                            }
                        }
                        break;
                    case END_ELEMENT:
                        writer.writeEndElement();
                        break;
                    case CHARACTERS:
                        writer.writeCharacters(reader.getText());
                }
            } while (state != END_DOCUMENT);
        } catch (XMLStreamException e) {
            throw new SerializationException(e);
        }
    }


//    private void displayDOM(Node node, java.io.OutputStream ostream) {
//        try {
//            System.out.println("\n====\n");
//            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new javax.xml.transform.dom.DOMSource(node),
//                new javax.xml.transform.stream.StreamResult(ostream));
//            System.out.println("\n====\n");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    private String sourceToXMLString(Source result) {
//        String xmlResult = null;
//        try {
//            TransformerFactory factory = TransformerFactory.newInstance();
//            Transformer transformer = factory.newTransformer();
//            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//            OutputStream out = new ByteArrayOutputStream();
//            StreamResult streamResult = new StreamResult();
//            streamResult.setOutputStream(out);
//            transformer.transform(result, streamResult);
//            xmlResult = streamResult.getOutputStream().toString();
//        } catch (TransformerException e) {
//            e.printStackTrace();
//        }
//        return xmlResult;
//    }

}
