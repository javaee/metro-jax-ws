package com.sun.xml.ws.db.xmlbeans;

import static javax.xml.stream.XMLStreamConstants.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.ws.WebServiceException;

import org.xml.sax.Attributes;

//Duplicates com.sun.xml.ws.streaming.XMLStreamReaderUtil to avoid depending on non-API
public class XMLUtil {

    public static void verifyTag(XMLStreamReader reader, String namespaceURI, String localName) {
        if (!localName.equals(reader.getLocalName()) || !namespaceURI.equals(reader.getNamespaceURI())) {
            throw new WebServiceException( "UnexpectedState.tag - expected: " +
                    "{" + namespaceURI + "}" + localName + " got " +
                    "{" + reader.getNamespaceURI() + "}" + reader.getLocalName());
        }
    }
    
    public static void verifyTag(XMLStreamReader reader, QName name) {
        verifyTag(reader, name.getNamespaceURI(), name.getLocalPart());
    }

    
    public static void toNextTag(XMLStreamReader reader, QName name) {
        // skip any whitespace
        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT &&
                reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            XMLUtil.nextElementContent(reader);
        }
        if(reader.getEventType() == XMLStreamConstants.END_ELEMENT && name.equals(reader.getName())) {
            XMLUtil.nextElementContent(reader); 
        }
    }


    public static int nextElementContent(XMLStreamReader reader) {
        int state = nextContent(reader);
        if (state == CHARACTERS) {
            throw new WebServiceException("xmlreader.unexpectedCharacterContent " + reader.getText());
        }
        return state;
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
            throw new WebServiceException(e);
        }
    }
    
    static public Attributes emptyAttributes() {
        return new Attributes() {
            @Override public int getLength() { return 0; }
            @Override public String getURI(int index) { return null; }
            @Override public String getLocalName(int index)  { return null; }
            @Override public String getQName(int index) { return null; }
            @Override public String getType(int index) { return null; }
            @Override public String getValue(int index)  { return null; }
            @Override public int getIndex(String uri, String localName)  { return 0; }
            @Override public int getIndex(String qName) {  return 0; }
            @Override public String getType(String uri, String localName)  { return null; }
            @Override public String getType(String qName)  { return null; }
            @Override public String getValue(String uri, String localName)  { return null; }
            @Override public String getValue(String qName)  { return null; }
        };
    }
}
