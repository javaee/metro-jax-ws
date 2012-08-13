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

package com.sun.xml.ws.api.addressing;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.message.AbstractHeaderImpl;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;

/**
 * Used to represent outbound endpoint reference header,
 * such as &lt;ReplyTo> and &lt;FaultTo>.
 * Used from {@link WSEndpointReference}.
 *
 * @author Kohsuke Kawaguchi
 * @see WSEndpointReference
 */
final class EPRHeader extends AbstractHeaderImpl {

    private final String nsUri,localName;
    private final WSEndpointReference epr;

    EPRHeader(QName tagName, WSEndpointReference epr) {
        this.nsUri = tagName.getNamespaceURI();
        this.localName = tagName.getLocalPart();
        this.epr = epr;
    }

    public @NotNull String getNamespaceURI() {
        return nsUri;
    }

    public @NotNull String getLocalPart() {
        return localName;
    }

    @Nullable
    public String getAttribute(@NotNull String nsUri, @NotNull String localName) {
        try {
            XMLStreamReader sr = epr.read("EndpointReference"/*doesn't matter*/);
            while(sr.getEventType()!= XMLStreamConstants.START_ELEMENT)
                sr.next();

            return sr.getAttributeValue(nsUri,localName);
        } catch (XMLStreamException e) {
            // since we are reading from buffer, this can't happen.
            throw new AssertionError(e);
        }
    }

    public XMLStreamReader readHeader() throws XMLStreamException {
        return epr.read(localName);
    }

    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        epr.writeTo(localName, w);
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        try {
            // TODO what about in-scope namespaces
            // Not very efficient consider implementing a stream buffer
            // processor that produces a DOM node from the buffer.
            Transformer t = XmlUtil.newTransformer();
            SOAPHeader header = saaj.getSOAPHeader();
            if (header == null)
                header = saaj.getSOAPPart().getEnvelope().addHeader();
            t.transform(epr.asSource(localName), new DOMResult(header));
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        epr.writeTo(localName,contentHandler,errorHandler,true);
    }
}
