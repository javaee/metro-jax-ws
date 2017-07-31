/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

import com.sun.istack.FragmentContentHandler;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.MessageHeaders;
import com.sun.xml.ws.streaming.DOMStreamReader;
import com.sun.xml.ws.util.DOMUtil;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;

/**
 * {@link Message} backed by a DOM {@link Element} that represents the payload.
 *
 * @author Kohsuke Kawaguchi
 */
public final class DOMMessage extends AbstractMessageImpl {
    private MessageHeaders headers;
    private final Element payload;

    public DOMMessage(SOAPVersion ver, Element payload) {
        this(ver,null,payload);
    }

    public DOMMessage(SOAPVersion ver, MessageHeaders headers, Element payload) {
        this(ver,headers,payload,null);        
    }

    public DOMMessage(SOAPVersion ver, MessageHeaders headers, Element payload, AttachmentSet attachments) {
        super(ver);
        this.headers = headers;
        this.payload = payload;
        this.attachmentSet = attachments;
        assert payload!=null;
    }
    /**
     * This constructor is a convenience and called by the {@link #copy}
     */
    private DOMMessage(DOMMessage that) {
        super(that);
        this.headers = HeaderList.copy(that.headers);
        this.payload = that.payload;
        this.copyFrom(that);
    }

    public boolean hasHeaders() {
        return getHeaders().hasHeaders();
    }

    public MessageHeaders getHeaders() {
        if (headers == null)
            headers = new HeaderList(getSOAPVersion());

        return headers;
    }

    public String getPayloadLocalPart() {
        return payload.getLocalName();
    }

    public String getPayloadNamespaceURI() {
        return payload.getNamespaceURI();
    }

    public boolean hasPayload() {
        return true;
    }

    public Source readPayloadAsSource() {
        return new DOMSource(payload);
    }

    public <T> T readPayloadAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        if(hasAttachments())
            unmarshaller.setAttachmentUnmarshaller(new AttachmentUnmarshallerImpl(getAttachments()));
        try {
            return (T)unmarshaller.unmarshal(payload);
        } finally{
            unmarshaller.setAttachmentUnmarshaller(null);
        }
    }
    /** @deprecated */
    public <T> T readPayloadAsJAXB(Bridge<T> bridge) throws JAXBException {
        return bridge.unmarshal(payload,
            hasAttachments()? new AttachmentUnmarshallerImpl(getAttachments()) : null);
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        DOMStreamReader dss = new DOMStreamReader();
        dss.setCurrentNode(payload);
        dss.nextTag();
        assert dss.getEventType()==XMLStreamReader.START_ELEMENT;
        return dss;
    }

    public void writePayloadTo(XMLStreamWriter sw) {
        try {
            if (payload != null)
                DOMUtil.serializeNode(payload, sw);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    protected void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        if(fragment)
            contentHandler = new FragmentContentHandler(contentHandler);
        DOMScanner ds = new DOMScanner();
        ds.setContentHandler(contentHandler);
        ds.scan(payload);
    }

    public Message copy() {
        return new DOMMessage(this).copyFrom(this);
    }

}
