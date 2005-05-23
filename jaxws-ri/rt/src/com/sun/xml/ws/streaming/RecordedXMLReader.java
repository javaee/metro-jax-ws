/*
 * $Id: RecordedXMLReader.java,v 1.1 2005-05-23 22:59:35 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
*
* @author JAX-RPC Development Team
*/
package com.sun.xml.ws.streaming;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.xml.ws.util.NamespaceSupport;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.util.StructMap;

public class RecordedXMLReader extends XMLReaderBase {
    //protected static final QName EMPTY_QNAME = new QName("");
    protected static final QName EMPTY_QNAME = new QName("fooqname");
    int frameIndex;
    List frames;
    ReaderFrame currentFrame;
    NamespaceSupport originalNamespaces;
    NamespaceSupport namespaceSupport;
    boolean lastRetWasEnd;

    public RecordedXMLReader(XMLReader reader, NamespaceSupport namespaces) {
        frameIndex = 0;
        frames = new ArrayList();

        originalNamespaces = new NamespaceSupport(namespaces);
        namespaceSupport = new NamespaceSupport(namespaces);
        lastRetWasEnd = false;

        int targetElementId = reader.getElementId();

        while (reader.getState() != END
            || reader.getElementId() != targetElementId) {
            recordFrame(reader);
            reader.next();
        }
        recordFrame(reader);

        setFrame(0);
    }

    protected void recordFrame(XMLReader reader) {
        Attributes attributeFrame = null;
        switch (reader.getState()) {
            case START :
                attributeFrame = new AttributeFrame(reader.getAttributes());
            case END :
                addFrame(
                    new ReaderFrame(
                        reader.getState(),
                        reader.getElementId(),
                        reader.getLineNumber(),
                        reader.getName(),
                        attributeFrame));
                break;
            case PI :
                // we neither anticipate nor handle Processing instructions
                break;
            case CHARS :
                addFrame(
                    new ReaderFrame(
                        reader.getState(),
                        reader.getElementId(),
                        reader.getLineNumber(),
                        reader.getValue()));
            default :
                // TODO: throw an exception
        }
    }

    protected void addFrame(ReaderFrame frame) {
        frames.add(frame);
    }
    protected ReaderFrame getFrame(int index) {
        return (ReaderFrame) frames.get(index);
    }
    protected void setFrame(int index) {
        currentFrame = getFrame(index);
        frameIndex = index;
    }
    protected void nextFrame() {
        setFrame(frameIndex + 1);
    }
    public void reset() {
        frameIndex = 0;
        lastRetWasEnd = false;
    }

    static class ReaderFrame {
        QName name;
        int state;
        Attributes attributes;
        String value;
        int elementId;
        int lineNumber;

        ReaderFrame(int state) {
            this.state = state;
            this.name = EMPTY_QNAME;
            this.attributes = null;
            this.value = null;
            this.elementId = -1;
            this.lineNumber = 0;
        }
        ReaderFrame(int state, int elementId, int lineNumber) {
            this(state);
            this.elementId = elementId;
            this.lineNumber = lineNumber;
        }
        ReaderFrame(
            int state,
            int elementId,
            int lineNumber,
            QName name,
            Attributes attributes) {
            this(state, elementId, lineNumber);
            this.name = name;
            this.attributes = attributes;
        }
        ReaderFrame(int state, int elementId, int lineNumber, String value) {
            this(state, elementId, lineNumber);
            this.value = value;
        }
    }

    static class AttributeFrame implements Attributes {
        private static final String XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

        StructMap recordedAttributes = new StructMap();
        List qnames = null;
        List qnameLocalParts = null;
        List values = null;

        AttributeFrame(Attributes attributes) {
            for (int i = 0; i < attributes.getLength(); ++i) {
                recordedAttributes.put(
                    attributes.getName(i),
                    attributes.getValue(i));
            }
        }

        List getQNames() {
            if (qnames == null) {
                qnames = (List) recordedAttributes.keys();
            }

            return qnames;
        }

