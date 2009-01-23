package whitebox.jaxb.client;

import junit.framework.TestCase;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.ws.streaming.XMLStreamWriterUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.stream.buffer.XMLStreamBuffer;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.OutputStream;

/**
 * Tests for JAXB.
 *
 * @author Rama Pulavarthi
 */
public class JAXBTest extends TestCase {

    public void testJAXBElementMarshalling() throws Exception {
    JAXBRIContext jc = (JAXBRIContext) JAXBContext.newInstance(whitebox.jaxb.client.DetailType.class);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().newDocument();
        Element e = doc.createElementNS("http://www.example1.com/faults","myFirstDetail");
        e.setTextContent("foo bar");
        doc.appendChild(e);

        DetailType dt = new DetailType();
        dt.getDetails().add(e);
        Marshaller m = jc.createMarshaller();
        m.marshal(dt,System.out);


//        XMLStreamWriter sw = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
//        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
//        // Get output stream and use JAXB UTF-8 writer
//        OutputStream os = XMLStreamWriterUtil.getOutputStream(sw);
//        ((MarshallerImpl)m).marshal(dt,os,sw.getNamespaceContext());
//        sw.flush();

//        Transformer transformer = TransformerFactory.newInstance().newTransformer();;
//        DOMSource source = new DOMSource(doc);
//        StreamResult result = new StreamResult(System.out);
//        transformer.transform(source, result);


        XMLStreamBufferResult sbr = new XMLStreamBufferResult();
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        // Get output stream and use JAXB UTF-8 writer
        //OutputStream os = XMLStreamWriterUtil.getOutputStream(sw);
        ((MarshallerImpl)m).marshal(dt,sbr);
        XMLStreamBuffer infoset = sbr.getXMLStreamBuffer();
        XMLStreamReader reader = infoset.readAsXMLStreamReader();
        if(reader.getEventType()== START_DOCUMENT)
                XMLStreamReaderUtil.nextElementContent(reader);
        verifyDetail(reader);

   }

   private void verifyDetail(XMLStreamReader rdr) throws Exception {
        boolean detail = false;
        while(rdr.hasNext()) {
            int event = rdr.next();
            if (event == XMLStreamReader.START_ELEMENT) {
                if (rdr.getName().getLocalPart().equals("detail") || rdr.getName().getLocalPart().equals("Detail")) {
                    detail = true;
                    XMLStreamReaderUtil.nextElementContent(rdr);    // <myFirstDetail>
                    assertEquals(DETAIL1_QNAME, rdr.getName());
                    XMLStreamReaderUtil.nextElementContent(rdr);    // </myFirstDetail>                
                }
            }
        }
        if (!detail) {
            fail("There is no detail element in the fault");
        }
    }
    private static final QName DETAIL1_QNAME =  new QName("http://www.example1.com/faults", "myFirstDetail");




}