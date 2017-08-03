/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Martin Grebac
 */
public class WSDLAddressPatcher extends Task {

    private String address;
    private File wsdl;

    /**
     * New address to be written into WSDL.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * WSDL file to be patched.
     */
    public void setWsdl(File wsdl) {
        this.wsdl = wsdl;
    }

    public void execute() throws BuildException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(this.wsdl);

            patch(doc.getDocumentElement());

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource();
            StreamResult result = new StreamResult(this.wsdl);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * @param e Element that we are visiting.
     */
    private void patch(Element e) {

        NodeList ports = e.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/", "port");
        for (int i=0; i<ports.getLength(); i++) {
            Element portE = (Element) ports.item(i);
            String portName = portE.getAttribute("name");
            Element addrE = (Element) portE.getElementsByTagName("address").item(0);
            String location = addrE.getAttribute("location");
            if (location != null) {
                addrE.removeAttribute("location");
                addrE.setAttribute("location", address.replace('\\','/').replace("#PORTNAME#",portName));
            }
            
            NodeList eprs = e.getElementsByTagNameNS("http://www.w3.org/2005/08/addressing", "EndpointReference");
            Element eprE = (Element) ((eprs != null && eprs.getLength()>0) ? eprs.item(0) : null);
            if (eprE != null) {
                NodeList addresses = eprE.getElementsByTagNameNS("http://www.w3.org/2005/08/addressing", "Address");
                Element eprAddrE = (Element) ((addresses != null && addresses.getLength() > 0) ? addresses.item(0) : null);
                if (eprAddrE != null) {
                    eprAddrE.setTextContent(address.replace('\\', '/').replace("#PORTNAME#", portName));
                }
            }
        }
    }
}