        //needed for bug 483378
        List getQNameLocalParts() {
            List tempQNames = new ArrayList();

            if (qnames == null) {
                qnames = (List) recordedAttributes.keys();
            }

            if (qnameLocalParts == null)
                qnameLocalParts = new ArrayList();
            //make list of qnameLocalParts(Strings)
            for (int i = 0; i < qnames.size(); i++) {
                QName qname = (QName) qnames.get(i);
                qnameLocalParts.add(qname.getLocalPart());
            }
            return qnameLocalParts;
        }

        List getValues() {
            if (values == null) {
                values = (List) recordedAttributes.values();
            }

            return values;
        }
        public int getIndex(QName name) {
            List qnames = getQNames();

            for (int i = 0; i < qnames.size(); ++i) {
                if (qnames.get(i).equals(name)) {
                    return i;
                }
            }

            return -1;
        }

        public int getIndex(String uri, String localName) {
            List qnames = getQNames();

            for (int i = 0; i < qnames.size(); ++i) {
                QName qname = (QName) qnames.get(i);
                if (qname.getNamespaceURI().equals(uri)
                    && qname.getLocalPart().equals(localName)) {
                    return i;
                }
            }

            return -1;
        }

        public int getIndex(String localName) {
            List qnames = getQNames();

            for (int i = 0; i < qnames.size(); ++i) {
                QName qname = (QName) qnames.get(i);
                if (qname.getLocalPart().equals(localName)) {
                    return i;
                }
            }

            return -1;
        }

        public int getLength() {
            return recordedAttributes.size();
        }

        public String getLocalName(int index) {
            return getName(index).getLocalPart();
        }

        public QName getName(int index) {
            List qnames = getQNames();

            return (QName) qnames.get(index);
        }

        public String getPrefix(int index) {
            QName qname = getName(index);

            return XmlUtil.getPrefix(qname.getNamespaceURI());
        }

        public String getURI(int index) {
            return getName(index).getNamespaceURI();
        }

        public String getValue(int index) {
            if (index == -1) {
                return null;
            }

            List values = getValues();

            return (String) values.get(index);
        }

        public String getValue(QName name) {
            return getValue(getIndex(name));
        }

        public String getValue(String uri, String localName) {
            return getValue(getIndex(uri, localName));
        }

        public String getValue(String localName) {
            return getValue(getIndex(localName));
        }

        public boolean isNamespaceDeclaration(int index) {
            return getURI(index) == XMLNS_NAMESPACE_URI;
        }
    }

    public void close() {
        reset();
    }

    public int getState() {
        return currentFrame.state;
    }

    public QName getName() {
        return currentFrame.name;
    }

    public String getURI() {
        return getName().getNamespaceURI();
    }

    public String getLocalName() {
        return getName().getLocalPart();
    }

    public Attributes getAttributes() {
        return currentFrame.attributes;
    }

    public String getValue() {
        return currentFrame.value;
    }

    public int getElementId() {
        return currentFrame.elementId;
    }

    public int getLineNumber() {
        return currentFrame.lineNumber;
    }

    public String getURI(String prefix) {
        return namespaceSupport.getURI(prefix);
    }

    public Iterator getPrefixes() {
        return namespaceSupport.getPrefixes();
    }

    public int next() {
        if (frameIndex + 1 >= frames.size() - 1) {
            // throw new StreamingException("xmlrecorder.recording.ended");
            return EOF;
        }
        nextFrame();
        int ret = getState();

        if (lastRetWasEnd) {
            namespaceSupport.popContext();
            lastRetWasEnd = false;
        }

        if (ret == START) {
            namespaceSupport.pushContext();
            Attributes attributes = getAttributes();
            for (int i = 0; i < attributes.getLength(); ++i) {
                if (attributes.isNamespaceDeclaration(i)) {
                    String prefix = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    namespaceSupport.declarePrefix(prefix, value);
                }
            }
        } else if (ret == END) {
            lastRetWasEnd = true;
        }

        return ret;
    }

    public XMLReader recordElement() {
        return new RecordedXMLReader(this, namespaceSupport);
    }

    public void skipElement(int elementId) {
        while (!(currentFrame.state == EOF
            || (currentFrame.state == END
                && currentFrame.elementId == elementId))) {
            if (next() == EOF) {
                return;
            }
        }
    }
}
