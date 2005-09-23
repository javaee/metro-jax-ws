/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author WS Development Team
 */

public class Dispatch12Serializer implements SerializerIF{

    private static final Logger logger =
            Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private final static int MAX_BUFFER_SIZE = 50;

    private static JAXBTypeSerializer jaxbSerializer = JAXBTypeSerializer.getInstance();
    private static final Dispatch12Serializer serializer = new Dispatch12Serializer();

    public static Dispatch12Serializer getInstance() {
        return serializer;
    }
    public Dispatch12Serializer() {
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
                if ((reader.getEventType() == END_ELEMENT) && (reader.getName().equals(SOAP12Constants.QNAME_SOAP_BODY)))
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
            } while (state != END_DOCUMENT);
        }
        catch (XMLStreamException e) {
            throw new SerializationException(e);
        }
    }
}
