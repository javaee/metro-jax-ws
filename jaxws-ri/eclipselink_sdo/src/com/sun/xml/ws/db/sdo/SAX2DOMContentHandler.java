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
