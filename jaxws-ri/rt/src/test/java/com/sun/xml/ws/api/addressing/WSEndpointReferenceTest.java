/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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
