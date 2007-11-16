package com.sun.xml.ws.streaming;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class DOMStreamReaderTest extends TestCase {
    /**
     * https://jax-ws.dev.java.net/issues/show_bug.cgi?id=464
     */
    public void test464() throws Exception {
        XMLStreamReader r = load("issue464.xml");

        r.nextTag();
        assertEquals(1,r.getNamespaceCount());
        r.nextTag();
        assertEquals("elem2",r.getLocalName());
        assertEquals(0,r.getNamespaceCount());
        r.nextTag();
        assertEquals("elem2",r.getLocalName());
        assertEquals(0,r.getNamespaceCount());
        r.nextTag();
        assertEquals(1,r.getNamespaceCount());
    }

    /**
     * https://wsit.dev.java.net/issues/show_bug.cgi?id=727
     */
    public void test727() throws Exception {
        XMLStreamReader r = load("wsit-727.xml");

        while(r.hasNext()) {
            switch(r.next()) {
            case XMLStreamConstants.START_ELEMENT:
            case XMLStreamConstants.END_ELEMENT:
                // call some of the stream reader methods to make it do some work
                assertEquals(0,r.getNamespaceCount());
                r.getLocalName();
                assertEquals("",r.getNamespaceURI());
                break;
            }
        }
    }

    private XMLStreamReader load(String resourceName) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document dom = dbf.newDocumentBuilder().parse(getClass().getResourceAsStream(resourceName));
        return new DOMStreamReader(dom);
    }
}
