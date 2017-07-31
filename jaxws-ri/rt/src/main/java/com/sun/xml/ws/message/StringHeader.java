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
import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Header;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link Header} that has a single text value in it
 * (IOW, of the form &lt;foo>text&lt;/foo>.)
 *
 * @author Rama Pulavarthi
 * @author Arun Gupta
 */
public class StringHeader extends AbstractHeaderImpl {
    /**
     * Tag name.
     */
    protected final QName name;
    /**
     * Header value.
     */
    protected final String value;

    protected boolean mustUnderstand = false;
    protected SOAPVersion soapVersion;

    public StringHeader(@NotNull QName name, @NotNull String value) {
        assert name != null;
        assert value != null;
        this.name = name;
        this.value = value;
    }

    public StringHeader(@NotNull QName name, @NotNull String value, @NotNull SOAPVersion soapVersion, boolean mustUnderstand ) {
        this.name = name;
        this.value = value;
        this.soapVersion = soapVersion;
        this.mustUnderstand = mustUnderstand;
    }

    public @NotNull String getNamespaceURI() {
        return name.getNamespaceURI();
    }

    public @NotNull String getLocalPart() {
        return name.getLocalPart();
    }

    @Nullable public String getAttribute(@NotNull String nsUri, @NotNull String localName) {
        if(mustUnderstand && soapVersion.nsUri.equals(nsUri) && MUST_UNDERSTAND.equals(localName)) {
            return getMustUnderstandLiteral(soapVersion);
        }
        return null;
    }

    public XMLStreamReader readHeader() throws XMLStreamException {
        MutableXMLStreamBuffer buf = new MutableXMLStreamBuffer();
        XMLStreamWriter w = buf.createFromXMLStreamWriter();
        writeTo(w);
        return buf.readAsXMLStreamReader();
    }

    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("", name.getLocalPart(), name.getNamespaceURI());
        w.writeDefaultNamespace(name.getNamespaceURI());
        if (mustUnderstand) {
            //Writing the ns declaration conditionally checking in the NSContext breaks XWSS. as readHeader() adds ns declaration,
            // where as writing alonf with the soap envelope does n't add it.
            //Looks like they expect the readHeader() and writeTo() produce the same infoset, Need to understand their usage

            //if(w.getNamespaceContext().getPrefix(soapVersion.nsUri) == null) {
            w.writeNamespace("S", soapVersion.nsUri);
            w.writeAttribute("S", soapVersion.nsUri, MUST_UNDERSTAND, getMustUnderstandLiteral(soapVersion));
            // } else {
            // w.writeAttribute(soapVersion.nsUri,MUST_UNDERSTAND, getMustUnderstandLiteral(soapVersion));
            // }
        }
        w.writeCharacters(value);
        w.writeEndElement();
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        SOAPHeader header = saaj.getSOAPHeader();
        if(header == null)
            header = saaj.getSOAPPart().getEnvelope().addHeader();
        SOAPHeaderElement she = header.addHeaderElement(name);
        if(mustUnderstand) {
            she.setMustUnderstand(true);
        }
        she.addTextNode(value);
    }

    public void writeTo(ContentHandler h, ErrorHandler errorHandler) throws SAXException {
        String nsUri = name.getNamespaceURI();
        String ln = name.getLocalPart();

        h.startPrefixMapping("",nsUri);
        if(mustUnderstand) {
            AttributesImpl attributes = new AttributesImpl();
            attributes.addAttribute(soapVersion.nsUri,MUST_UNDERSTAND,"S:"+MUST_UNDERSTAND,"CDATA", getMustUnderstandLiteral(soapVersion));
            h.startElement(nsUri,ln,ln,attributes);
        } else {
            h.startElement(nsUri,ln,ln,EMPTY_ATTS);
        }
        h.characters(value.toCharArray(),0,value.length());
        h.endElement(nsUri,ln,ln);
    }

    private static String getMustUnderstandLiteral(SOAPVersion sv) {
        if(sv == SOAPVersion.SOAP_12) {
            return S12_MUST_UNDERSTAND_TRUE;
        } else {
            return S11_MUST_UNDERSTAND_TRUE;
        }

    }

    protected static final String MUST_UNDERSTAND = "mustUnderstand";
    protected static final String S12_MUST_UNDERSTAND_TRUE ="true";
    protected static final String S11_MUST_UNDERSTAND_TRUE ="1";
}
