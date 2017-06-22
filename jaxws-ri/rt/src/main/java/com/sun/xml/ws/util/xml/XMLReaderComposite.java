/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jvnet.staxex.NamespaceContextEx;
import org.jvnet.staxex.XMLStreamReaderEx;

import com.sun.xml.ws.encoding.TagInfoset;

/**
 * XMLReaderComposite
 * 
 * @author shih-chang.chen@oracle.com
 */
public class XMLReaderComposite implements XMLStreamReaderEx {

    static public enum State { StartTag, Payload, EndTag }

    protected State state = State.StartTag;
    protected ElemInfo elemInfo;
    protected TagInfoset tagInfo;
    protected XMLStreamReader[] children;
    protected int payloadIndex = -1;
    protected XMLStreamReader payloadReader;
    
    static public class ElemInfo implements NamespaceContext {        
        ElemInfo ancestor;
        TagInfoset tagInfo; 
        public ElemInfo(TagInfoset tag, ElemInfo parent) { tagInfo = tag; ancestor = parent; }
        public String getNamespaceURI(String prefix) {
            String n = tagInfo.getNamespaceURI(prefix);
            return (n != null) ? n : (ancestor != null) ?  ancestor.getNamespaceURI(prefix) : null;
        }
        public String getPrefix(String uri) {
            String p = tagInfo.getPrefix(uri);
            return (p != null) ? p : (ancestor != null) ?  ancestor.getPrefix(uri) : null;
        }
        //Who wants this?
        public List<String> allPrefixes(String namespaceURI) {
            List<String> l = tagInfo.allPrefixes(namespaceURI);
            if (ancestor != null) {
                List<String> p = ancestor.allPrefixes(namespaceURI);
                p.addAll(l);
                return p;
            }
            return l;
        }
        public Iterator<String> getPrefixes(String namespaceURI) {
            return allPrefixes(namespaceURI).iterator();
        }
    }
    
    public XMLReaderComposite(final ElemInfo elem, XMLStreamReader[] wrapees) {
        elemInfo = elem;
        tagInfo = elem.tagInfo;
        children = wrapees.clone();
        if (children != null && children.length > 0) {
            payloadIndex = 0;
            payloadReader = children[payloadIndex]; 
        }
    }
    

    @Override
    public int next() throws XMLStreamException {
        switch (state) {
        case StartTag:
            if (payloadReader != null) {
                state = State.Payload;
                return payloadReader.getEventType();
            } else {
                state = State.EndTag;
                return XMLStreamReader.END_ELEMENT;
            }
        case EndTag: return XMLStreamReader.END_DOCUMENT;
        case Payload:
        default:
            int next = XMLStreamReader.END_DOCUMENT;
            if (payloadReader != null && payloadReader.hasNext()) { 
                next = payloadReader.next();
            } 
            if (next != XMLStreamReader.END_DOCUMENT) return next;
            else {
                if (payloadIndex+1 < children.length ) {
                    payloadIndex++;
                    payloadReader = children[payloadIndex]; 
                    return payloadReader.getEventType();
                } else {
                    state = State.EndTag;
                    return XMLStreamReader.END_ELEMENT;
                }
            }
        }
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        switch (state) {
        case EndTag: return false;  
        case StartTag:        
        case Payload:
        default: return true; 
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        switch (state) {
        case StartTag:
            if (payloadReader.isCharacters()) return payloadReader.getText();
            return "";
        case Payload:
        default:
            return payloadReader.getElementText();
        }
    }

    @Override
    public int nextTag() throws XMLStreamException {
        int e = next();
        if (e == XMLStreamReader.END_DOCUMENT) return e;
        while (e != XMLStreamReader.END_DOCUMENT) {
            if (e == XMLStreamReader.START_ELEMENT) return e;
            if (e == XMLStreamReader.END_ELEMENT) return e;
            e = next();
        }
        return e;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return (payloadReader != null) ? payloadReader.getProperty(name) : null;
    }

    @Override
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if (payloadReader!=null) payloadReader.require(type, namespaceURI, localName);
    }

