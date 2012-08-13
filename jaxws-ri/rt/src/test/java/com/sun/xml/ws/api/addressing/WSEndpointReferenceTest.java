package com.sun.xml.ws.api.addressing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class WSEndpointReferenceTest extends TestCase {

  public void testAddressingMetadataElement() throws Exception {
    WSEndpointReference erf = 
        new WSEndpointReference(AddressingVersion.W3C, AddressingVersion.W3C.anonymousUri,
            new QName("http://org.test","simpleService"), null, null, new ArrayList<Element>(), "http://localhost/", null);
    Source source = erf.asSource("Metadata");
    TransformerFactory tfactory = TransformerFactory.newInstance();
    javax.xml.transform.Transformer xform = tfactory.newTransformer();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(os);
    xform.transform(source, result);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(true);
    try {
      Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(os.toByteArray()));
      assertNotNull(document);
    } catch (Exception e) {
      e.printStackTrace();
      fail("No Exception should have been thrown");
    }
  }
}
