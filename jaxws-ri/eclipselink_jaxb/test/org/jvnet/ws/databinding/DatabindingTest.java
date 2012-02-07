package org.jvnet.ws.databinding;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import com.sun.xml.ws.InVmWSDLResolver;


import junit.framework.TestCase;

public class DatabindingTest extends TestCase {
    @WebService
    public static class Hello1 {
        public String echo(String str) { return str; }
    }
    @WebService
    public static interface Hello2 {
        public String echo(String str);
    }
    public void testWsdlGenHello() throws Exception {
        DatabindingFactory fac = DatabindingFactory.newInstance();
//        {
//        Databinding.Builder builder = fac.createBuilder(null, Hello1.class);
//        WSDLGenerator wsdlgen = builder.createWSDLGenerator();
//        wsdlgen.inlineSchema(true);
//        InVmWSDLResolver res = new InVmWSDLResolver();
//        wsdlgen.generate(res);
////        res.print();
//        assertEquals(1, res.getAll().size());
//        }
        //TODO serviceName and portName
        {
        Databinding.Builder builder = fac.createBuilder(Hello2.class, null);
        builder.targetNamespace("mytns");
        builder.serviceName(new QName("mytns", "myservice"));
        WSDLGenerator wsdlgen = builder.createWSDLGenerator();
        wsdlgen.inlineSchema(true);
        InVmWSDLResolver res = new InVmWSDLResolver();
        wsdlgen.generate(res);
        res.print();
        assertEquals(1, res.getAll().size());
        }
    }
}
