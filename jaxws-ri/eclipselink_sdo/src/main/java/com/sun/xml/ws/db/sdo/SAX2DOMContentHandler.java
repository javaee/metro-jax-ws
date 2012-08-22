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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: Jun 5, 2009
 * Time: 4:20:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SAX2DOMContentHandler implements ContentHandler {

    private Document doc;
    private Stack<Node> stack;
    private Map<String, String> prefixMappings;

    public SAX2DOMContentHandler() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            doc = documentBuilder.newDocument();
            stack = new Stack<Node>();
            stack.push(doc);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        prefixMappings = new HashMap<String, String>();
    }

    public SAX2DOMContentHandler(Document d) {
        doc = d;
        stack = new Stack<Node>();
        stack.push(d);
        prefixMappings = new HashMap<String, String>();
    }

    public void setDocumentLocator(Locator locator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startDocument() throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void endDocument() throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixMappings.put(prefix, uri);
    }

    public void endPrefixMapping(String string) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void startElement(String ns, String local, String qname, Attributes attributes) throws SAXException {
        if (stack.isEmpty()) {
            throw new SAXException("invalid state");
        }
        Element e = doc.createElementNS(ns, qname);
        if (!prefixMappings.isEmpty()) {
            Set<Map.Entry<String, String>> prefixMappingSet = prefixMappings.entrySet();
            String pname = null;
            Iterator<Map.Entry<String, String>> i = prefixMappingSet.iterator();
            while (i.hasNext()) {
                Map.Entry<String, String> prefixMapping = i.next();
                String p = prefixMapping.getKey();
                String u = prefixMapping.getValue();
                if (p == null || p.length() == 0) {
                    pname = "xmlns";
                }
                else {
                    pname = "xmlns:" + p;
                }
                e.setAttributeNS("http://www.w3.org/2000/xmlns/",pname, u);
            }
            prefixMappings.clear();
        }
        if (attributes != null) {
            for (int i=0; i<attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                String uri = attributes.getURI(i);
                String value = attributes.getValue(i);
                e.setAttributeNS(uri, name, value);
            }
        }
        Node node = stack.peek();
        node.appendChild(e);
        stack.push(e);
    }

    public void endElement(String string, String string1, String string2) throws SAXException {
        stack.pop();
    }

    public void characters(char[] chars, int i, int i1) throws SAXException {
        Text text = doc.createTextNode(new String(chars, i, i1));
        if (stack.isEmpty()) {
            throw new SAXException("invalid state");
        }
        Node node = stack.peek();
        node.appendChild(text);
    }

    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void processingInstruction(String string, String string1) throws SAXException {
        ProcessingInstruction pi = doc.createProcessingInstruction(string, string1);
        if (stack.isEmpty()) {
            throw new SAXException("invalid state");
        }
        Node node = stack.peek();
        node.appendChild(pi);
    }

    public void skippedEntity(String string) throws SAXException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
