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

package com.sun.xml.ws.streaming;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;

/**
 * <p> XMLStreamReaderUtil provides some utility methods intended to be used
 * in conjunction with a StAX XMLStreamReader. </p>
 *
 * @author WS Development Team
 */
public class XMLStreamReaderUtil {

    private XMLStreamReaderUtil() {
    }

    public static void close(XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    public static void readRest(XMLStreamReader reader) {
        try {
            while(reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
                reader.next();
            }
        } catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    public static int next(XMLStreamReader reader) {
        try {
            int readerEvent = reader.next();

            while (readerEvent != END_DOCUMENT) {
                switch (readerEvent) {
                    case START_ELEMENT:
                    case END_ELEMENT:
                    case CDATA:
                    case CHARACTERS:
                    case PROCESSING_INSTRUCTION:
                        return readerEvent;
                    default:
                        // falls through ignoring event
                }
                readerEvent = reader.next();
            }

            return readerEvent;
        }
        catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    public static int nextElementContent(XMLStreamReader reader) {
        int state = nextContent(reader);
        if (state == CHARACTERS) {
            throw new XMLStreamReaderException(
                "xmlreader.unexpectedCharacterContent", reader.getText());
        }
        return state;
    }
    
    public static void toNextTag(XMLStreamReader reader, QName name) {
        // skip any whitespace
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT &&
                reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            XMLStreamReaderUtil.nextElementContent(reader);
        }
        if(reader.getEventType() == XMLStreamConstants.END_ELEMENT && name.equals(reader.getName())) {
            XMLStreamReaderUtil.nextElementContent(reader); 
        }
    }

    /**
     * Moves next and read spaces from the reader as long as to the next element.
     * Comments are ignored
     * @param reader
     * @return
     */
    public static String nextWhiteSpaceContent(XMLStreamReader reader) {
        next(reader);
        return currentWhiteSpaceContent(reader);
    }

    /**
     * Read spaces from the reader as long as to the next element, starting from
     * current position. Comments are ignored.
     * @param reader
     * @return
     */
    public static String currentWhiteSpaceContent(XMLStreamReader reader) {

        // since the there might be several valid chunks (spaces/comment/spaces)
        // StringBuilder must be used; it's initialized lazily, only when needed
        StringBuilder whiteSpaces = null;

        for (;;) {
            switch (reader.getEventType()) {
                case START_ELEMENT:
                case END_ELEMENT:
                case END_DOCUMENT:
                    return whiteSpaces == null ? null : whiteSpaces.toString();
                case CHARACTERS:
                    if (reader.isWhiteSpace()) {
                        if (whiteSpaces == null) {
                            whiteSpaces = new StringBuilder();
                        }
                        whiteSpaces.append(reader.getText());
                    } else {
                        throw new XMLStreamReaderException(
                                "xmlreader.unexpectedCharacterContent", reader.getText());
                    }
            }
            next(reader);
        }
    }

    public static int nextContent(XMLStreamReader reader) {
        for (;;) {
            int state = next(reader);
            switch (state) {
                case START_ELEMENT:
                case END_ELEMENT:
                case END_DOCUMENT:
                    return state;
                case CHARACTERS:
                    if (!reader.isWhiteSpace()) {
                        return CHARACTERS;
                    }
            }
        }
    }

    /**
     * Skip current element, leaving the cursor at END_ELEMENT of
     * current element.
     */
    public static void skipElement(XMLStreamReader reader) {
        assert reader.getEventType() == START_ELEMENT;
        skipTags(reader, true);
        assert reader.getEventType() == END_ELEMENT;
    }

    /**
     * Skip following siblings, leaving cursor at END_ELEMENT of
     * parent element.
     */
    public static void skipSiblings(XMLStreamReader reader, QName parent) {
        skipTags(reader, reader.getName().equals(parent));
        assert reader.getEventType() == END_ELEMENT;
    }

    private static void skipTags(XMLStreamReader reader, boolean exitCondition) {
        try {
            int state, tags = 0;
            while ((state = reader.next()) != END_DOCUMENT) {
                if (state == START_ELEMENT) {
                    tags++;
                }
                else if (state == END_ELEMENT) {
                    if (tags == 0 && exitCondition) return;
                    tags--;
                }
            }
        }
        catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    /*
    * Get the text of an element
    */
    public static String getElementText(XMLStreamReader reader) {
        try {
            return reader.getElementText();
        } catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    /*
    * Get a QName with 'someUri' and 'localname' from an
    * element of qname type:
    * <xyz xmlns:ns1="someUri">ns1:localname</xyz>
    */
    public static QName getElementQName(XMLStreamReader reader) {
        try {
            String text = reader.getElementText().trim();
            String prefix = text.substring(0,text.indexOf(':'));
            String namespaceURI = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (namespaceURI == null) {
                namespaceURI = "";
            }
            String localPart = text.substring(
                    text.indexOf(':') + 1, text.length());
            return new QName(namespaceURI, localPart);
        } catch (XMLStreamException e) {
            throw wrapException(e);
        }
    }

    /**
     * Read all attributes into an data structure. Note that this method cannot
     * be called multiple times to get the same list of attributes. 
     */
    public static Attributes getAttributes(XMLStreamReader reader) {
        return (reader.getEventType() == START_ELEMENT ||
                reader.getEventType() == ATTRIBUTE) ?
                new AttributesImpl(reader) : null;
    }

    public static void verifyReaderState(XMLStreamReader reader, int expectedState) {
        int state = reader.getEventType();
        if (state != expectedState) {
            throw new XMLStreamReaderException(
                "xmlreader.unexpectedState",
                getStateName(expectedState), getStateName(state));
        }
    }

    public static void verifyTag(XMLStreamReader reader, String namespaceURI, String localName) {
        if (!localName.equals(reader.getLocalName()) || !namespaceURI.equals(reader.getNamespaceURI())) {
            throw new XMLStreamReaderException(
                "xmlreader.unexpectedState.tag",
                    "{" + namespaceURI + "}" + localName,
                    "{" + reader.getNamespaceURI() + "}" + reader.getLocalName());
        }
    }
    
    public static void verifyTag(XMLStreamReader reader, QName name) {
        verifyTag(reader, name.getNamespaceURI(), name.getLocalPart());
    }

    public static String getStateName(XMLStreamReader reader) {
        return getStateName(reader.getEventType());
    }

    public static String getStateName(int state) {
        switch (state) {
            case ATTRIBUTE:
                return "ATTRIBUTE";
            case CDATA:
                return "CDATA";
            case CHARACTERS:
                return "CHARACTERS";
            case COMMENT:
                return "COMMENT";
            case DTD:
                return "DTD";
            case END_DOCUMENT:
                return "END_DOCUMENT";
            case END_ELEMENT:
                return "END_ELEMENT";
            case ENTITY_DECLARATION:
                return "ENTITY_DECLARATION";
            case ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case NAMESPACE:
                return "NAMESPACE";
            case NOTATION_DECLARATION:
                return "NOTATION_DECLARATION";
            case PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case SPACE:
                return "SPACE";
            case START_DOCUMENT:
                return "START_DOCUMENT";
            case START_ELEMENT:
                return "START_ELEMENT";
            default :
                return "UNKNOWN";
        }
    }
    
    private static XMLStreamReaderException wrapException(XMLStreamException e) {
        return new XMLStreamReaderException("xmlreader.ioException",e);
    }

    // -- Auxiliary classes ----------------------------------------------

    /**
     * AttributesImpl class copied from old StAXReader. This class is used to implement
     * getAttributes() on a StAX Reader.
     */
    public static class AttributesImpl implements Attributes {

        static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

        static class AttributeInfo {

            private QName name;
            private String value;

            public AttributeInfo(QName name, String value) {
                this.name = name;
                if (value == null) {
                    // e.g., <return xmlns=""> -- stax returns null
                    this.value = "";
                } else {
                    this.value = value;
                }
            }

            QName getName() {
                return name;
            }

            String getValue() {
                return value;
            }

            /*
             * Return "xmlns:" as part of name if namespace.
             */
            String getLocalName() {
                if (isNamespaceDeclaration()) {
                    if (name.getLocalPart().equals("")) {
                        return "xmlns";
                    }
                    return "xmlns:" + name.getLocalPart();
                }
                return name.getLocalPart();
            }

            boolean isNamespaceDeclaration() {
                return (name.getNamespaceURI() == XMLNS_NAMESPACE_URI);
            }
        }

        // stores qname and value for each attribute
        AttributeInfo [] atInfos;

        /*
         * Will create a list that contains the namespace declarations
         * as well as the other attributes.
         */
        public AttributesImpl(XMLStreamReader reader) {
            if (reader == null) {

                // this is the case when we call getAttributes() on the
                // reader when it is not on a start tag
                atInfos = new AttributeInfo[0];
            } else {

                // this is the normal case
                int index = 0;
                int namespaceCount = reader.getNamespaceCount();
                int attributeCount = reader.getAttributeCount();
                atInfos = new AttributeInfo[namespaceCount + attributeCount];
                for (int i=0; i<namespaceCount; i++) {
                    String namespacePrefix = reader.getNamespacePrefix(i);

                    // will be null if default prefix. QName can't take null
                    if (namespacePrefix == null) {
                        namespacePrefix = "";
                    }
                    atInfos[index++] = new AttributeInfo(
                        new QName(XMLNS_NAMESPACE_URI,
                            namespacePrefix,
                            "xmlns"),
                        reader.getNamespaceURI(i));
                }
                for (int i=0; i<attributeCount; i++) {
                    atInfos[index++] = new AttributeInfo(
                        reader.getAttributeName(i),
                        reader.getAttributeValue(i));
                }
            }
        }

        public int getLength() {
            return atInfos.length;
        }

        public String getLocalName(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].getLocalName();
            }
            return null;
        }

        public QName getName(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].getName();
            }
            return null;
        }

        public String getPrefix(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].getName().getPrefix();
            }
            return null;
        }

        public String getURI(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].getName().getNamespaceURI();
            }
            return null;
        }

        public String getValue(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].getValue();
            }
            return null;
        }

        public String getValue(QName name) {
            int index = getIndex(name);
            if (index != -1) {
                return atInfos[index].getValue();
            }
            return null;
        }

        public String getValue(String localName) {
            int index = getIndex(localName);
            if (index != -1) {
                return atInfos[index].getValue();
            }
            return null;
        }

        public String getValue(String uri, String localName) {
            int index = getIndex(uri, localName);
            if (index != -1) {
                return atInfos[index].getValue();
            }
            return null;
        }

        public boolean isNamespaceDeclaration(int index) {
            if (index >= 0 && index < atInfos.length) {
                return atInfos[index].isNamespaceDeclaration();
            }
            return false;
        }

        public int getIndex(QName name) {
            for (int i=0; i<atInfos.length; i++) {
                if (atInfos[i].getName().equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        public int getIndex(String localName) {
            for (int i=0; i<atInfos.length; i++) {
                if (atInfos[i].getName().getLocalPart().equals(localName)) {
                    return i;
                }
            }
            return -1;
        }

        public int getIndex(String uri, String localName) {
            QName qName;
            for (int i=0; i<atInfos.length; i++) {
                qName = atInfos[i].getName();
                if (qName.getNamespaceURI().equals(uri) &&
                    qName.getLocalPart().equals(localName)) {

                    return i;
                }
            }
            return -1;
        }
    }
}
