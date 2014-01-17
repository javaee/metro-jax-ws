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

package com.sun.xml.ws.message.source;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.api.pipe.StreamSOAPCodec;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

/**
 * Implementation of {@link Message} backed by {@link Source} where the Source
 * represents the complete message such as a SOAP envelope. It uses
 * {@link StreamSOAPCodec} to create a {@link Message} and uses it as a
 * delegate for all the methods.
 *
 * @author Vivek Pandey
 * @author Jitendra Kotamraju
 */
public class ProtocolSourceMessage extends Message {
    private final Message sm;

    public ProtocolSourceMessage(Source source, SOAPVersion soapVersion) {
        XMLStreamReader reader = SourceReaderFactory.createSourceReader(source, true);
        com.sun.xml.ws.api.pipe.StreamSOAPCodec codec = Codecs.createSOAPEnvelopeXmlCodec(soapVersion);
        sm = codec.decode(reader);
    }

    public boolean hasHeaders() {
        return sm.hasHeaders();
    }

    public String getPayloadLocalPart() {
        return sm.getPayloadLocalPart();
    }

    public String getPayloadNamespaceURI() {
        return sm.getPayloadNamespaceURI();
    }

    public boolean hasPayload() {
        return sm.hasPayload();
    }

    public Source readPayloadAsSource() {
        return sm.readPayloadAsSource();
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        return sm.readPayload();
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        sm.writePayloadTo(sw);
    }

    public void writeTo(XMLStreamWriter sw) throws XMLStreamException {
        sm.writeTo(sw);
    }

    public Message copy() {
        return sm.copy().copyFrom(sm);
    }

    public Source readEnvelopeAsSource() {
        return sm.readEnvelopeAsSource();
    }

    public SOAPMessage readAsSOAPMessage() throws SOAPException {
        return sm.readAsSOAPMessage();
    }

    public SOAPMessage readAsSOAPMessage(Packet packet, boolean inbound) throws SOAPException {
        return sm.readAsSOAPMessage(packet, inbound);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        return (T)sm.readPayloadAsJAXB(unmarshaller);
    }
    /** @deprecated */
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        return sm.readPayloadAsJAXB(bridge);
    }
    public <T> T readPayloadAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        return sm.readPayloadAsJAXB(bridge);
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        sm.writeTo(contentHandler, errorHandler);
    }

    public SOAPVersion getSOAPVersion() {
        return sm.getSOAPVersion();
    }

    @Override
    public MessageHeaders getHeaders() {
        return sm.getHeaders();
    }
}
