/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.xml.ws.message.jaxb;

import com.sun.istack.FragmentContentHandler;
import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.message.AbstractMessageImpl;
import com.sun.xml.ws.message.AttachmentSetImpl;
import com.sun.xml.ws.message.RootElementSniffer;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.streaming.XMLStreamWriterUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.MtomStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.util.JAXBResult;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;
import java.io.OutputStream;
import java.util.Map;

/**
 * {@link Message} backed by a JAXB bean.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JAXBMessage extends AbstractMessageImpl {
    private HeaderList headers;

    /**
     * The JAXB object that represents the payload.
     */
    private final Object jaxbObject;
    
    private final AttachmentSet attachmentSet;

    private final Bridge bridge;

    /**
     * Lazily sniffed payload element name
     */
    private String nsUri,localName;

    /**
     * If we have the infoset representation for the payload, this field is non-null.
     */
    private XMLStreamBuffer infoset;

    public static Message create(JAXBRIContext context, Object jaxbObject, SOAPVersion soapVersion, HeaderList headers, AttachmentSet attachments) {
        if(!context.hasSwaRef()) {
            return new JAXBMessage(context,jaxbObject,soapVersion,headers,attachments);
        }

        // If we have swaRef, then that means we might have attachments.
        // to comply with the packet API, we need to eagerly turn the JAXB object into infoset
        // to correctly find out about attachments.

        try {
            MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();

            Marshaller m = context.createMarshaller();
            AttachmentMarshallerImpl am = new AttachmentMarshallerImpl(attachments);
            m.setAttachmentMarshaller(am);
            am.cleanup();
            m.marshal(jaxbObject,xsb.createFromXMLStreamWriter());

            // any way to reuse this XMLStreamBuffer in StreamMessage?
            return new StreamMessage(headers,attachments,xsb.readAsXMLStreamReader(),soapVersion);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }
    /**
     * Creates a {@link Message} backed by a JAXB bean.
     *
     * @param context
     *      The JAXBContext to be used for marshalling.
     * @param jaxbObject
     *      The JAXB object that represents the payload. must not be null. This object
     *      must be bound to an element (which means it either is a {@link JAXBElement} or
     *      an instanceof a class with {@link XmlRootElement}).
     * @param soapVersion
     *      The SOAP version of the message. Must not be null.
     */
    public static Message create(JAXBRIContext context, Object jaxbObject, SOAPVersion soapVersion) {
        return create(context,jaxbObject,soapVersion,null,null);
    }

    private JAXBMessage( JAXBRIContext context, Object jaxbObject, SOAPVersion soapVer, HeaderList headers, AttachmentSet attachments ) {
        super(soapVer);
        this.bridge = new MarshallerBridge(context);
        this.jaxbObject = jaxbObject;
        this.headers = headers;
        this.attachmentSet = attachments;
    }

    /**
     * Creates a {@link Message} backed by a JAXB bean.
     *
     * @param bridge
     *      Specify the payload tag name and how <tt>jaxbObject</tt> is bound.
     * @param jaxbObject
     */
    public static Message create(Bridge bridge, Object jaxbObject, SOAPVersion soapVer) {
        if(!bridge.getContext().hasSwaRef()) {
            return new JAXBMessage(bridge,jaxbObject,soapVer);
        }

        // If we have swaRef, then that means we might have attachments.
        // to comply with the packet API, we need to eagerly turn the JAXB object into infoset
        // to correctly find out about attachments.

        try {
            MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();

            AttachmentSetImpl attachments = new AttachmentSetImpl();
            AttachmentMarshallerImpl am = new AttachmentMarshallerImpl(attachments);
            bridge.marshal(jaxbObject,xsb.createFromXMLStreamWriter(), am);
            am.cleanup();

            // any way to reuse this XMLStreamBuffer in StreamMessage?
            return new StreamMessage(null,attachments,xsb.readAsXMLStreamReader(),soapVer);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    private JAXBMessage(Bridge bridge, Object jaxbObject, SOAPVersion soapVer) {
        super(soapVer);
        // TODO: think about a better way to handle BridgeContext
        this.bridge = bridge;
        this.jaxbObject = jaxbObject;
        QName tagName = bridge.getTypeReference().tagName;
        this.nsUri = tagName.getNamespaceURI();
        this.localName = tagName.getLocalPart();
        this.attachmentSet = new AttachmentSetImpl();
    }

    /**
     * Copy constructor.
     */
    public JAXBMessage(JAXBMessage that) {
        super(that);
        this.headers = that.headers;
        if(this.headers!=null)
            this.headers = new HeaderList(this.headers);
        this.attachmentSet = that.attachmentSet;

        this.jaxbObject = that.jaxbObject;
        this.bridge = that.bridge;
    }
    
    @Override
    public @NotNull AttachmentSet getAttachments() {
        return attachmentSet;
    }

    public boolean hasHeaders() {
        return headers!=null && !headers.isEmpty();
    }

    public HeaderList getHeaders() {
        if(headers==null)
            headers = new HeaderList();
        return headers;
    }

    public String getPayloadLocalPart() {
        if(localName==null)
            sniff();
        return localName;
    }

    public String getPayloadNamespaceURI() {
        if(nsUri==null)
            sniff();
        return nsUri;
    }

    public boolean hasPayload() {
        return true;
    }

    /**
     * Obtains the tag name of the root element.
     */
    private void sniff() {
        RootElementSniffer sniffer = new RootElementSniffer(false);
        try {
            bridge.marshal(jaxbObject,sniffer);
        } catch (JAXBException e) {
            // if it's due to us aborting the processing after the first element,
            // we can safely ignore this exception.
            //
            // if it's due to error in the object, the same error will be reported
            // when the readHeader() method is used, so we don't have to report
            // an error right now.
            nsUri = sniffer.getNsUri();
            localName = sniffer.getLocalName();
        }
    }

    public Source readPayloadAsSource() {
        return new JAXBBridgeSource(bridge,jaxbObject);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        JAXBResult out = new JAXBResult(unmarshaller);
        // since the bridge only produces fragments, we need to fire start/end document.
        try {
            out.getHandler().startDocument();
            bridge.marshal(jaxbObject,out);
            out.getHandler().endDocument();
        } catch (SAXException e) {
            throw new JAXBException(e);
        }
        return (T)out.getResult();
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
       try {
            if(infoset==null) {
                XMLStreamBufferResult sbr = new XMLStreamBufferResult();
                bridge.marshal(jaxbObject,sbr);
                infoset = sbr.getXMLStreamBuffer();
            }
            XMLStreamReader reader = infoset.readAsXMLStreamReader();
            if(reader.getEventType()== START_DOCUMENT)
                XMLStreamReaderUtil.nextElementContent(reader);
            return reader;
        } catch (JAXBException e) {
           // bug 6449684, spec 4.3.4
           throw new WebServiceException(e);
        }
    }

    /**
     * Writes the payload as SAX events.
     */
    protected void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        try {
            if(fragment)
                contentHandler = new FragmentContentHandler(contentHandler);
            AttachmentMarshallerImpl am = new AttachmentMarshallerImpl(attachmentSet);
            bridge.marshal(jaxbObject,contentHandler, am);
            am.cleanup();
        } catch (JAXBException e) {
            // this is really more helpful but spec compliance
            // errorHandler.fatalError(new SAXParseException(e.getMessage(),NULL_LOCATOR,e));
            // bug 6449684, spec 4.3.4
            throw new WebServiceException(e.getMessage(),e);
        }
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        try {
            // MtomCodec sets its own AttachmentMarshaller
            AttachmentMarshaller am = (sw instanceof MtomStreamWriter)
                    ? ((MtomStreamWriter)sw).getAttachmentMarshaller()
                    : new AttachmentMarshallerImpl(attachmentSet);

            // Get output stream and use JAXB UTF-8 writer
            OutputStream os = XMLStreamWriterUtil.getOutputStream(sw);
            if (os != null) {
                bridge.marshal(jaxbObject, os, sw.getNamespaceContext(),am);
            } else {
                bridge.marshal(jaxbObject,sw,am);   
            }
            //cleanup() is not needed since JAXB doesn't keep ref to AttachmentMarshaller
            //am.cleanup();
        } catch (JAXBException e) {
            // bug 6449684, spec 4.3.4
            throw new WebServiceException(e);
        }
    }

    public Message copy() {
        return new JAXBMessage(this);
    }
}
