/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package whitebox.epr.client;

import com.sun.xml.bind.marshaller.XMLWriter;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferException;
import com.sun.xml.stream.buffer.stax.StreamReaderBufferProcessor;
import static com.sun.xml.ws.api.addressing.AddressingVersion.W3C;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

/**
 * Tests {@link WSEndpointReferenceTest}.
 *
 * @author Kohsuke Kawaguchi
 */
    public class WSEndpointReferenceTest extends XMLTestCase {
    public void testCreateViaSpec() throws Exception {
        W3CEndpointReference spec = new W3CEndpointReference(new StreamSource(
            getClass().getResource("test-epr.xml").toExternalForm()
        ));
        new WSEndpointReference(spec, W3C);
    }

    public void testCreateViaBuffer() throws Exception {
        WSEndpointReference epr = createEPR();
        System.out.println(epr.toString());
    }

    private WSEndpointReference createEPR() throws XMLStreamBufferException, XMLStreamException {
        return new WSEndpointReference(getClass().getResourceAsStream("test-epr.xml"),W3C);
    }

    /**
     * This creates {@link WSEndpointReference} that uses a fragment infoset.
     * We should really fix this in XMLStreamBuffer.
     */
    private WSEndpointReference creaateSubtreeEPR() throws XMLStreamException {
        XMLStreamBuffer xsb = XMLStreamBuffer.createNewBufferFromXMLStreamReader(
            XMLInputFactory.newInstance().createXMLStreamReader(
                getClass().getResourceAsStream("test-epr.xml")));
        StreamReaderBufferProcessor p = xsb.readAsXMLStreamReader();
        XMLStreamBuffer mark = p.nextTagAndMark();

        return new WSEndpointReference(mark,W3C);
    }

    private Reader getReferenceInfoset() {
        return new InputStreamReader(getClass().getResourceAsStream("test-epr.ref.xml"));
    }

    public void testEchoStAX() throws Exception {
        StringWriter w = new StringWriter();
        XMLStreamWriter xmlwriter = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
        createEPR().writeTo("root", xmlwriter);
        xmlwriter.close();
        //System.out.println(w.toString());
        XMLAssert.assertXMLIdentical(compareXML(w.toString(),getReferenceInfoset()),true);
    }

    public void testEchoSAX() throws Exception {
        StringWriter w = new StringWriter();
        createEPR().writeTo("root", new XMLWriter(new PrintWriter(w),"UTF-8"),
            new DefaultHandler(), true
        );
        System.out.println(w.toString());
        XMLAssert.assertXMLIdentical(compareXML(w.toString(),getReferenceInfoset()),true);
    }

    public void testAsSource() throws Exception {
        StringWriter w = new StringWriter();
        XmlUtil.newTransformer().transform(createEPR().asSource("root"),new StreamResult(w));

        XMLAssert.assertXMLIdentical(compareXML(w.toString(),getReferenceInfoset()),true);
    }

    public void testAddress() throws Exception {
        WSEndpointReference epr = createEPR();
        assertEquals("http://example.com/fabrikam/acct",epr.getAddress());
    }

    public void testReplace() throws Exception {
        WSEndpointReference n = createEPR().createWithAddress("newAddress");
        System.out.println(n);
        assertEquals("newAddress",n.getAddress());
        XMLAssert.assertXMLIdentical(compareXML(n.toString(),
            new InputStreamReader(getClass().getResourceAsStream("newAddress.epr.xml"))),true);
    }

    public void testSubtreeToSpec() throws Exception {
        creaateSubtreeEPR().toSpec();
        creaateSubtreeEPR().toSpec(MemberSubmissionEndpointReference.class);
    }
}
