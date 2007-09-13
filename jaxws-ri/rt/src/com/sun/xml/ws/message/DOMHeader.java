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

package com.sun.xml.ws.message;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.ws.streaming.DOMStreamReader;
import com.sun.xml.ws.util.DOMUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link com.sun.xml.ws.api.message.Header} implementation for a DOM.
 *
 * @author Kohsuke Kawaguchi
 */
public class DOMHeader<N extends Element> extends AbstractHeaderImpl {
    protected final N node;

    private final String nsUri;
    private final String localName;

    public DOMHeader(N node) {
        assert node!=null;
        this.node = node;

        this.nsUri = fixNull(node.getNamespaceURI());
        this.localName = node.getLocalName();
    }


    public String getNamespaceURI() {
        return nsUri;
    }

    public String getLocalPart() {
        return localName;
    }

    public XMLStreamReader readHeader() throws XMLStreamException {
        DOMStreamReader r = new DOMStreamReader(node);
        r.nextTag();    // move ahead to the start tag
        return r;
    }

    public <T> T readAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        return (T) unmarshaller.unmarshal(node);
    }

    public <T> T readAsJAXB(Bridge<T> bridge) throws JAXBException {
        return bridge.unmarshal(node);
    }

    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        DOMUtil.serializeNode(node, w);
    }

    private static String fixNull(String s) {
        if(s!=null)     return s;
        else            return "";
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        DOMScanner ds = new DOMScanner();
        ds.setContentHandler(contentHandler);
        ds.scan(node);
    }

    public String getAttribute(String nsUri, String localName) {
        if(nsUri.length()==0)   nsUri=null; // DOM wants null, not "".
        return node.getAttributeNS(nsUri,localName);
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        SOAPHeader header = saaj.getSOAPHeader();
        Node clone = header.getOwnerDocument().importNode(node,true);
        header.appendChild(clone);
    }

    @Override
    public String getStringContent() {
        return node.getTextContent();
    }
}
