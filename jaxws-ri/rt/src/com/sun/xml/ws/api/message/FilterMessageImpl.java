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

package com.sun.xml.ws.api.message;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

/**
 * A <code>FilterMessageImpl</code> contains some other Message, which it uses
 * as its  basic source of message data, possibly transforming the data along
 * the way or providing  additional functionality.
 *
 * <p>
 * The class <code>FilterMessageImpl</code> itself simply overrides
 * all the methods of <code>Message</code> and invokes them on
 * contained Message delegate. Subclasses of <code>FilterMessageImpl</code>
 * may further override some of  these methods and may also provide
 * additional methods and fields.
 *
 * @author Jitendra Kotamraju
 */
public class FilterMessageImpl extends Message {
    private final Message delegate;

    protected FilterMessageImpl(Message delegate) {
        this.delegate = delegate;
    }

    public boolean hasHeaders() {
        return delegate.hasHeaders();
    }

    public @NotNull HeaderList getHeaders() {
        return delegate.getHeaders();
    }

    public @NotNull AttachmentSet getAttachments() {
        return delegate.getAttachments();
    }

    protected boolean hasAttachments() {
        return delegate.hasAttachments();    
    }

    public boolean isOneWay(@NotNull WSDLPort port) {
        return delegate.isOneWay(port);
    }

    public @Nullable String getPayloadLocalPart() {
        return delegate.getPayloadLocalPart();
    }

    public String getPayloadNamespaceURI() {
        return delegate.getPayloadNamespaceURI();
    }

    public boolean hasPayload() {
        return delegate.hasPayload();
    }

    public boolean isFault() {
        return delegate.isFault();
    }

    public @Nullable QName getFirstDetailEntryName() {
        return delegate.getFirstDetailEntryName();
    }

    public Source readEnvelopeAsSource() {
        return delegate.readEnvelopeAsSource();
    }

    public Source readPayloadAsSource() {
        return delegate.readPayloadAsSource();
    }

    public SOAPMessage readAsSOAPMessage() throws SOAPException {
        return delegate.readAsSOAPMessage();
    }

    public SOAPMessage readAsSOAPMessage(Packet packet, boolean inbound) throws SOAPException {
        return delegate.readAsSOAPMessage(packet, inbound);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        return (T)delegate.readPayloadAsJAXB(unmarshaller);
    }

    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        return delegate.readPayloadAsJAXB(bridge);
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        return delegate.readPayload();
    }

    public void consume() {
        delegate.consume();
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        delegate.writePayloadTo(sw);
    }

    public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
        delegate.writeTo(sw);
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        delegate.writeTo(contentHandler, errorHandler);
    }

    public Message copy() {
        return delegate.copy();
    }

    public @NotNull String getID(@NotNull WSBinding binding) {
        return delegate.getID(binding);
    }

    public @NotNull String getID(AddressingVersion av, SOAPVersion sv) {
        return delegate.getID(av, sv);
    }
}
