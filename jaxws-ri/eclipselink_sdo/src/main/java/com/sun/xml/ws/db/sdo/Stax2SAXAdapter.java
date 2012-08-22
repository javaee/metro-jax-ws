/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db.sdo;

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: Jun 8, 2009
 * Time: 2:44:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Stax2SAXAdapter {

    private XMLStreamReader staxReader;
    private boolean readDocument = false;      // the input is the entire document.

    public Stax2SAXAdapter(XMLStreamReader stax, boolean readDocument) {
        this.staxReader = stax;
        this.readDocument = readDocument;
    }

    public void parse(ContentHandler handler) throws XMLStreamException, SAXException {

        Stack<String> stack = new Stack<String>();
        boolean done = false;
        while (!done) {
            int event = staxReader.getEventType();
            switch (event) {
                case XMLStreamConstants.START_DOCUMENT:
                    if (readDocument) {
                        processDocument(handler);
                    }                    
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    if (readDocument) {
                        handler.endDocument();
                    }
                    done = true;
                    break;
                case XMLStreamConstants.START_ELEMENT:
                    stack.push(staxReader.getLocalName());
                    processStartElement(handler);
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    processEndElement(handler);
                    stack.pop();
                    if (stack.isEmpty() && !readDocument) {
                        done = true;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    handler.characters(staxReader.getTextCharacters(), staxReader.getTextStart(), staxReader.getTextLength());
                    break;
                case XMLStreamConstants.CDATA:
                    //TODO
                    break;
                case XMLStreamConstants.ATTRIBUTE:
                    // already processed in element
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    break;
                case XMLStreamConstants.DTD:
                    break;
                case XMLStreamConstants.ENTITY_DECLARATION:
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    break;
                case XMLStreamConstants.NAMESPACE:
                    break;
                case XMLStreamConstants.NOTATION_DECLARATION:
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    handler.processingInstruction(staxReader.getPIData(), staxReader.getPITarget());
                    break;
                default:
                    break;
            }
            staxReader.next();
        }

        staxReader.close();


    }

    private void processDocument(ContentHandler handler) throws SAXException {
        Location loc = staxReader.getLocation();
        handler.setDocumentLocator(new SAXLocator(loc.getPublicId(), loc.getSystemId(), loc.getLineNumber(), loc.getColumnNumber()));
        handler.startDocument();
    }

    private void processStartElement(ContentHandler handler) throws SAXException {

        int namespaceCount = staxReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String p = staxReader.getNamespacePrefix(i);
            if (p == null) {
                p = "";
            }
            String u = staxReader.getNamespaceURI(i);
            handler.startPrefixMapping(p, u);
        }

        QName qname = staxReader.getName();
        String namespaceURI = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();

        int attCount = staxReader.getAttributeCount();
        AttributesImpl atts = new AttributesImpl();
        for (int i = 0; i < attCount; i++) {
            String p = staxReader.getAttributePrefix(i);
            if (p == null) {
                p = "";
            }
            String l = staxReader.getAttributeLocalName(i);
            String u = staxReader.getAttributeNamespace(i);
            String type = staxReader.getAttributeType(i);
            String value = staxReader.getAttributeValue(i);
            atts.addAttribute(u, l, getPrefixName(p, l), type, value);
        }
        handler.startElement(namespaceURI, localName, getPrefixName(prefix, localName), atts);
    }

    private void processEndElement(ContentHandler handler) throws SAXException {

        QName qname = staxReader.getName();
        String namespaceURI = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();

        handler.endElement(namespaceURI, localName, getPrefixName(prefix, localName));
        int namespaceCount = staxReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String p = staxReader.getNamespacePrefix(i);
            if (p == null) {
                p = "";
            }
            handler.endPrefixMapping(p);
        }


    }

    private String getPrefixName(String prefix, String local) {
        if (prefix == null || prefix.length() == 0) {
            return local;
        } else {
            return prefix + ":" + local;
        }
    }

    private static class SAXLocator implements Locator {

        private String publicId;
        private String systemId;
        private int lineNumber;
        private int columnNumber;

        public SAXLocator(String publicId, String systemId, int lineNumber, int columnNumber) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }

        public String getPublicId() {
            return publicId;
        }

        public String getSystemId() {
            return systemId;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumnNumber() {
            return columnNumber;
        }
    }


}
