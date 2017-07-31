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

package com.sun.xml.ws.api.addressing;

import com.sun.istack.FinalArrayList;
import com.sun.istack.NotNull;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.message.AbstractHeaderImpl;
import com.sun.xml.ws.util.xml.XMLStreamWriterFilter;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPHeader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.ws.WebServiceException;

/**
 * Used to represent outbound header created from {@link WSEndpointReference}'s
 * referenec parameters.
 *
 * <p>
 * This is optimized for outbound use, so it implements some of the methods lazily,
 * in a slow way.
 *
 * <p>
 * This header adds "wsa:IsReferenceParameter" and thus only used for the W3C version.
 *
 * @author Kohsuke Kawaguchi
 */
final class OutboundReferenceParameterHeader extends AbstractHeaderImpl {
    private final XMLStreamBuffer infoset;
    private final String nsUri,localName;

    /**
     * The attributes on the header element.
     * Lazily parsed.
     * Null if not parsed yet.
     */
    private FinalArrayList<Attribute> attributes;

    OutboundReferenceParameterHeader(XMLStreamBuffer infoset, String nsUri, String localName) {
        this.infoset = infoset;
        this.nsUri = nsUri;
        this.localName = localName;
    }

    @Override
    public @NotNull String getNamespaceURI() {
        return nsUri;
    }

    @Override
    public @NotNull String getLocalPart() {
        return localName;
    }

    @Override
    public String getAttribute(String nsUri, String localName) {
        if(attributes==null) {
            parseAttributes();
        }
        for(int i=attributes.size()-1; i>=0; i-- ) {
            Attribute a = attributes.get(i);
            if (a.localName.equals(localName) && a.nsUri.equals(nsUri)) {
                return a.value;
            }
        }
        return null;
    }

