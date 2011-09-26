/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.message;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.util.xml.XmlUtil;
import javax.xml.soap.AttachmentPart;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.util.List;
import java.util.Map;

/**
 * Partial {@link Message} implementation.
 *
 * <p>
 * This class implements some of the {@link Message} methods.
 * The idea is that those implementations may be non-optimal but
 * it may save effort in implementing {@link Message} and reduce
 * the code size.
 *
 * <p>
 * {@link Message} classes that are used more commonly should
 * examine carefully which method can be implemented faster,
 * and override them accordingly.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractMessageImpl extends Message {
    /**
     * SOAP version of this message.
     * Used to implement some of the methods, but nothing more than that.
     *
     * <p>
     * So if you aren't using those methods that use this field,
     * this can be null.
     */
    protected final SOAPVersion soapVersion;

    protected AbstractMessageImpl(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
    }

    /**
     * Copy constructor.
     */
    protected AbstractMessageImpl(AbstractMessageImpl that) {
        this.soapVersion = that.soapVersion;
    }

    public Source readEnvelopeAsSource() {
        return new SAXSource(new XMLReaderImpl(this), XMLReaderImpl.THE_SOURCE);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        if(hasAttachments())
            unmarshaller.setAttachmentUnmarshaller(new AttachmentUnmarshallerImpl(getAttachments()));
        try {
            return (T) unmarshaller.unmarshal(readPayloadAsSource());
        } finally{
            unmarshaller.setAttachmentUnmarshaller(null);
        }
    }
    /** @deprecated */
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        return bridge.unmarshal(readPayloadAsSource(),
            hasAttachments()? new AttachmentUnmarshallerImpl(getAttachments()) : null );
    }
    
    public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        return bridge.unmarshal(readPayloadAsSource(),
            hasAttachments()? new AttachmentUnmarshallerImpl(getAttachments()) : null );
    }

    /**
     * Default implementation that relies on {@link #writePayloadTo(XMLStreamWriter)}
     */
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        String soapNsUri = soapVersion.nsUri;
        w.writeStartDocument();
        w.writeStartElement("S","Envelope",soapNsUri);
        w.writeNamespace("S",soapNsUri);
        if(hasHeaders()) {
            w.writeStartElement("S","Header",soapNsUri);
            HeaderList headers = getHeaders();
            int len = headers.size();
            for( int i=0; i<len; i++ ) {
                headers.get(i).writeTo(w);
            }
            w.writeEndElement();
        }
        // write the body
        w.writeStartElement("S","Body",soapNsUri);

        writePayloadTo(w);

        w.writeEndElement();
        w.writeEndElement();
        w.writeEndDocument();
    }

    /**
     * Writes the whole envelope as SAX events.
     */
    public void writeTo( ContentHandler contentHandler, ErrorHandler errorHandler ) throws SAXException {
        String soapNsUri = soapVersion.nsUri;

        contentHandler.setDocumentLocator(NULL_LOCATOR);
        contentHandler.startDocument();
        contentHandler.startPrefixMapping("S",soapNsUri);
        contentHandler.startElement(soapNsUri,"Envelope","S:Envelope",EMPTY_ATTS);
        if(hasHeaders()) {
            contentHandler.startElement(soapNsUri,"Header","S:Header",EMPTY_ATTS);
            HeaderList headers = getHeaders();
            int len = headers.size();
            for( int i=0; i<len; i++ ) {
                // shouldn't JDK be smart enough to use array-style indexing for this foreach!?
                headers.get(i).writeTo(contentHandler,errorHandler);
            }
            contentHandler.endElement(soapNsUri,"Header","S:Header");
        }
        // write the body
        contentHandler.startElement(soapNsUri,"Body","S:Body",EMPTY_ATTS);
        writePayloadTo(contentHandler,errorHandler, true);
        contentHandler.endElement(soapNsUri,"Body","S:Body");
        contentHandler.endElement(soapNsUri,"Envelope","S:Envelope");
    }

    /**
     * Writes the payload to SAX events.
     *
     * @param fragment
     *      if true, this method will fire SAX events without start/endDocument events,
     *      suitable for embedding this into a bigger SAX event sequence.
     *      if false, this method generaets a completely SAX event sequence on its own.
     */
    protected abstract void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException;

    /**
     * Default implementation that uses {@link #writeTo(ContentHandler, ErrorHandler)}
     */
    public SOAPMessage readAsSOAPMessage() throws SOAPException {
        SOAPMessage msg = soapVersion.getMessageFactory().createMessage();

        SAX2DOMEx s2d = new SAX2DOMEx(msg.getSOAPPart());
        try {
            writeTo(s2d, XmlUtil.DRACONIAN_ERROR_HANDLER);
        } catch (SAXException e) {
            throw new SOAPException(e);
        }

        for(Attachment att : getAttachments()) {
            AttachmentPart part = msg.createAttachmentPart();
            part.setDataHandler(att.asDataHandler());
            part.setContentId('<'+att.getContentId()+'>');
            msg.addAttachmentPart(part);


        }
        
        if (msg.saveRequired())
        	msg.saveChanges();
        return msg;
    }

    /**
     *
     */
    public SOAPMessage readAsSOAPMessage(Packet packet, boolean inbound) throws SOAPException {
        SOAPMessage msg = readAsSOAPMessage();
        Map<String, List<String>> headers = null;
        String key = inbound ? Packet.INBOUND_TRANSPORT_HEADERS : Packet.OUTBOUND_TRANSPORT_HEADERS;
        if (packet.supports(key)) {
            headers = (Map<String, List<String>>)packet.get(key);
        }
        if (headers != null) {
            for(Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (!e.getKey().equalsIgnoreCase("Content-Type")) {
                    for(String value : e.getValue()) {
                        msg.getMimeHeaders().addHeader(e.getKey(), value);
                    }
                }
            }
        }
        
        if (msg.saveRequired())
        	msg.saveChanges();
        return msg;
    }


    protected static final AttributesImpl EMPTY_ATTS = new AttributesImpl();
    protected static final LocatorImpl NULL_LOCATOR = new LocatorImpl();
}
