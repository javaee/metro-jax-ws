/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.soap12.mtom_optional.client;

import junit.framework.TestCase;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.handler.MessageContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Rama Pulavarthi
 */
public class MtomTest extends TestCase {

    //test for WSIT 1069, makes sure Mtom is enabled and this happens when the WSDL has the necessary policy assertion.
    public void testMtom() throws Exception {
        MtomSample proxy = new MtomSampleService().getMtomSamplePort();
        Source output = proxy.echo(getSource("sample_doc.xml"));
        Map<String, List<String>> response_headers =
        (Map<String, List<String>>)((BindingProvider)proxy).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
        String s = response_headers.get("Content-Type").get(0);
        assertTrue(s.startsWith("multipart/related"));
        assertTrue(s.contains("type=\"application/xop+xml\""));
        
    }

    //test for WSIT 1062
    public void testMtomOptionality() throws Exception {
        MtomSample proxy = new MtomSampleService().getMtomSamplePort(new MTOMFeature(false));
        Source output = proxy.echo(getSource("sample_doc.xml"));
        Map<String, List<String>> response_headers =
        (Map<String, List<String>>)((BindingProvider)proxy).getResponseContext().get(MessageContext.HTTP_RESPONSE_HEADERS);
        String s = response_headers.get("Content-Type").get(0);
        System.out.println(s);
        assertTrue(s.startsWith("application/soap+xml"));
        assertTrue(!s.contains("type=\"application/xop+xml\""));
    }

    //test for WSIT 1062, testing wsp:Optional="true" in the wsdl
    public void testMtomPolicyOptionality() throws Exception {
        MtomSample proxy = new MtomSampleService().getMtomSamplePort(new MTOMFeature(false));
        String address = (String) ((BindingProvider) proxy).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document wsdl = db.parse(address + "?wsdl");

        Element el = (Element) wsdl.getElementsByTagNameNS(
                "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization","OptimizedMimeSerialization").item(0);
        String optional = el.getAttributeNS("http://www.w3.org/ns/ws-policy","Optional");
        assertTrue(Boolean.valueOf(optional));



    }
    
    private StreamSource getSource(String file) throws Exception {
           InputStream is = getClass().getClassLoader().getResourceAsStream(file);
           return new StreamSource(is);
       }

       private InputStream getResource(String file) throws Exception {
           InputStream is = getClass().getClassLoader().getResourceAsStream(file);
           return is;
       }

}
