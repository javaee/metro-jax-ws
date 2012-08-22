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

package fromwsdl.freeze.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

//import com.example.globalcompany.ns.orderbookingservice.OrderProcessor;
//import com.example.globalcompany.ns.orderbookingservice.OrderProcessorService;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.model.wsdl.WSDLModelImpl;
import com.sun.xml.ws.model.wsdl.WSDLOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortTypeImpl;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

import junit.framework.TestCase;

public class FODTest extends TestCase {
//	OrderProcessorService service = null;
	
	public void testFreezeFOD() throws Exception {
		/*
		 * Verify that we can get messages from a port type by walking the model.
		 * 
		 */
		//File f = new File("testcases/fromwsdl/freeze/concrete.wsdl");
//		System.out.println(f.getAbsolutePath());
		
//		URL wsdl = new URL("file:/scratch/bnaugle/bugs/fod/v2/concrete.wsdl");
//		URL wsdl = new URL("file:/scratch/bnaugle/bugs/fod/FusionOrderDemoShared/services/orderbooking/output/concrete.wsdl");
		String WSDL_NAME = "concrete.wsdl";  
		Source wsdlSource = getSource(WSDL_NAME);
		
		WSDLModelImpl model = RuntimeWSDLParser.parse(getURL(WSDL_NAME), wsdlSource, XmlUtil.createDefaultCatalogResolver(), true, Container.NONE, new WSDLParserExtension[]{});
		Map<QName, WSDLPortTypeImpl> portTypes = model.getPortTypes();
		Set<QName> keySet = portTypes.keySet();
		for (QName name : keySet) {
			WSDLPortTypeImpl pt = portTypes.get(name);
			System.out.println(name.toString() + portTypes.get(name));
			Iterable<WSDLOperationImpl> operations = pt.getOperations();
			for (WSDLOperationImpl operation : operations)  {
				assertNotNull(operation.getInput().getMessage());
			
			}
		}


	}
    private URL getURL(String file) throws Exception {
        return getClass().getClassLoader().getResource(file);
       
    }

	   private StreamSource getSource(String file) throws Exception {
	        InputStream is = getClass().getClassLoader().getResourceAsStream(file);
	        return new StreamSource(is, getURL(file).toString());
	    }

}
