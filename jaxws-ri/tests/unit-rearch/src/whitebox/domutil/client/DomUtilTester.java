/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package whitebox.domutil.client;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.message.saaj.SAAJMessage;
import com.sun.xml.ws.util.DOMUtil;
import junit.framework.TestCase;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Rama Pulavarthi
 */

public class DomUtilTester extends TestCase {
    private final XMLOutputFactory staxOut;
    final File folder = new File(System.getProperty("tempdir") + "/classes/soapmessages");

    public DomUtilTester(String name) {
        super(name);
        this.staxOut = XMLOutputFactory.newInstance();
        staxOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }

    public void testSOAPEnvelope1() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            DOMSource src = makeDomSource(f);
            Node node = src.getNode();
            XMLStreamWriter writer = staxOut.createXMLStreamWriter(new PrintStream(System.out));
            DOMUtil.serializeNode((Element) node.getFirstChild(), writer);
            writer.close();
            assert(true);
            System.out.println("*****************************************");
        }
    }

    public void testSOAPEnvelope1_1() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            DOMSource src = makeDomSource(f);
            Node node = src.getNode();
            XMLStreamWriter writer = XMLStreamWriterFactory.create(System.out);
            DOMUtil.serializeNode((Element) node.getFirstChild(), writer);
            writer.close();
            assert(true);
            System.out.println("*****************************************");
        }
    }

    public void testSOAPEnvelope2() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            SOAPMessage soapmsg = getSOAPMessage(f);
            SAAJMessage saajmessage = new SAAJMessage(soapmsg);
            XMLStreamWriter writer = XMLStreamWriterFactory.create(System.out);
            saajmessage.writeTo(writer);
            writer.close();
            assert(true);
            System.out.println("*****************************************");
        }
    }

    public void testSOAPEnvelope2_1() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            SOAPMessage soapmsg = getSOAPMessage(f);
            SAAJMessage saajmessage = new SAAJMessage(soapmsg);
            XMLStreamWriter writer = staxOut.createXMLStreamWriter(new PrintStream(System.out));
            saajmessage.writeTo(writer);
            writer.close();
            assert(true);
            System.out.println("*****************************************");
        }
    }

    public void testSOAPEnvelope3() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            SOAPMessage soapmsg = getSOAPMessage(f);
            soapmsg.writeTo(System.out);
            assert(true);
            System.out.println("*****************************************");
        }
    }


    public static DOMSource makeDomSource(File f) throws Exception {
        InputStream is = new FileInputStream(f);
        DOMSource domSource = new DOMSource(createDOMNode(is));
        return domSource;
    }

    public static void printNode(Node node) {
        DOMSource source = new DOMSource(node);
        String msgString = null;
        try {
            Transformer xFormer = TransformerFactory.newInstance().newTransformer();
            xFormer.setOutputProperty("omit-xml-declaration", "yes");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            Result result = new StreamResult(outStream);
            xFormer.transform(source, result);
            outStream.writeTo(System.out);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Node createDOMNode(InputStream inputStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            try {
                return builder.parse(inputStream);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException pce) {
            IllegalArgumentException iae = new IllegalArgumentException(pce.getMessage());
            iae.initCause(pce);
            throw iae;
        }
        return null;
    }

    public SOAPMessage getSOAPMessage(SOAPVersion version, Source msg) throws Exception {
        MessageFactory factory = version.saajMessageFactory;
        SOAPMessage message = factory.createMessage();
        message.getSOAPPart().setContent((Source) msg);
        message.saveChanges();
        return message;
    }

    public SOAPMessage getSOAPMessage(File f) throws Exception {
        SOAPVersion version = SOAPVersion.SOAP_11;
        if (f.getName().endsWith("_12.xml")) {
            version = SOAPVersion.SOAP_12;
        }
        MessageFactory mf = version.saajMessageFactory;
        SOAPMessage sm = mf.createMessage(null, new FileInputStream(f));
        return sm;
    }
}
