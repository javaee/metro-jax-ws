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

package whitebox.xmlutil.client;

import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import com.sun.xml.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author Rama Pulavarthi
 */

public class XMLUtilTester extends TestCase {
    private final XMLOutputFactory staxOut;
    private final XMLInputFactory staxIn;
    final File folder = new File(System.getProperty("tempdir") + "/classes/xmldoc");

    public XMLUtilTester(String name) {
        super(name);
        this.staxOut = XMLOutputFactory.newInstance();
        this.staxIn = XMLInputFactory.newInstance();
        staxOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }

    public void testXMLStreamReaderToXMLStreamWriter() throws Exception {
        for (File f : folder.listFiles()) {
            System.out.println("\n\n***********"+ f.getName() + "***********");
            XMLStreamReaderToXMLStreamWriter readerToWriter = new XMLStreamReaderToXMLStreamWriter();
            XMLStreamReader reader = staxIn.createXMLStreamReader(new FileInputStream(f));
            final ByteOutputStream bos = new ByteOutputStream();
            XMLStreamWriter writer = staxOut.createXMLStreamWriter(bos);
            writer.writeStartDocument();
            readerToWriter.bridge(reader,writer);
            writer.writeEndDocument();
            writer.close();
            reader.close();
            printStream(bos.newInputStream());
            System.out.println("\n*****************************************");
        }
    }

    public static void printStream(InputStream is) {
        StreamSource source = new StreamSource(is);
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
}
