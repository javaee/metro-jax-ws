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

package whitebox.wsam_epr_interop.client;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.util.ServiceFinder;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import junit.framework.TestCase;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.EndpointReference;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * @author Rama Pulavarthi
 */

public class WSAM_EPRTester extends TestCase {
    private final XMLOutputFactory staxOut;

    public WSAM_EPRTester(String name) {
        super(name);
        this.staxOut = XMLOutputFactory.newInstance();
        staxOut.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }
    public void testWSAM_EPR_Interop() throws Exception {
        System.out.println("****************************************");
        System.out.println("Web Services Addressing 1.0 - Metadata");
        System.out.println("Section 2.1 - Interop Tests");
        System.out.println("Sun Microsystems Report - " + new Date());
        System.out.println("****************************************\n");
        runMandatoryEPRTests();
        runOptionalEPRTests();
    }


    private void runMandatoryEPRTests() throws Exception {
        URL res = getClass().getClassLoader().getResource("epr/wsamTest.wsdl");
        File folder = new File(new File(res.getFile()).getParentFile(), "mandatory");   // assuming that this is a file:// URL.
        System.out.println("\n\nMandatory Tests:\n");
        for (File f : folder.listFiles()) {
            if (!f.getName().endsWith(".xml"))
                continue;
            System.out.println("***************************");
            System.out.println("TestFile: " + f.getParentFile().getName() + "/" + f.getName());
            try {
                InputStream is = new FileInputStream(f);
                StreamSource s = new StreamSource(is);
                EndpointReference epr = EndpointReference.readFrom(s);
                WSEndpointReference wsepr = new WSEndpointReference(epr);
                System.out.println("Address: " + wsepr.getAddress());
                WSEndpointReference.Metadata metadata = wsepr.getMetaData();
                System.out.println("Metadata Valid?: true");
                if (metadata.getPortTypeName() != null)
                    System.out.println("InterfaceName: " + metadata.getPortTypeName());
                if (metadata.getServiceName() != null)
                    System.out.println("ServiceName: " + metadata.getServiceName());
                if (metadata.getPortName() != null)
                    System.out.println("Endpoint: " + metadata.getPortName().getLocalPart());
                String wsdliLocation = metadata.getWsdliLocation();
                if (metadata.getWsdliLocation() != null) {
                    System.out.println("wsdli:wsdlLocation: " + wsdliLocation);
                    String wsdlLocation = wsdliLocation.substring(wsdliLocation.lastIndexOf(" "));
                    WSDLModel wsdlModel = RuntimeWSDLParser.parse(new URL(wsdlLocation),
                            new StreamSource(wsdlLocation),
                            XmlUtil.createDefaultCatalogResolver(),
                            false, Container.NONE, ServiceFinder.find(WSDLParserExtension.class).toArray());
                    QName binding = wsdlModel.getBinding(metadata.getServiceName(), metadata.getPortName()).getName();
                    System.out.println("Binding from WSDL: " + binding);

                }
                System.out.println("");
            } catch (Exception e) {
                System.out.println("Metadata Valid?: false");
                System.out.println(e.getMessage());
//                e.printStackTrace();
            }
        }
    }

    private void runOptionalEPRTests() throws Exception {
        URL res = getClass().getClassLoader().getResource("epr/wsamTest.wsdl");
        File folder = new File(new File(res.getFile()).getParentFile(), "optional");   // assuming that this is a file:// URL.
        System.out.println("\n\nOptional Tests:\n");
        for (File f : folder.listFiles()) {
            if (!f.getName().endsWith(".xml"))
                continue;
            System.out.println("***************************");
            System.out.println("TestFile: " + f.getParentFile().getName() + "/" + f.getName());
            try {
                InputStream is = new FileInputStream(f);
                StreamSource s = new StreamSource(is);
                EndpointReference epr = EndpointReference.readFrom(s);
                WSEndpointReference wsepr = new WSEndpointReference(epr);
                System.out.println("Address: " + wsepr.getAddress());
                WSEndpointReference.Metadata metadata = wsepr.getMetaData();
                System.out.println("Metadata Valid?: true");
                if (metadata.getPortTypeName() != null)
                    System.out.println("InterfaceName: " + metadata.getPortTypeName());
                if (metadata.getServiceName() != null)
                    System.out.println("ServiceName: " + metadata.getServiceName());
                if (metadata.getPortName() != null)
                    System.out.println("Endpoint: " + metadata.getPortName().getLocalPart());
                String wsdliLocation = metadata.getWsdliLocation();
                if (metadata.getWsdliLocation() != null) {
                    System.out.println("wsdli:wsdlLocation: " + wsdliLocation);
                    String wsdlLocation = wsdliLocation.substring(wsdliLocation.lastIndexOf(" "));
                    WSDLModel wsdlModel = RuntimeWSDLParser.parse(new URL(wsdlLocation),
                            new StreamSource(wsdlLocation),
                            XmlUtil.createDefaultCatalogResolver(),
                            false, Container.NONE, ServiceFinder.find(WSDLParserExtension.class).toArray());
                    QName binding = wsdlModel.getBinding(metadata.getServiceName(), metadata.getPortName()).getName();
                    System.out.println("Binding from WSDL: " + binding);

                }
                System.out.println("");
            } catch (Exception e) {
                System.out.println("Metadata Valid?: false");
                System.out.println("Reason: "+ e.getMessage());
//                e.printStackTrace();
                System.out.println("");
            }
        }
    }

    public void xtestEPR() throws Exception {
        URL res = getClass().getClassLoader().getResource("epr/mandatory/epr3.xml");
        InputStream is = res.openStream();
        StreamSource s = new StreamSource(is);
        EndpointReference epr = EndpointReference.readFrom(s);
        WSEndpointReference wsepr = new WSEndpointReference(epr);
        WSEndpointReference.Metadata metadata = wsepr.getMetaData();
        System.out.println(metadata.getPortName());
        System.out.println(metadata.getServiceName());
        System.out.println(metadata.getPortTypeName());

    }
}
