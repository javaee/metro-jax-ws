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

package com.sun.xml.ws.util.xml;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Stack;

/**
 * This is a simple utility class that adapts SAX events into StAX
 * {@link XMLStreamWriter} events, bridging between
 * the two parser technologies.
 *
 * This ContentHandler does not own the XMLStreamWriter.  Therefore, it will
 * not close or flush the writer at any point.
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version 1.0
 */
public class ContentHandlerToXMLStreamWriter extends DefaultHandler {

    // SAX events will be sent to this XMLStreamWriter
    private final XMLStreamWriter staxWriter;

    // storage for prefix bindings
    private final Stack prefixBindings;

    public ContentHandlerToXMLStreamWriter(XMLStreamWriter staxCore) {
        this.staxWriter = staxCore;
        prefixBindings = new Stack(); // default of 10 seems reasonable
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            staxWriter.writeEndDocument();
            staxWriter.flush();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        try {
            staxWriter.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException {

        try {
            staxWriter.writeCharacters(ch, start, length);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {

        characters(ch,start,length);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        // TODO: no-op?

        // I think we can ignore these SAX events because StAX
        // automatically scopes the prefix bindings.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        try {
            staxWriter.writeEntityRef(name);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        // TODO: no-op?
        // there doesn't seem to be any way to pass location info
        // along to the XMLStreamWriter. On the XMLEventWriter side, you
        // can set the location info on the event objects.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
        throws SAXException {

        try {
            staxWriter.writeProcessingInstruction(target, data);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        // defend against parsers that pass null in for "xmlns" prefix
        if (prefix == null) {
            prefix = "";
        }

        if (prefix.equals("xml")) {
            return;
        }

        prefixBindings.add(prefix);
        prefixBindings.add(uri);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {

        try {
            // TODO: is this all we have to do?
            staxWriter.writeEndElement();
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException {

        try {
            staxWriter.writeStartElement(
                getPrefix(qName),
                localName,
                namespaceURI);

            String uri, prefix;
            while (prefixBindings.size() != 0) {
                uri = (String)prefixBindings.pop();
                prefix = (String)prefixBindings.pop();
                if (prefix.length() == 0) {
                    staxWriter.setDefaultNamespace(uri);
                } else {
                    staxWriter.setPrefix(prefix, uri);
                }

                // this method handles "", null, and "xmlns" prefixes properly
                staxWriter.writeNamespace(prefix, uri);
            }

            writeAttributes(atts);
        } catch (XMLStreamException e) {
            throw new SAXException(e);
        }

    }

    /**
     * Generate a StAX writeAttribute event for each attribute
     *
     * @param atts
     *                attributes from the SAX event
     */
    private void writeAttributes(Attributes atts) throws XMLStreamException {
        for (int i = 0; i < atts.getLength(); i++) {
            final String prefix = getPrefix(atts.getQName(i));
            if(!prefix.equals("xmlns")) { // defend againts broken transformers that report xmlns decls as attrs
                staxWriter.writeAttribute(
                    prefix,
                    atts.getURI(i),
                    atts.getLocalName(i),
                    atts.getValue(i));
                }
        }
    }

    /**
     * Pull the prefix off of the specified QName.
     *
     * @param qName
     *                the QName
     * @return the prefix or the empty string if it doesn't exist.
     */
    private String getPrefix(String qName) {
        int idx = qName.indexOf(':');
        if (idx == -1) {
            return "";
        } else {
            return qName.substring(0, idx);
        }
    }

}
