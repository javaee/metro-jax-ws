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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 15, 2009
 * Time: 3:44:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class SAX2StaxContentHandler implements ContentHandler {

    private XMLStreamWriter xwriter;
    private List<PrefixMapping> prefixMappings;
    private boolean writeDocument = false;

    static class PrefixMapping {
        PrefixMapping(String p, String u) {
            if (p == null) {
                prefix = "";
            } else {
                prefix = p;
            }
            uri = u;
        }

        String prefix;
        String uri;
    }

    public SAX2StaxContentHandler(XMLStreamWriter writer, boolean writeDoc) {
        xwriter = writer;
        prefixMappings = new ArrayList<PrefixMapping>();
        writeDocument = writeDoc;
    }

    public SAX2StaxContentHandler(XMLStreamWriter writer) {
        xwriter = writer;
        prefixMappings = new ArrayList<PrefixMapping>();
        writeDocument = false;
    }

    public void setDocumentLocator(Locator locator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startDocument() throws SAXException {
        if (writeDocument) {
            try {
                xwriter.writeStartDocument();
                xwriter.flush();
            }
            catch (XMLStreamException xe) {
                throw new SAXException(xe);
            }
        }
    }

    public void endDocument() throws SAXException {
        if (writeDocument) {
            try {
                xwriter.writeEndDocument();
                xwriter.flush();
            }
            catch (XMLStreamException xe) {
                throw new SAXException(xe);
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {

        if (prefix.equals("xml")) {
            return;
        }

        prefixMappings.add(new PrefixMapping(prefix, uri));
    }

    public void endPrefixMapping(String string) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String getPrefix(String qname) {
        int index = qname.indexOf(":");
        String prefix = "";
        if (index > 0) {
            prefix = qname.substring(0, index);
        }
        return prefix;
    }

    public void startElement(String ns, String local, String qname, Attributes attributes) throws SAXException {
        try {
            String prefix = getPrefix(qname);
            xwriter.writeStartElement(prefix, local, ns);
            if (!prefixMappings.isEmpty()) {

                for (int i = 0; i < prefixMappings.size(); i++) {

                    PrefixMapping prefixMapping = prefixMappings.get(i);
                    String p = prefixMapping.prefix;
                    String u = prefixMapping.uri;

                    if (p.length() == 0) {
                        xwriter.setDefaultNamespace(u);
                    } else {
                        xwriter.setPrefix(p, u);
                    }
                    xwriter.writeNamespace(p, u);
                }
                prefixMappings.clear();
            }
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String name = attributes.getQName(i);
                    String px = getPrefix(name);
                    String uri = attributes.getURI(i);
                    String value = attributes.getValue(i);
                    String localname = attributes.getLocalName(i);
                    if (px != null && px.equals("xmlns")) {
                        continue;
                    }
                    if (px != null && px.length() > 0) {
                        xwriter.writeAttribute(px, uri, localname, value);
                    } else {
                        xwriter.writeAttribute(localname, value);
                    }
                }
            }
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }

    public void endElement(String string, String string1, String string2) throws SAXException {
        try {
            xwriter.writeEndElement();
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }

    public void characters(char[] chars, int i, int i1) throws SAXException {
        try {
            xwriter.writeCharacters(chars, i, i1);
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }

    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        try {
            xwriter.writeCharacters(chars, i, i1);
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }

    public void processingInstruction(String string, String string1) throws SAXException {
        try {
            xwriter.writeProcessingInstruction(string, string1);
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }

    public void skippedEntity(String string) throws SAXException {
        try {
            xwriter.writeEntityRef(string);
        }
        catch (XMLStreamException xe) {
            throw new SAXException(xe);
        }
    }
}

