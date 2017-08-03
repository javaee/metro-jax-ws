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

package whitebox.domutil.client;

import com.sun.xml.ws.streaming.DOMStreamReader;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

/**
 * Tests {@link DOMStreamReader}.
 *
 * @author Kohsuke Kawaguchi
 */
public class DOMStreamReaderTest extends TestCase implements XMLStreamConstants {

    DocumentBuilder db;

    protected void setUp() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        db = dbf.newDocumentBuilder();
    }

    public void test1() throws Exception {
        String sample = "<?xml version='1.0' encoding='UTF-8'?><env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'><env:Body><env:Fault><faultcode>env:Server</faultcode><faultstring>Internal server error</faultstring></env:Fault></env:Body></env:Envelope>";
        Document dd = db.parse(new ByteArrayInputStream(sample.getBytes("UTF-8")));

        scanAll(dd);
    }

    public void test2() throws Exception {
        scanAll(buildBrokenTree());
    }

    /**
     * Makes sure that namespace URIs are properly repaired.
     */
    public void testNamespaceFix() throws Exception {
        DOMStreamReader sr = new DOMStreamReader(buildBrokenTree());
        sr.nextTag();
        assertEquals(1,sr.getNamespaceCount());
        assertEquals("foo",sr.getNamespaceURI(0));
        assertEquals("",sr.getNamespacePrefix(0));

        sr.nextTag();
        assertEquals(1,sr.getNamespaceCount());
        assertEquals("test",sr.getNamespaceURI(0));
        assertEquals("p",sr.getNamespacePrefix(0));
    }

    /**
     * Makes sure that adjacent text are concatanated.
     *
     * See https://jax-ws.dev.java.net/issues/show_bug.cgi?id=160
     */
    public void testAdjacentText() throws Exception {
        Document dd = db.newDocument();
        Element root = dd.createElementNS("foo", "bar");
        dd.appendChild(root);
        for( int i=0; i<3; i++ )
            root.appendChild(dd.createTextNode("foo"));

        DOMStreamReader r = new DOMStreamReader(dd);
        r.nextTag();
        r.next();

        assertEquals("foofoofoo",r.getText());
        r.next();
        assertEquals(END_ELEMENT,r.getEventType());
    }

    private Document buildBrokenTree() {
        Document dd = db.newDocument();
        Element root = dd.createElementNS("foo", "bar");
        dd.appendChild(root);
        root.appendChild(dd.createElementNS("test","p:test"));
        return dd;
    }

    private void scanAll(Document dd) throws XMLStreamException {
        DOMStreamReader dsr = new DOMStreamReader(dd);
        while (dsr.hasNext()) {
            System.out.println("dsr.next() = " + dsr.next());
            if (dsr.getEventType() == START_ELEMENT || dsr.getEventType() == END_ELEMENT) {
                System.out.println("dsr.getName = " + dsr.getName());
                if (dsr.getEventType() == START_ELEMENT) {
                    System.out.println("dsr.getAttributeCount() = " + dsr.getAttributeCount());
                    System.out.println("dsr.getNamespaceCount() = " + dsr.getNamespaceCount());
                }
            }
        }
    }
}