    /**
     * We don't really expect this to be used, but just to satisfy
     * the {@link Header} contract.
     *
     * So this is rather slow.
     */
    private void parseAttributes() {
        try {
            XMLStreamReader reader = readHeader();
            reader.nextTag();   // move to the first element, which is the header element

            attributes = new FinalArrayList<Attribute>();
            boolean refParamAttrWritten = false;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                final String attrLocalName = reader.getAttributeLocalName(i);
                final String namespaceURI = reader.getAttributeNamespace(i);
                final String value = reader.getAttributeValue(i);
                if (namespaceURI.equals(AddressingVersion.W3C.nsUri)&& attrLocalName.equals("IS_REFERENCE_PARAMETER")) {
                    refParamAttrWritten = true;
                }
                attributes.add(new Attribute(namespaceURI,attrLocalName,value));
            }
            // we are adding one more attribute "wsa:IsReferenceParameter", if its not alrady there
            if (!refParamAttrWritten) {
                attributes.add(new Attribute(AddressingVersion.W3C.nsUri,IS_REFERENCE_PARAMETER,TRUE_VALUE));
            }
        } catch (XMLStreamException e) {
            throw new WebServiceException("Unable to read the attributes for {"+nsUri+"}"+localName+" header",e);
        }
    }

    @Override
    public XMLStreamReader readHeader() throws XMLStreamException {
        return new StreamReaderDelegate(infoset.readAsXMLStreamReader()) {
            int state=0; /* 0:expecting root, 1:in root, 2:past root */
            @Override
            public int next() throws XMLStreamException {
                return check(super.next());
            }

            @Override
            public int nextTag() throws XMLStreamException {
                return check(super.nextTag());
            }

            private int check(int type) {
                switch (state) {
                    case 0:
                        if (type == START_ELEMENT) {
                            state = 1;
                        }
                        break;
                    case 1:
                        state = 2;
                        break;
                    default: 
                        break;
                }

                return type;
            }

            @Override
            public int getAttributeCount() {
                if (state == 1) {
                    return super.getAttributeCount()+1;
                } else {
                    return super.getAttributeCount();
                }
            }

            @Override
            public String getAttributeLocalName(int index) {
                if (state == 1 && index == super.getAttributeCount()) {
                    return IS_REFERENCE_PARAMETER;
                } else {
                    return super.getAttributeLocalName(index);
                }
            }

            @Override
            public String getAttributeNamespace(int index) {
                if (state == 1 && index==super.getAttributeCount()) {
                    return AddressingVersion.W3C.nsUri;
                }
                else {
                    return super.getAttributeNamespace(index);
                }
            }

            @Override
            public String getAttributePrefix(int index) {
                if(state==1 && index==super.getAttributeCount()) {
                    return "wsa";
                } else {
                    return super.getAttributePrefix(index);
                }
            }

            @Override
            public String getAttributeType(int index) {
                if(state==1 && index==super.getAttributeCount()) {
                    return "CDATA";
                } else {
                    return super.getAttributeType(index);
                }
            }

            @Override
            public String getAttributeValue(int index) {
                if(state==1 && index==super.getAttributeCount()) {
                    return TRUE_VALUE;
                } else {
                    return super.getAttributeValue(index);
                }
            }

            @Override
            public QName getAttributeName(int index) {
                if(state==1 && index==super.getAttributeCount()) {
                    return new QName(AddressingVersion.W3C.nsUri, IS_REFERENCE_PARAMETER, "wsa");
                } else {
                    return super.getAttributeName(index);
                }
            }

            @Override
            public String getAttributeValue(String namespaceUri, String localName) {
                if(state==1 && localName.equals(IS_REFERENCE_PARAMETER) && namespaceUri.equals(AddressingVersion.W3C.nsUri)) {
                    return TRUE_VALUE;
                } else {
                    return super.getAttributeValue(namespaceUri, localName);
                }
            }
        };
    }

    @Override
    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        infoset.writeToXMLStreamWriter(new XMLStreamWriterFilter(w) {
            private boolean root=true;
            private boolean onRootEl = true;
            @Override
            public void writeStartElement(String localName) throws XMLStreamException {
                super.writeStartElement(localName);
                writeAddedAttribute();
            }

            private void writeAddedAttribute() throws XMLStreamException {
                if(!root) {
                    onRootEl = false;
                    return;
                }
                root=false;
                writeNamespace("wsa",AddressingVersion.W3C.nsUri);
                super.writeAttribute("wsa",AddressingVersion.W3C.nsUri,IS_REFERENCE_PARAMETER,TRUE_VALUE);
            }

            @Override
            public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
                super.writeStartElement(namespaceURI, localName);
                writeAddedAttribute();
            }

            @Override
            public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                //TODO: Verify with KK later
                //check if prefix is declared before writing start element.
                boolean prefixDeclared = isPrefixDeclared(prefix,namespaceURI);
                super.writeStartElement(prefix, localName, namespaceURI);
                if (!prefixDeclared && !prefix.equals("")) {
                    super.writeNamespace(prefix,namespaceURI);
                }
                writeAddedAttribute();
            }
            @Override
            public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException{
                if (!isPrefixDeclared(prefix, namespaceURI)) {
                    super.writeNamespace(prefix,namespaceURI);
                }
            }

            @Override
            public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
                //skip if its on root element and attribute is wsa;IsReferenceParameter, as we write it.
                if(onRootEl && namespaceURI.equals(AddressingVersion.W3C.nsUri)
                            && localName.equals(IS_REFERENCE_PARAMETER)) {
                    return;
                }
                writer.writeAttribute(prefix, namespaceURI, localName, value);
            }
            
            @Override
            public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
                writer.writeAttribute(namespaceURI, localName, value);
            }
            private boolean isPrefixDeclared(String prefix, String namespaceURI ) {
                return namespaceURI.equals(getNamespaceContext().getNamespaceURI(prefix));
            }
        },true);
    }

    @Override
    public void writeTo(SOAPMessage saaj) throws SOAPException {
        //TODO: SAAJ returns null instead of throwing SOAPException,
        // when there is no SOAPHeader in the message,
        // which leads to NPE.
        try {
            SOAPHeader header = saaj.getSOAPHeader();
            if (header == null) {
                header = saaj.getSOAPPart().getEnvelope().addHeader();
            }
            Element node = (Element)infoset.writeTo(header);
            node.setAttributeNS(AddressingVersion.W3C.nsUri,AddressingVersion.W3C.getPrefix()+":"+IS_REFERENCE_PARAMETER,TRUE_VALUE);
        } catch (XMLStreamBufferException e) {
            throw new SOAPException(e);
        }
    }

    @Override
    public void writeTo(ContentHandler contentHandler, ErrorHandler errorHandler) throws SAXException {
        class Filter extends XMLFilterImpl {
            Filter(ContentHandler ch) { setContentHandler(ch); }
            private int depth=0;
            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                if(depth++==0) {
                    // add one more attribute
                    super.startPrefixMapping("wsa",AddressingVersion.W3C.nsUri);

                    //Add the attirbute wsa:IsReferenceParameter if not present already
                    if(atts.getIndex(AddressingVersion.W3C.nsUri,IS_REFERENCE_PARAMETER) == -1) {
                        AttributesImpl atts2 = new AttributesImpl(atts);
                        atts2.addAttribute(
                                AddressingVersion.W3C.nsUri,
                                IS_REFERENCE_PARAMETER,
                                "wsa:IsReferenceParameter",
                                "CDATA",
                                TRUE_VALUE);
                        atts = atts2;
                    }
                }

                super.startElement(uri, localName, qName, atts);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                if (--depth == 0) {
                    super.endPrefixMapping("wsa");
                }
            }
        }

        infoset.writeTo(new Filter(contentHandler),errorHandler);
    }


    /**
     * Keep the information about an attribute on the header element.
     */
    static final class Attribute {
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

        /**
         * Convert null to "".
         */
        private static String fixNull(String s) {
            return s == null ? "" : s;
        }
    }

    /**
     * We the performance paranoid people in the JAX-WS RI thinks
     * saving three bytes is worth while...
     */
    private static final String TRUE_VALUE = "1";
    private static final String IS_REFERENCE_PARAMETER = "IsReferenceParameter";
}
