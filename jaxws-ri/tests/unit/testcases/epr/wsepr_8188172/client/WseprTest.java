/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package epr.wsepr_8188172.client;

import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * @author lingling.guo@oracle.com
 */
public class WseprTest extends TestCase {
	
    private String nameSpaceURI = "http://wseprservice.org/wsdl";
    private QName serviceName = new QName(nameSpaceURI, "WseprService");
    private QName portName = new QName(nameSpaceURI, "WseprPort");
    private QName portTypeName = new QName(nameSpaceURI, "Wsepr");
    private String address = "http://wseprservice.org/Wsepr";
    private List<Element> referenceParameters = new ArrayList<Element>();
    
    public WseprTest(String name) {
    	super(name);
    }
    
    /**
     * Empty ReferenceParameters element should not exist in  EndpointReference element.
     * @throws Exception
     */
    public void testReferenceParameters() throws Exception {   	
    	WSEndpointReference wSEndpointReference = new WSEndpointReference(AddressingVersion.W3C, address, serviceName, portName, portTypeName, null, null, referenceParameters);
    	Node epr = string2Doc(wSEndpointReference.toString(), "wsa:ReferenceParameters");
    	validateEndpointReference(wSEndpointReference.toString());
    	assertNull("EndpointReference element should not contain empty ReferenceParameters element.",epr);  	    	   	   	
    }
    
    /**
     * Empty Metadata element should not exsit in EndpointReference element.
     * @throws Exception
     */
    public void testMetadataWithNullParams() throws Exception {   	
    	WSEndpointReference wSEndpointReference = new WSEndpointReference(AddressingVersion.W3C, address, null, null, null, null, null, referenceParameters);
    	Node md = string2Doc(wSEndpointReference.toString(), "wsa:Metadata");	
    	validateEndpointReference(wSEndpointReference.toString());
    	assertNull("EndpointReference element should not contain empty Metadata element.",md);
    }

    /**
     * Empty Metadata element should not exsit in EndpointReference element.
     * @throws Exception
     */
    public void testMetadataWithEmptyParams() throws Exception {
      WSEndpointReference wSEndpointReference = new WSEndpointReference(AddressingVersion.W3C, address, new QName("",""), new QName("",""), new QName("",""), null, "", referenceParameters);
      Node md = string2Doc(wSEndpointReference.toString(), "wsa:Metadata");
      validateEndpointReference(wSEndpointReference.toString());
      assertNull("EndpointReference element should not contain empty Metadata element.",md);
    }

    private Node string2Doc (String xml, String tagName) throws Exception {
    	DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();	    	
    	InputStream stream = new ByteArrayInputStream(xml.getBytes());    		    	
    	Document doc = db.parse(stream);	    	
    	NodeList nl = doc.getElementsByTagName(tagName);
    	if(nl == null || nl.getLength() == 0) {
    		return null;
    	}
    	return nl.item(0); 	    	
    }
    
    private void validateEndpointReference(String wsepr) throws Exception{    	
    	InputStream is = new ByteArrayInputStream(wsepr.getBytes());
    	WSEndpointReference wSEndpointReference = new WSEndpointReference(is, AddressingVersion.W3C);
    }
}
