package com.sun.xml.ws.streaming;

import junit.framework.TestCase;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Kohsuke Kawaguchi
 */
public class DOMStreamReaderTest extends TestCase {
    /**
     * https://jax-ws.dev.java.net/issues/show_bug.cgi?id=464
     */
    public void test464() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document dom = dbf.newDocumentBuilder().parse(getClass().getResourceAsStream("issue464.xml"));
        XMLStreamReader r = new DOMStreamReader(dom);

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
}
