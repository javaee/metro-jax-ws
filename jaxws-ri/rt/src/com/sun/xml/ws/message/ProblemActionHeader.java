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

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * {@link Header} that represents &lt;wsa:ProblemAction>
 * @author Arun Gupta
 */
public class ProblemActionHeader extends AbstractHeaderImpl {
    protected @NotNull String action;
    protected String soapAction;
    protected @NotNull AddressingVersion av;

    private static final String actionLocalName = "Action";
    private static final String soapActionLocalName = "SoapAction";

    public ProblemActionHeader(@NotNull String action, @NotNull AddressingVersion av) {
        this(action,null,av);
    }

    public ProblemActionHeader(@NotNull String action, String soapAction, @NotNull AddressingVersion av) {
        assert action!=null;
        assert av!=null;
        this.action = action;
        this.soapAction = soapAction;
        this.av = av;
    }

    public
    @NotNull
    String getNamespaceURI() {
        return av.nsUri;
    }

    public
    @NotNull
    String getLocalPart() {
        return "ProblemAction";
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
        w.writeStartElement("", getLocalPart(), getNamespaceURI());
        w.writeDefaultNamespace(getNamespaceURI());
        w.writeStartElement(actionLocalName);
        w.writeCharacters(action);
        w.writeEndElement();
        if (soapAction != null) {
            w.writeStartElement(soapActionLocalName);
            w.writeCharacters(soapAction);
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    public void writeTo(SOAPMessage saaj) throws SOAPException {
        SOAPHeader header = saaj.getSOAPHeader();
        if(header == null)
            header = saaj.getSOAPPart().getEnvelope().addHeader();
        SOAPHeaderElement she = header.addHeaderElement(new QName(getNamespaceURI(), getLocalPart()));
        she.addChildElement(actionLocalName);
        she.addTextNode(action);
        if (soapAction != null) {
            she.addChildElement(soapActionLocalName);
            she.addTextNode(soapAction);
        }
    }

    public void writeTo(ContentHandler h, ErrorHandler errorHandler) throws SAXException {
        String nsUri = getNamespaceURI();
        String ln = getLocalPart();

        h.startPrefixMapping("",nsUri);
        h.startElement(nsUri,ln,ln,EMPTY_ATTS);
        h.startElement(nsUri,actionLocalName,actionLocalName,EMPTY_ATTS);
        h.characters(action.toCharArray(),0,action.length());
        h.endElement(nsUri,actionLocalName,actionLocalName);
        if (soapAction != null) {
            h.startElement(nsUri,soapActionLocalName,soapActionLocalName,EMPTY_ATTS);
            h.characters(soapAction.toCharArray(),0,soapAction.length());
            h.endElement(nsUri,soapActionLocalName,soapActionLocalName);
        }
        h.endElement(nsUri,ln,ln);
    }
}
