/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
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

package testutil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class is called from ant to create jaxrpc-ri-runtime.xml
 * from jaxrpc-ri.xml. It adds url-pattern and WSDL location to 
 * endpoint element so that Client when run locally can use this 
 * WSDL location and url-pattern for stub generation. 
 * This class puts the information needed by LocalConfigTransformer
 * class to run it locally.
 */

public class MappingJaxrpcRiRuntime {

    /**
     * Must pass in files jaxrpc-ri.xml, config-server.xml
     * and the location to save newly created jaxrpc-ri-runtime.xml file. 
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println(
                "ERROR: need args: jaxrpc-ri.xml,\n"
                    + "config-server.xml,\ntemp dir");
            return;
        }
        try {
            String jaxrpcri = args[0];
            String serverConfig = args[1];
            String newConfig = args[2] + "jaxrpc-ri-runtime.xml";
            String tempdir = args[2];

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();

            Document newdoc = builder.newDocument();
            Element root = (Element) newdoc.createElement("endpoints");
            newdoc.appendChild(root);
            root.setAttribute(
                "xmlns",
                "http://java.sun.com/xml/ns/jax-rpc/ri/runtime");
            root.setAttribute("version", "1.0");

            Comment comment =
                newdoc.createComment(
                    "This xml file is not created by WsDeploy and is just hand-coded from jaxrpc-ri.xml"
                        + " to run cleints locally");

            root.appendChild(comment);

            // get wsdl file names from jaxrpc-ri-runtime.xml
            Document doc = builder.parse(jaxrpcri);
            Element endpointElement =
                (Element) doc.getElementsByTagName("endpoint").item(0);

            Attr wsdlAttr = endpointElement.getAttributeNode("wsdl");

            if (wsdlAttr == null) {
                // starting from SEI, so no wsdl attribute
                // Add wsdl attribute by getting service name from server-config.xml

                Document doc1 = builder.parse(serverConfig);
                Element serviceElement =
                    (Element) doc1.getElementsByTagName("service").item(0);
                String serviceName = serviceElement.getAttribute("name");
                String wsdlLocation = "/WEB-INF/" + serviceName + ".wsdl";
                endpointElement.setAttribute("wsdl", wsdlLocation);
            }

            Element webServicesElement =
                (Element) doc.getElementsByTagName("webServices").item(0);
            Element endpointMappingElement =
                (Element) webServicesElement.getElementsByTagName(
                    "endpointMapping").item(
                    0);
            String urlPatternAttr =
                endpointMappingElement.getAttribute("urlPattern");
            endpointElement.setAttribute("urlpattern", urlPatternAttr);

            Node endpointNode = newdoc.importNode(endpointElement, true);
            root.appendChild(endpointNode);

            // save file
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(
                new DOMSource(newdoc),
                new StreamResult(newConfig));

        } catch (Exception e) {
            System.err.println("exception in JaxrpcRiRuntimeConfigCreator:");
            e.printStackTrace();
        }
    }
}
