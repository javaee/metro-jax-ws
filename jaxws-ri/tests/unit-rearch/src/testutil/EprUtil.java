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

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.transform.dom.DOMResult;
import javax.xml.namespace.QName;
import com.sun.xml.ws.developer.MemberSubmissionEndpointReference;

/**
 * @author Rama Pulavarthi
 */

public class EprUtil extends junit.framework.Assert {
    private static final String W3C_EPR_NS = "http://www.w3.org/2005/08/addressing";
    private static final String W3C_EPR_WSDLBINDING_NS = "http://www.w3.org/2006/05/addressing/wsdl";
    private static final String MS_EPR_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
    public static final QName WSDL_DEFINITIONS_NAME = new QName("http://schemas.xmlsoap.org/wsdl/", "definitions");

    private static final String W3C_EPR_METADATA_NS = "http://www.w3.org/2007/05/addressing/metadata";
    public static final String WSAM_WSDLI_ATTRIBUTE_NAMESPACE="http://www.w3.org/ns/wsdl-instance";
    public static final String WSAM_WSDLI_ATTRIBUTE_LOCALNAME="wsdlLocation";

    @Deprecated
    public static boolean validateEPR(Node node, Class epr, String endpointAddress, QName serviceName,
                                      QName portName, QName portTypeName, boolean hasWSDL) {
        //Default validation is as per Metadata NS;
        return validateEPR(node,epr,endpointAddress,serviceName,portName, portTypeName,hasWSDL,false,null);

    }

    public static boolean validateEPR(Node node, Class epr, String endpointAddress, QName serviceName,
                                      QName portName, QName portTypeName, String wsdlLocation) {
        return validateEPR(node,epr,endpointAddress,serviceName,portName, portTypeName,(wsdlLocation!=null)?true:false,false,wsdlLocation);

    }
    public static boolean validateEPR(Node node, Class epr, String endpointAddress, QName serviceName,
                                      QName portName, QName portTypeName, String wsdlLocation, boolean useWsdlBindingNS) {
        return validateEPR(node,epr,endpointAddress,serviceName,portName, portTypeName,(wsdlLocation!=null)?true:false,false,wsdlLocation);

    }

