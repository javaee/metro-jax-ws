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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.MessageHeaders;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

/**
 * {@link Message} that has no body.
 * 
 * @author Kohsuke Kawaguchi
 */
public class EmptyMessageImpl extends AbstractMessageImpl {

    /**
     * If a message has no payload, it's more likely to have
     * some header, so we create it eagerly here.
     */
    private final MessageHeaders headers;
    private final AttachmentSet attachmentSet;

    public EmptyMessageImpl(SOAPVersion version) {
        super(version);
        this.headers = new HeaderList(version);
        this.attachmentSet = new AttachmentSetImpl();
    }

    public EmptyMessageImpl(MessageHeaders headers, @NotNull AttachmentSet attachmentSet, SOAPVersion version){
        super(version);
        if(headers==null)
            headers = new HeaderList(version);
        this.attachmentSet = attachmentSet;
        this.headers = headers;
    }

    /**
     * Copy constructor.
     */
    private EmptyMessageImpl(EmptyMessageImpl that) {
        super(that);
        this.headers = new HeaderList(that.headers);
        this.attachmentSet = that.attachmentSet;
        this.copyFrom(that);
    }

    public boolean hasHeaders() {
        return headers.hasHeaders();
    }
    
    public MessageHeaders getHeaders() {
        return headers;
    }

    public String getPayloadLocalPart() {
        return null;
    }

    public String getPayloadNamespaceURI() {
        return null;
    }

    public boolean hasPayload() {
        return false;
    }

    public Source readPayloadAsSource() {
        return null;
    }

    public XMLStreamReader readPayload() throws XMLStreamException {
        return null;
    }

    public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
        // noop
    }

    public void writePayloadTo(ContentHandler contentHandler, ErrorHandler errorHandler, boolean fragment) throws SAXException {
        // noop
    }

    public Message copy() {
        return new EmptyMessageImpl(this).copyFrom(this);
    }

}
