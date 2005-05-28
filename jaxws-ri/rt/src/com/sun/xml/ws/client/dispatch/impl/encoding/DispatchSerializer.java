/*
 * $Id: DispatchSerializer.java,v 1.3 2005-05-28 01:10:10 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author JAX-RPC RI Development Team
 */

public class DispatchSerializer {
    
    private static final Logger logger =
            Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private final static int MAX_BUFFER_SIZE = 50;
    
    private JAXBTypeSerializer jaxbSerializer = new JAXBTypeSerializer();
    
    public DispatchSerializer() {
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
        try {
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
            
            int state = START_ELEMENT;
            do {
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
                                // namespace declaration for the element is written during previous writeElement
                                if (!elementName.getPrefix().equals(atts.getName(i).getLocalPart())) {
                                    String value = atts.getValue(i);
                                    String localName = atts.getName(i).getLocalPart();
                                    writer.setPrefix(localName, value);
                                    writer.writeNamespace(localName, value);
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
                
                if (SOAPNamespaceConstants.TAG_BODY.equals(reader.getLocalName())
                && (SOAPNamespaceConstants.ENVELOPE.equals(reader.getNamespaceURI())))
                    break;
                
            } while (state != END_DOCUMENT);
            
            writer.writeEndDocument();
            writer.close();
            
        } catch (Exception e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(e));
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
                                // namespace declaration for the element is written during previous writeElement
                                if (!elementName.getPrefix().equals(atts.getName(i).getLocalPart())) {
                                    String value = atts.getValue(i);
                                    String localName = atts.getName(i).getLocalPart();
                                    writer.setPrefix(localName, value);
                                    writer.writeNamespace(localName, value);
                                }
                            } else {
                                writer.writeAttribute(atts.getLocalName(i), atts.getURI(i), atts.getValue(i));
                            }
                        }
                        //writeAttributes(reader.getAttributes(), namespaceURI, prefix, writer);
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
            throw new SerializationException(new LocalizableExceptionAdapter(e));           
        }
    }
}