    @Override
    public void close() throws XMLStreamException {
        if (payloadReader!=null) payloadReader.close();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        switch (state) {
        case StartTag:
        case EndTag: 
            return elemInfo.getNamespaceURI(prefix);
        case Payload:
        default:
            return payloadReader.getNamespaceURI(prefix);
        }
    }

    @Override
    public boolean isStartElement() {
        switch (state) {
        case StartTag: return true;
        case EndTag: return false;
        case Payload:
        default:
            return payloadReader.isStartElement();
        }
    }

    @Override
    public boolean isEndElement() {
        switch (state) {
        case StartTag: return false;
        case EndTag: return true;
        case Payload:
        default:
            return payloadReader.isEndElement();
        }
    }

    @Override
    public boolean isCharacters() {
        switch (state) {
        case StartTag: 
        case EndTag: return false;
        case Payload:
        default:
            return payloadReader.isCharacters();
        }
    }

    @Override
    public boolean isWhiteSpace() {
        switch (state) {
        case StartTag: 
        case EndTag: return false;
        case Payload:
        default:
            return payloadReader.isWhiteSpace();
        }
    }

    @Override
    public String getAttributeValue(String uri, String localName) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getValue(uri, localName);
        case Payload:
        default:
            return payloadReader.getAttributeValue(uri, localName);
        }
    }

    @Override
    public int getAttributeCount() {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getLength();
        case Payload:
        default:
            return payloadReader.getAttributeCount();
        }
    }

    @Override
    public QName getAttributeName(int i) {
        switch (state) {
        case StartTag: 
        case EndTag: return new QName(tagInfo.atts.getURI(i),tagInfo.atts.getLocalName(i),getPrfix(tagInfo.atts.getQName(i)));
        case Payload:
        default:
            return payloadReader.getAttributeName(i);
        }
    }

    @Override
    public String getAttributeNamespace(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getURI(index);
        case Payload:
        default:
            return payloadReader.getAttributeNamespace(index);
        }
    }

    @Override
    public String getAttributeLocalName(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getLocalName(index);
        case Payload:
        default:
            return payloadReader.getAttributeLocalName(index);
        }
    }

    @Override
    public String getAttributePrefix(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return getPrfix(tagInfo.atts.getQName(index));
        case Payload:
        default:
            return payloadReader.getAttributePrefix(index);
        }
    }

    static private String getPrfix(String qName) {
        if (qName == null) return null;
        int i = qName.indexOf(":");
        return (i > 0)? qName.substring(0, i) : "";
    }


    @Override
    public String getAttributeType(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getType(index);
        case Payload:
        default:
            return payloadReader.getAttributeType(index);
        }
    }

    @Override
    public String getAttributeValue(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.atts.getValue(index);
        case Payload:
        default:
            return payloadReader.getAttributeValue(index);
        }
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return (index < tagInfo.atts.getLength()) ? tagInfo.atts.getLocalName(index) != null : false;
        case Payload:
        default:
            return payloadReader.isAttributeSpecified(index);
        }
    }

    @Override
    public int getNamespaceCount() {
        switch (state) {
        case StartTag: 
        case EndTag: return (tagInfo.ns.length/2);
        case Payload:
        default:
            return payloadReader.getNamespaceCount();
        }
    }

    @Override
    public String getNamespacePrefix(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.ns[2*index];
        case Payload:
        default:
            return payloadReader.getNamespacePrefix(index);
        }
    }

    @Override
    public String getNamespaceURI(int index) {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.ns[2*index+1];
        case Payload:
        default:
            return payloadReader.getNamespaceURI(index);
        }
    }

    @Override
    public NamespaceContextEx getNamespaceContext() {
        switch (state) {
        case StartTag: 
        case EndTag: return new NamespaceContextExAdaper(elemInfo);
        case Payload:
        default:
            return isPayloadReaderEx()?
                   payloadReaderEx().getNamespaceContext() : 
                   new NamespaceContextExAdaper(payloadReader.getNamespaceContext());
        }
    }
    
    private boolean isPayloadReaderEx() { return (payloadReader instanceof XMLStreamReaderEx); }   
    
    private XMLStreamReaderEx payloadReaderEx() { return (XMLStreamReaderEx)payloadReader; }

    @Override
    public int getEventType() {
        switch (state) {
        case StartTag: return XMLStreamReader.START_ELEMENT;
        case EndTag: return XMLStreamReader.END_ELEMENT;
        case Payload:
        default:
            return payloadReader.getEventType();
        }
    }

    @Override
    public String getText() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getText();
        }
    }

    @Override
    public char[] getTextCharacters() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getTextCharacters();
        }
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        switch (state) {
        case StartTag: 
        case EndTag: return -1;
        case Payload:
        default:
            return payloadReader.getTextCharacters(sourceStart, target, targetStart, length);
        }
    }

    @Override
    public int getTextStart() {
        switch (state) {
        case StartTag: 
        case EndTag: return 0;
        case Payload:
        default:
            return payloadReader.getTextStart();
        }
    }

    @Override
    public int getTextLength() {
        switch (state) {
        case StartTag: 
        case EndTag: return 0;
        case Payload:
        default:
            return payloadReader.getTextLength();
        }
    }

    @Override
    public String getEncoding() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getEncoding();
        }
    }

    @Override
    public boolean hasText() {
        switch (state) {
        case StartTag: 
        case EndTag: return false;
        case Payload:
        default:
            return payloadReader.hasText();
        }
    }

    @Override
    public Location getLocation() {
        switch (state) {
        case StartTag: 
        case EndTag: return new Location() {

            @Override
            public int getLineNumber() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getColumnNumber() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getCharacterOffset() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getPublicId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSystemId() {
                // TODO Auto-generated method stub
                return null;
            }
            
        };
        case Payload:
        default:
            return payloadReader.getLocation();
        }
    }

    @Override
    public QName getName() {
        switch (state) {
        case StartTag: 
        case EndTag: return new QName(tagInfo.nsUri, tagInfo.localName, tagInfo.prefix);
        case Payload:
        default:
            return payloadReader.getName();
        }
    }

    @Override
    public String getLocalName() {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.localName;
        case Payload:
        default:
            return payloadReader.getLocalName();
        }
    }

    @Override
    public boolean hasName() {
        switch (state) {
        case StartTag: 
        case EndTag: return true;
        case Payload:
        default:
            return payloadReader.hasName();
        }
    }

    @Override
    public String getNamespaceURI() {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.nsUri;
        case Payload:
        default:
            return payloadReader.getNamespaceURI();
        }
    }

    @Override
    public String getPrefix() {
        switch (state) {
        case StartTag: 
        case EndTag: return tagInfo.prefix;
        case Payload:
        default:
            return payloadReader.getPrefix();
        }
    }

    @Override
    public String getVersion() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getVersion();
        }
    }

    @Override
    public boolean isStandalone() {
        switch (state) {
        case StartTag: 
        case EndTag: return true;
        case Payload:
        default:
            return payloadReader.isStandalone();
        }
    }

    @Override
    public boolean standaloneSet() {
        switch (state) {
        case StartTag: 
        case EndTag: return true;
        case Payload:
        default:
            return payloadReader.standaloneSet();
        }
    }

    @Override
    public String getCharacterEncodingScheme() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getCharacterEncodingScheme();
        }
    }

    @Override
    public String getPITarget() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getPITarget();
        }
    }

    @Override
    public String getPIData() {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return payloadReader.getPIData();
        }
    }

    @Override
    public String getElementTextTrim() throws XMLStreamException {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return isPayloadReaderEx()? payloadReaderEx().getElementTextTrim() : payloadReader.getElementText().trim();
        }
    }

    @Override
    public CharSequence getPCDATA() throws XMLStreamException {
        switch (state) {
        case StartTag: 
        case EndTag: return null;
        case Payload:
        default:
            return isPayloadReaderEx()? payloadReaderEx().getPCDATA() : payloadReader.getElementText();
        }
    }
}
