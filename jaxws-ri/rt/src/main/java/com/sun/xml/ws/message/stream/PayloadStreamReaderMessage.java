/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.message.stream;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.message.AbstractMessageImpl;
import com.sun.xml.ws.message.AttachmentSetImpl;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

/**
 * {@link Message} backed by {@link XMLStreamReader} as payload
 *
 * @author Jitendra Kotamraju
 */
public class PayloadStreamReaderMessage extends AbstractMessageImpl {
    private final StreamMessage message;

    public PayloadStreamReaderMessage(XMLStreamReader reader, SOAPVersion soapVer) {
        this(null, reader,new AttachmentSetImpl(), soapVer);
    }

    public PayloadStreamReaderMessage(@Nullable HeaderList headers, @NotNull XMLStreamReader reader,
                                      @NotNull AttachmentSet attSet, @NotNull SOAPVersion soapVersion) {
        super(soapVersion);
        message = new StreamMessage(headers, attSet, reader, soapVersion);
    }
    
    public boolean hasHeaders() {
        return message.hasHeaders();
    }

    /**
     * @deprecated - use getMessageHeaders() instead
     */
    public HeaderList getHeaders() {
        return message.getHeaders();
    }

    public AttachmentSet getAttachments() {
        return message.getAttachments();
    }

    public String getPayloadLocalPart() {
        return message.getPayloadLocalPart();
    }

    public String getPayloadNamespaceURI() {
        return message.getPayloadNamespaceURI();
    }

    public boolean hasPayload() {
        return true;
    }

    public Source readPayloadAsSource() {
        return message.readPayloadAsSource();
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        return message.readPayload();
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        message.writePayloadTo(sw);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        return (T) message.readPayloadAsJAXB(unmarshaller);
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        message.writeTo(contentHandler, errorHandler);
    }

    protected void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        message.writePayloadTo(contentHandler, errorHandler, fragment);
    }

    public Message copy() {
        return message.copy();
    }
    
    @Override
    public @NotNull MessageHeaders getMessageHeaders() {
        return message.getMessageHeaders();
    }
}
