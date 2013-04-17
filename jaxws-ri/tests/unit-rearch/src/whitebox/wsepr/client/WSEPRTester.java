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

package whitebox.wsepr.client;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import junit.framework.TestCase;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Rama Pulavarthi
 */

public class WSEPRTester extends TestCase {
    private final XMLOutputFactory staxOut;

    public WSEPRTester(String name) {
        super(name);
        this.staxOut = XMLOutputFactory.newInstance();
        staxOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }

    public void testEPRs() throws Exception {
        URL res = getClass().getResource("../epr/ms_epr_metadata.xml");
        File folder = new File(res.getFile()).getParentFile();   // assuming that this is a file:// URL.

        for (File f : folder.listFiles()) {
            System.out.println("***********"+ f.getName() + "***********");
            if(!f.getName().endsWith(".xml"))
                continue;
            InputStream is = new FileInputStream(f);
            StreamSource s = new StreamSource(is);
            EndpointReference epr = EndpointReference.readFrom(s);
            WSEndpointReference wsepr = new WSEndpointReference(epr);
            WSEndpointReference.Metadata metadata = wsepr.getMetaData();
            if (f.getName().equals("w3c_epr_identity.xml")) {
                QName q = new QName("http://schemas.xmlsoap.org/ws/2006/02/addressingidentity", "Identity");
                WSEndpointReference.EPRExtension eprExtn = wsepr.getEPRExtension(q);
                XMLStreamReader xsr = eprExtn.readAsXMLStreamReader();
                if (xsr.getEventType() == XMLStreamConstants.START_DOCUMENT)
                    xsr.next();
                assertEquals(q.getNamespaceURI(), xsr.getNamespaceURI());
                assertEquals(q.getLocalPart(),xsr.getLocalName());
            }
        }
    }
}
