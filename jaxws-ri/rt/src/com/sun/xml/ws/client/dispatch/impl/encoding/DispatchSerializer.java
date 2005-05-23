/*
 * $Id: DispatchSerializer.java,v 1.1 2005-05-23 22:13:11 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.streaming.*;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

import static com.sun.xml.ws.streaming.XMLReader.BOF;
import static com.sun.xml.ws.streaming.XMLReader.CHARS;
import static com.sun.xml.ws.streaming.XMLReader.END;
import static com.sun.xml.ws.streaming.XMLReader.EOF;
import static com.sun.xml.ws.streaming.XMLReader.START;

/**
 * @author JAX-RPC RI Development Team
 */

public class DispatchSerializer {

    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private final static int MAX_BUFFER_SIZE = 50;


    private JAXBTypeSerializer jaxbSerializer =
        new JAXBTypeSerializer();

    public DispatchSerializer() {

    }

    public void serialize(Object obj, XMLWriter writer, JAXBContext context) {

        if (obj instanceof Source)
            serializeSource(obj, writer);
        else if (obj instanceof JAXBBeanInfo) {
            Object bean = ((JAXBBeanInfo) obj).getBean();
            jaxbSerializer.serialize(bean, writer, context);
        } else
            throw new WebServiceException("Unable to serialize object type " + obj.getClass().getName());
        //should not happen
    }

    public Object deserialize(XMLReader reader, JAXBContext context) {

        if (context != null)
            return jaxbSerializer.deserialize(reader, context);
        else
            return deserializeSource(reader, context);
    }

    private Object deserializeSource(XMLReader reader, JAXBContext context) {

        ByteArrayOutputStream baos =
            new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance();
        try {
            XMLWriter writer = factory.createXMLWriter(baos);

            int state = START;
            do {
                switch (state) {
                    case START:
                        QName elementName = reader.getName();
                        String localPart = elementName.getLocalPart();
                        String namespaceURI = elementName.getNamespaceURI();
                        String prefix = elementName.getPrefix();

                        writer.startElement(localPart,
                            namespaceURI,
                            prefix);

                        Attributes atts = reader.getAttributes();
                        writer.flush();
                        for (int i = 0; i < atts.getLength(); i++) {
                            if (atts.isNamespaceDeclaration(i)) {
                                // namespace declaration for the element is written during previous writeElement
                                if (!elementName.getPrefix().equals(atts.getName(i).getLocalPart()))
                                    writer.writeNamespaceDeclaration(atts.getName(i).getLocalPart(), atts.getValue(i));
                            } else {
                                writer.writeAttribute(atts.getLocalName(i), atts.getURI(i), atts.getValue(i));
                            }
                        }

                        break;
                    case END:
                        writer.endElement();
                        break;
                    case CHARS:
                        writer.writeChars(reader.getValue());
                }
                state = reader.next();

                if (SOAPNamespaceConstants.TAG_BODY.equals(reader.getLocalName())
                    && (SOAPNamespaceConstants.ENVELOPE.equals(reader.getURI())))
                    break;

            } while (state != EOF);

            writer.close();

        } catch (Exception e) {
            throw new DeserializationException(new LocalizableExceptionAdapter(e));
        }

        ByteArrayInputStream istream =
            new ByteArrayInputStream(baos.toByteArray());

        return new StreamSource(istream);

    }

    private void serializeSource(Object source, XMLWriter writer) {

        XMLReader reader =
            XMLReaderFactory.newInstance().createXMLReader((Source) source, true);

        int state = BOF;
        do {
            state = reader.next();
            switch (state) {
                case START:
                    QName elementName = reader.getName();
                    String localPart = elementName.getLocalPart();
                    String namespaceURI = elementName.getNamespaceURI();
                    String prefix = elementName.getPrefix();

                    writer.startElement(localPart,
                        namespaceURI,
                        prefix);

                    Attributes atts = reader.getAttributes();
                    writer.flush();
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (atts.isNamespaceDeclaration(i)) {
                            // namespace declaration for the element is written during previous writeElement
                            if (!elementName.getPrefix().equals(atts.getName(i).getLocalPart()))
                                writer.writeNamespaceDeclaration(atts.getName(i).getLocalPart(), atts.getValue(i));
                        } else {
                            writer.writeAttribute(atts.getLocalName(i), atts.getURI(i), atts.getValue(i));
                        }
                    }
                    //writeAttributes(reader.getAttributes(), namespaceURI, prefix, writer);
                    break;
                case END:
                    writer.endElement();
                    break;
                case CHARS:
                    writer.writeChars(reader.getValue());
            }
        } while (state != EOF);
    }
}
