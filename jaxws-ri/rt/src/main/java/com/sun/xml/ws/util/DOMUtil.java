/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.util;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author JAXWS Development Team
 */
public class DOMUtil {

    private static DocumentBuilder db;

    /**
     * Creates a new DOM document.
     */
    public static Document createDom() {
        synchronized (DOMUtil.class) {
            if (db == null) {
                try {
                    DocumentBuilderFactory dbf = XmlUtil.newDocumentBuilderFactory();
                    dbf.setNamespaceAware(true);
                    db = dbf.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new FactoryConfigurationError(e);
                }
            }
            return db.newDocument();
        }
    }

    /**
     * Traverses a DOM node and writes out on a streaming writer.
     *
     * @param node
     * @param writer
     */
    public static void serializeNode(Element node, XMLStreamWriter writer) throws XMLStreamException {
        writeTagWithAttributes(node, writer);

        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                switch (child.getNodeType()) {
                    case Node.PROCESSING_INSTRUCTION_NODE:
                        writer.writeProcessingInstruction(child.getNodeValue());
                        break;
                    case Node.DOCUMENT_TYPE_NODE:
                        break;
                    case Node.CDATA_SECTION_NODE:
                        writer.writeCData(child.getNodeValue());
                        break;
                    case Node.COMMENT_NODE:
                        writer.writeComment(child.getNodeValue());
                        break;
                    case Node.TEXT_NODE:
                        writer.writeCharacters(child.getNodeValue());
                        break;
                    case Node.ELEMENT_NODE:
                        serializeNode((Element) child, writer);
                        break;
                    default: break;
                }
            }
        }
        writer.writeEndElement();
    }

    public static void writeTagWithAttributes(Element node, XMLStreamWriter writer) throws XMLStreamException {
        String nodePrefix = fixNull(node.getPrefix());
        String nodeNS = fixNull(node.getNamespaceURI());
        //fix to work with DOM level 1 nodes.
        String nodeLocalName = node.getLocalName()== null?node.getNodeName():node.getLocalName();
        
        // See if nodePrefix:nodeNS is declared in writer's NamespaceContext before writing start element
        // Writing start element puts nodeNS in NamespaceContext even though namespace declaration not written
        boolean prefixDecl = isPrefixDeclared(writer, nodeNS, nodePrefix);
        writer.writeStartElement(nodePrefix, nodeLocalName, nodeNS);

        if (node.hasAttributes()) {
            NamedNodeMap attrs = node.getAttributes();
            int numOfAttributes = attrs.getLength();
            // write namespace declarations first.
            // if we interleave this with attribue writing,
            // Zephyr will try to fix it and we end up getting inconsistent namespace bindings.
            for (int i = 0; i < numOfAttributes; i++) {
                Node attr = attrs.item(i);
                String nsUri = fixNull(attr.getNamespaceURI());
                if (nsUri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    // handle default ns declarations
                    String local = attr.getLocalName().equals(XMLConstants.XMLNS_ATTRIBUTE) ? "" : attr.getLocalName();
                    if (local.equals(nodePrefix) && attr.getNodeValue().equals(nodeNS)) {
                        prefixDecl = true;
                    }
                    if (local.equals("")) {
                        writer.writeDefaultNamespace(attr.getNodeValue());
                    } else {
                        // this is a namespace declaration, not an attribute
                        writer.setPrefix(attr.getLocalName(), attr.getNodeValue());
                        writer.writeNamespace(attr.getLocalName(), attr.getNodeValue());
                    }
                }
            }
        }
        // node's namespace is not declared as attribute, but declared on ancestor
        if (!prefixDecl) {
            writer.writeNamespace(nodePrefix, nodeNS);
        }

        // Write all other attributes which are not namespace decl.
        if (node.hasAttributes()) {
            NamedNodeMap attrs = node.getAttributes();
            int numOfAttributes = attrs.getLength();

            for (int i = 0; i < numOfAttributes; i++) {
                Node attr = attrs.item(i);
                String attrPrefix = fixNull(attr.getPrefix());
                String attrNS = fixNull(attr.getNamespaceURI());
                if (!attrNS.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    String localName = attr.getLocalName();
                    if (localName == null) {
                        // TODO: this is really a bug in the caller for not creating proper DOM tree.
                        // will remove this workaround after plugfest
                        localName = attr.getNodeName();
                    }
                    boolean attrPrefixDecl = isPrefixDeclared(writer, attrNS, attrPrefix);
                    if (!attrPrefix.equals("") && !attrPrefixDecl) {
                        // attr has namespace but namespace decl is there in ancestor node
                        // So write the namespace decl before writing the attr
                        writer.setPrefix(attr.getLocalName(), attr.getNodeValue());
                        writer.writeNamespace(attrPrefix, attrNS);
                    }
                    writer.writeAttribute(attrPrefix, attrNS, localName, attr.getNodeValue());
                }
            }
        }
    }

    private static boolean isPrefixDeclared(XMLStreamWriter writer, String nsUri, String prefix) {
        boolean prefixDecl = false;
        NamespaceContext nscontext = writer.getNamespaceContext();
        Iterator prefixItr = nscontext.getPrefixes(nsUri);
        while (prefixItr.hasNext()) {
            if (prefix.equals(prefixItr.next())) {
                prefixDecl = true;
                break;
            }
        }
        return prefixDecl;
    }

    /**
     * Gets the first child of the given name, or null.
     */
    public static Element getFirstChild(Element e, String nsUri, String local) {
        for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element c = (Element) n;
                if (c.getLocalName().equals(local) && c.getNamespaceURI().equals(nsUri)) {
                    return c;
                }
            }
        }
        return null;
    }

    private static
    @NotNull
    String fixNull(@Nullable String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    /**
     * Gets the first element child.
     */
    public static
    @Nullable
    Element getFirstElementChild(Node parent) {
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) n;
            }
        }
        return null;
    }

    public static @NotNull
    List<Element> getChildElements(Node parent){
        List<Element> elements = new ArrayList<Element>();
        for (Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            }
        }
        return elements;
    }
}
