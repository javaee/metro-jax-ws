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
                        xwriter.writeAttribute(prefix, uri, localname, value);
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

