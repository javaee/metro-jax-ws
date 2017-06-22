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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.namespace.QName;

import com.sun.istack.Nullable;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * @author Arun Gupta
 */
public class FaultDetailHeader extends AbstractHeaderImpl {

    private AddressingVersion av;
    private String wrapper;
    private String problemValue = null;

    public FaultDetailHeader(AddressingVersion av, String wrapper, QName problemHeader) {
        this.av = av;
        this.wrapper = wrapper;
        this.problemValue = problemHeader.toString();
    }

    public FaultDetailHeader(AddressingVersion av, String wrapper, String problemValue) {
        this.av = av;
        this.wrapper = wrapper;
        this.problemValue = problemValue;
    }

    public
    @NotNull
    String getNamespaceURI() {
        return av.nsUri;
    }

    public
    @NotNull
    String getLocalPart() {
        return av.faultDetailTag.getLocalPart();
    }

    @Nullable
    public String getAttribute(@NotNull String nsUri, @NotNull String localName) {
        return null;
    }

    public XMLStreamReader readHeader() throws XMLStreamException {
        MutableXMLStreamBuffer buf = new MutableXMLStreamBuffer();
        XMLStreamWriter w = buf.createFromXMLStreamWriter();
        writeTo(w);
        return buf.readAsXMLStreamReader();
    }

    public void writeTo(XMLStreamWriter w) throws XMLStreamException {
        w.writeStartElement("", av.faultDetailTag.getLocalPart(), av.faultDetailTag.getNamespaceURI());
        w.writeDefaultNamespace(av.nsUri);
        w.writeStartElement("", wrapper, av.nsUri);
        w.writeCharacters(problemValue);
        w.writeEndElement();
        w.writeEndElement();
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        SOAPHeader header = saaj.getSOAPHeader();
        if (header == null)
                header = saaj.getSOAPPart().getEnvelope().addHeader();
        SOAPHeaderElement she = header.addHeaderElement(av.faultDetailTag);
        she = header.addHeaderElement(new QName(av.nsUri, wrapper));
        she.addTextNode(problemValue);
    }

    public void writeTo(ContentHandler h, ErrorHandler errorHandler) throws SAXException {
        String nsUri = av.nsUri;
        String ln = av.faultDetailTag.getLocalPart();

        h.startPrefixMapping("",nsUri);
        h.startElement(nsUri,ln,ln,EMPTY_ATTS);
        h.startElement(nsUri,wrapper,wrapper,EMPTY_ATTS);
        h.characters(problemValue.toCharArray(),0,problemValue.length());
        h.endElement(nsUri,wrapper,wrapper);
        h.endElement(nsUri,ln,ln);
    }
}
