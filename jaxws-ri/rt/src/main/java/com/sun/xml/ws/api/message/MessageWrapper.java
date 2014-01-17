/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.message;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.spi.db.XMLBridge;

/**
 * A <code>MessageWrapper</code> wraps the Message for the access through Packet.
 * 
 * @author shih-chang.chen@oracle.com
 */
class MessageWrapper extends StreamMessage {
    
    Packet packet;
    Message delegate;
    StreamMessage streamDelegate;
    
    @Override
    public void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        streamDelegate.writePayloadTo(contentHandler, errorHandler, fragment);
    }

    @Override
    public String getBodyPrologue() {
        return streamDelegate.getBodyPrologue();
    }

    @Override
    public String getBodyEpilogue() {
        return streamDelegate.getBodyEpilogue();
    }

    MessageWrapper(Packet p, Message m) {
        super(m.getSOAPVersion());
        packet = p;
        delegate = m;
        streamDelegate = (m instanceof StreamMessage) ? (StreamMessage) m : null; 
        setMessageMedadata(p);
    }  
    
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public boolean hasHeaders() {
        return delegate.hasHeaders();
    }

    @Override
    public AttachmentSet getAttachments() {
        return delegate.getAttachments();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean isOneWay(WSDLPort port) {
        return delegate.isOneWay(port);
    }

    @Override
    public String getPayloadLocalPart() {
        return delegate.getPayloadLocalPart();
    }

    @Override
    public String getPayloadNamespaceURI() {
        return delegate.getPayloadNamespaceURI();
    }

    @Override
    public boolean hasPayload() {
        return delegate.hasPayload();
    }

    @Override
    public boolean isFault() {
        return delegate.isFault();
    }

    @Override
    public QName getFirstDetailEntryName() {
        return delegate.getFirstDetailEntryName();
    }

    @Override
    public Source readEnvelopeAsSource() {
        //TODO if (delegate instanceof SAAJMessage)
        return delegate.readEnvelopeAsSource();
    }

    @Override
    public Source readPayloadAsSource() {
        //TODO if (delegate instanceof SAAJMessage)
        return delegate.readPayloadAsSource();
    }

    @Override
    public SOAPMessage readAsSOAPMessage() throws SOAPException {
        if (!(delegate instanceof SAAJMessage)) {
            delegate = toSAAJ(packet, null);
        }
        return delegate.readAsSOAPMessage();
    }
    
    @Override
    public SOAPMessage readAsSOAPMessage(Packet p, boolean inbound) throws SOAPException {
        if (!(delegate instanceof SAAJMessage)) {
            delegate = toSAAJ(p, inbound);
        }
        return delegate.readAsSOAPMessage();
    }

    @Override
    public Object readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        return delegate.readPayloadAsJAXB(unmarshaller);
    }

    @Override
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        return delegate.readPayloadAsJAXB(bridge);
    }

    @Override
    public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        return delegate.readPayloadAsJAXB(bridge);
    }

    @Override
    public XMLStreamReader readPayload() {
        try {
            return delegate.readPayload();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void consume() {
        delegate.consume();
    }

    @Override
    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        delegate.writePayloadTo(sw);
    }

    @Override
    public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
        delegate.writeTo(sw);
    }

    @Override
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler)
            throws SAXException {
        delegate.writeTo(contentHandler, errorHandler);
    }

    @Override
    public Message copy() {
        return delegate.copy().copyFrom(delegate);
    }

    @Override
    public String getID(WSBinding binding) {
        return delegate.getID(binding);
    }

    @Override
    public String getID(AddressingVersion av, SOAPVersion sv) {
        return delegate.getID(av, sv);
    }

    @Override
    public SOAPVersion getSOAPVersion() {
        return delegate.getSOAPVersion();
    }
    
    @Override
    public @NotNull MessageHeaders getHeaders() {
        return delegate.getHeaders();
    }

    @Override
    public void setMessageMedadata(MessageMetadata metadata) {
        super.setMessageMedadata(metadata);
        delegate.setMessageMedadata(metadata);
    }
}