    private static boolean validateEPR(Node node, Class epr, String endpointAddress, QName serviceName,
                                      QName portName, QName portTypeName, boolean hasWSDL, boolean useWsdlBindingNS, String wsdlLocation) {
        if (node.getNodeType() == Node.DOCUMENT_NODE)
            node = node.getFirstChild();

        if (epr.isAssignableFrom(W3CEndpointReference.class)) {
            String W3C_EPR_WSDL_NS;
            if(useWsdlBindingNS)
               W3C_EPR_WSDL_NS = W3C_EPR_WSDLBINDING_NS;
            else
                W3C_EPR_WSDL_NS = W3C_EPR_METADATA_NS;

            assertTrue(node.getNamespaceURI().equals(W3C_EPR_NS) &&
                    node.getLocalName().equals("EndpointReference"));

            Element elm = (Element) node;

            Element add = XmlUtil.getFirstElementChild(node);
            String value = XmlUtil.getTextForNode(add);
            assertTrue(value.equals(endpointAddress));
            if(serviceName == null && portName == null && !hasWSDL) {
                // Need not check metadata
                return true;
            }
            NodeList nl = elm.getElementsByTagNameNS(W3C_EPR_NS, "Metadata");
            assertTrue(nl.getLength() == 1);
            Element metdata = (Element) nl.item(0);
            String qname;
            String prefix;
            String name;
            String ns;
            //service
            if(serviceName != null) {
                nl = metdata.getElementsByTagNameNS(W3C_EPR_WSDL_NS, "ServiceName");
                assertTrue(nl.getLength() == 1);
                Node service = nl.item(0);
                qname = XmlUtil.getTextForNode(service);
                prefix = XmlUtil.getPrefix(qname);
                name = XmlUtil.getLocalPart(qname);
                ns = service.lookupNamespaceURI(prefix);
                assertEquals(ns, serviceName.getNamespaceURI());
                assertEquals(name, serviceName.getLocalPart());
                //port
                if (portName != null) {
                    String port = ((Element) service).getAttribute("EndpointName");
                    assertEquals(port, portName.getLocalPart());
                }
            }
            if(portTypeName != null) {
                //validate portType
                nl = metdata.getElementsByTagNameNS(W3C_EPR_WSDL_NS, "InterfaceName");
                assertTrue(nl.getLength() == 1);
                Node portType = nl.item(0);
                qname = XmlUtil.getTextForNode(portType);
                prefix = XmlUtil.getPrefix(qname);
                name = XmlUtil.getLocalPart(qname);
                ns = portType.lookupNamespaceURI(prefix);
                assertEquals(ns, portTypeName.getNamespaceURI());
                assertEquals(name, portTypeName.getLocalPart());

            }
            
            if (hasWSDL) {
                //validate WSDL
                if (useWsdlBindingNS) {
                    nl = metdata.getElementsByTagNameNS(WSDL_DEFINITIONS_NAME.getNamespaceURI(), WSDL_DEFINITIONS_NAME.getLocalPart());
                    assertTrue(nl.getLength() > 0);
                    Node wsdl = nl.item(0);
                    //TODO:What else to do to validate this WSDL?
                } else {
                    String wsdliLocation = metdata.getAttributeNS(WSAM_WSDLI_ATTRIBUTE_NAMESPACE, WSAM_WSDLI_ATTRIBUTE_LOCALNAME);
                    assertNotNull(wsdliLocation);
                    String tns = wsdliLocation.substring(0,wsdliLocation.indexOf(' '));
                    String wsdlAddress = wsdliLocation.substring(wsdliLocation.indexOf(' ')+1);
                    if(serviceName != null)
                        assertEquals(serviceName.getNamespaceURI(),tns);
                    if(wsdlAddress != null)
                        assertEquals(wsdlLocation,wsdlAddress);

                }

            }
            return true;
        } else if (epr.isAssignableFrom(MemberSubmissionEndpointReference.class)) {
            assertTrue(node.getNamespaceURI().equals(MS_EPR_NS) &&
                    node.getLocalName().equals("EndpointReference"));

            Element elm = (Element) node;

            Element add = XmlUtil.getFirstElementChild(node);
            String value = XmlUtil.getTextForNode(add);
            assertTrue(value.equals(endpointAddress));
            NodeList nl;
            String qname;
            String prefix;
            String name;
            String ns;
            //service
            if (serviceName != null) {
                nl = elm.getElementsByTagNameNS(MS_EPR_NS, "ServiceName");
                assertTrue(nl.getLength() == 1);
                Node service = nl.item(0);
                qname = XmlUtil.getTextForNode(service);
                prefix = XmlUtil.getPrefix(qname);
                name = XmlUtil.getLocalPart(qname);
                ns = service.lookupNamespaceURI(prefix);
                assertEquals(ns, serviceName.getNamespaceURI());
                assertEquals(name, serviceName.getLocalPart());
                //port
                if (portName != null) {
                    String port = ((Element) service).getAttribute("PortName");
                    assertEquals(port, portName.getLocalPart());
                }
            }

            if (hasWSDL) {
                //validate portType
                nl = elm.getElementsByTagNameNS(MS_EPR_NS, "PortType");
                assertTrue(nl.getLength() == 1);
                Node portType = nl.item(0);
                qname = XmlUtil.getTextForNode(portType);
                prefix = XmlUtil.getPrefix(qname);
                name = XmlUtil.getLocalPart(qname);
                ns = portType.lookupNamespaceURI(prefix);
                assertEquals(ns, portTypeName.getNamespaceURI());
                assertEquals(name, portTypeName.getLocalPart());
            }
            if (hasWSDL) {
                //validate WSDL
                nl = elm.getElementsByTagNameNS(WSDL_DEFINITIONS_NAME.getNamespaceURI(), WSDL_DEFINITIONS_NAME.getLocalPart());
                assertTrue(nl.getLength() > 0);
                Node wsdl = nl.item(0);
                //TODO:What else to do to validate this WSDL?
            }

            return true;
        }
        return false;
    }

    public static boolean validateEPR(EndpointReference epr, String endpointAddress, QName serviceName,
                                      QName portName, QName portTypeName, boolean hasWSDL) {
        assertTrue(epr != null);
        DOMResult dr = new DOMResult();
        epr.writeTo(dr);
        Node node = dr.getNode();
        return validateEPR(node, epr.getClass(), endpointAddress, serviceName, portName, portTypeName, hasWSDL);
    }

}
