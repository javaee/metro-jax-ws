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

package com.sun.xml.ws.message.stream;

import com.sun.istack.FinalArrayList;
import com.sun.istack.NotNull;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferSource;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.message.AbstractHeaderImpl;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import java.util.List;
import java.util.Set;

/**
 * {@link Header} whose physical data representation is an XMLStreamBuffer.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class StreamHeader extends AbstractHeaderImpl {
    protected final XMLStreamBuffer _mark;

    protected boolean _isMustUnderstand;

    /**
     * Role or actor value.
     */
    protected @NotNull String _role;

    protected boolean _isRelay;

    protected String _localName;

    protected String _namespaceURI;

    /**
     * Keep the information about an attribute on the header element.
     *
     * TODO: this whole attribute handling could be done better, I think.
     */
    protected static final class Attribute {
        /**
         * Can be empty but never null.
         */
        final String nsUri;
        final String localName;
        final String value;

        public Attribute(String nsUri, String localName, String value) {
            this.nsUri = fixNull(nsUri);
            this.localName = localName;
            this.value = value;
        }
    }

    /**
     * The attributes on the header element.
     * We expect there to be only a small number of them,
     * so the use of {@link List} would be justified.
     *
     * Null if no attribute is present.
     */
    private final FinalArrayList<Attribute> attributes;

    /**
     * Creates a {@link StreamHeader}.
     *
     * @param reader
     *      The parser pointing at the start of the mark.
     *      Technically this information is redundant,
     *      but it achieves a better performance.
     * @param mark
     *      The start of the buffered header content.
     */
    protected StreamHeader(XMLStreamReader reader, XMLStreamBuffer mark) {
        assert reader!=null && mark!=null;
        _mark = mark;
        _localName = reader.getLocalName();
        _namespaceURI = reader.getNamespaceURI();
        attributes = processHeaderAttributes(reader);
    }

    /**
     * Creates a {@link StreamHeader}.
     *
     * @param reader
     *      The parser that points to the start tag of the header.
     *      By the end of this method, the parser will point at
     *      the end tag of this element.
     */
    protected StreamHeader(XMLStreamReader reader) throws XMLStreamException {
        _localName = reader.getLocalName();
        _namespaceURI = reader.getNamespaceURI();
        attributes = processHeaderAttributes(reader);
        // cache the body
        _mark = XMLStreamBuffer.createNewBufferFromXMLStreamReader(reader);
    }

    public final boolean isIgnorable(@NotNull SOAPVersion soapVersion, @NotNull Set<String> roles) {
        // check mustUnderstand
        if(!_isMustUnderstand) return true;

        if (roles == null)
            return true;

        // now role
        return !roles.contains(_role);
    }

    public @NotNull String getRole(@NotNull SOAPVersion soapVersion) {
        assert _role!=null;
        return _role;
    }

    public boolean isRelay() {
        return _isRelay;
    }

    public @NotNull String getNamespaceURI() {
        return _namespaceURI;
    }

    public @NotNull String getLocalPart() {
        return _localName;
    }

    public String getAttribute(String nsUri, String localName) {
        if(attributes!=null) {
            for(int i=attributes.size()-1; i>=0; i-- ) {
                Attribute a = attributes.get(i);
                if(a.localName.equals(localName) && a.nsUri.equals(nsUri))
                    return a.value;
            }
        }
        return null;
    }

    /**
     * Reads the header as a {@link XMLStreamReader}
     */
    public XMLStreamReader readHeader() throws XMLStreamException {
        return _mark.readAsXMLStreamReader();
    }

    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        if(_mark.getInscopeNamespaces().size() > 0)
            _mark.writeToXMLStreamWriter(w,true);
        else
            _mark.writeToXMLStreamWriter(w);
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        try {
            // TODO what about in-scope namespaces
            // Not very efficient consider implementing a stream buffer
            // processor that produces a DOM node from the buffer.
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            XMLStreamBufferSource source = new XMLStreamBufferSource(_mark);
            DOMResult result = new DOMResult();
            t.transform(source, result);
            Node d = result.getNode();
            if(d.getNodeType() == Node.DOCUMENT_NODE)
                d = d.getFirstChild();
            SOAPHeader header = saaj.getSOAPHeader();
            if(header == null)
                header = saaj.getSOAPPart().getEnvelope().addHeader();
            Node node = header.getOwnerDocument().importNode(d, true);
            header.appendChild(node);
        } catch (Exception e) {
            throw new SOAPException(e);
        }
    }

    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        _mark.writeTo(contentHandler);
    }

    /**
     * Creates an EPR without copying infoset.
     *
     * This is the most common implementation on which {@link Header#readAsEPR(AddressingVersion)}
     * is invoked on.
     */
    @Override @NotNull
    public WSEndpointReference readAsEPR(AddressingVersion expected) throws XMLStreamException {
        return new WSEndpointReference(_mark,expected);
    }

    protected abstract FinalArrayList<Attribute> processHeaderAttributes(XMLStreamReader reader);

    /**
     * Convert null to "".
     */
    private static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }
}
