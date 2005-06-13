/*
 * $Id: DispatchSerializer.java,v 1.7 2005-06-13 15:35:18 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author JAX-RPC RI Development Team
 */

public class DispatchSerializer {

    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private final static int MAX_BUFFER_SIZE = 50;
    private static final DispatchSerializer serializer = new DispatchSerializer();
    private static JAXBTypeSerializer jaxbSerializer;

    public static DispatchSerializer getInstance() {
        return serializer;
    }

    public DispatchSerializer() {
        jaxbSerializer = JAXBTypeSerializer.getInstance();
    }

    public void serialize(Object obj, XMLStreamWriter writer, JAXBContext context) {
        if (obj instanceof Source)
            serializeSource(obj, writer);
        else if (obj instanceof JAXBBeanInfo) {
            Object bean = ((JAXBBeanInfo) obj).getBean();
            jaxbSerializer.serialize(bean, writer, context);
        } else
            throw new WebServiceException("Unable to serialize object type " + obj.getClass().getName());
        //should not happen
    }

    public Object deserialize(XMLStreamReader reader, JAXBContext context) {
        if (context != null)
            return jaxbSerializer.deserialize(reader, context);
        else
            return deserializeSource(reader, context);
    }

    private Object deserializeSource(XMLStreamReader reader, JAXBContext context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

        try {
            while (reader.hasNext()) {
                int state = reader.getEventType();
                switch (state) {
                    case START_ELEMENT:
                        QName name = reader.getName();
                        writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                        Attributes atts = XMLStreamReaderUtil.getAttributes(reader);
                        writer.flush();
                        for (int i = 0; i < atts.getLength(); i++) {
                            if (atts.isNamespaceDeclaration(i)) {
                                String value = atts.getValue(i);
                                String localName = atts.getName(i).getLocalPart();
                                // namespace declaration for the element is written during previous writeElement
                                if (!(name.getPrefix().equals(atts.getName(i).getLocalPart()))) {
                                    writer.setPrefix(localName, value);
                                    writer.writeNamespace(localName, value);
                                } else {
                                    if (name.getPrefix().equals("")) {
                                        writer.setPrefix(localName, value);
                                        writer.writeNamespace(localName, value);
                                    }
                                }
                            } else {
                                writer.writeAttribute(atts.getLocalName(i), atts.getURI(i), atts.getValue(i));
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
                if ((reader.getEventType() == END_ELEMENT) && (reader.getName().equals(SOAPConstants.QNAME_SOAP_BODY)))
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

    private void serializeSource(Object source, XMLStreamWriter writer) {
        try {
            XMLStreamReader reader = SourceReaderFactory.createSourceReader((Source) source, true);

            int state = START_DOCUMENT;
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
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }


    private void displayDOM(Node node, java.io.OutputStream ostream) {
        try {
            System.out.println("\n====\n");
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new javax.xml.transform.dom.DOMSource(node),
                new javax.xml.transform.stream.StreamResult(ostream));
            System.out.println("\n====\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sourceToXMLString(Source result) {
        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }

}
