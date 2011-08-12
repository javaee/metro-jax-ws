package fromwsdl.freeze.client;

import java.io.File;
import java.io.IOException;
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
	
	public void testFreezeFOD() throws IOException, XMLStreamException, SAXException {
		/*
		 * Verify that we can get messages from a port type by walking the model.
		 * 
		 */
		File f = new File("testcases/fromwsdl/freeze/concrete.wsdl");
		System.out.println(f.getAbsolutePath());
		URL wsdl = f.toURL();
//		URL wsdl = new URL("file:/scratch/bnaugle/bugs/fod/v2/concrete.wsdl");
//		URL wsdl = new URL("file:/scratch/bnaugle/bugs/fod/FusionOrderDemoShared/services/orderbooking/output/concrete.wsdl");
		Source wsdlSource = new StreamSource(wsdl.toString());
		WSDLModelImpl model = RuntimeWSDLParser.parse(wsdl, wsdlSource, XmlUtil.createDefaultCatalogResolver(), true, Container.NONE, new WSDLParserExtension[]{});
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

}
