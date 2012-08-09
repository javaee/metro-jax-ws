package whitebox.fault.client;

import java.lang.reflect.Constructor;
import javax.xml.namespace.QName; 
import javax.xml.parsers.*;
import org.w3c.dom.*;

import junit.framework.TestCase;

public class FaultDetailTest extends TestCase {

    public static void testFaultDetailNS() throws Exception {
	Element detail = getDetail("FaultDetail");
	assertNull(detail.getNamespaceURI());
	getFault(detail);
    }

    private static Element getDetail(String name) throws Exception {
	DOMImplementation dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
	Element detail = dom.createDocument("", "FaultDocument", null).createElement(name);
	return detail;       
    }

    private static void getFault(Element detail) throws Exception {
	Class cls = Class.forName("com.sun.xml.ws.fault.SOAP11Fault");
        Class partypes[] = new Class[4];
        partypes[0] = Class.forName("javax.xml.namespace.QName");
        partypes[1] = Class.forName("java.lang.String");
	partypes[2] = Class.forName("java.lang.String");
	partypes[3] = Class.forName("org.w3c.dom.Element");
        Constructor ct = cls.getDeclaredConstructor(partypes);
	ct.setAccessible(true);
        Object arglist[] = new Object[4];
        arglist[0] = new QName("", "FaultCode");
        arglist[1] = "FaultReason";
	arglist[2] = "FaultActor";
	arglist[3] = detail;
	try {        
	    Object retobj = ct.newInstance(arglist);	  
        } catch(NullPointerException e){
	    fail(e.getMessage());
	}	
    }

}
