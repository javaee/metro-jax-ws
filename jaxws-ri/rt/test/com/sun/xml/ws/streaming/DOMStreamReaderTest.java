/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
