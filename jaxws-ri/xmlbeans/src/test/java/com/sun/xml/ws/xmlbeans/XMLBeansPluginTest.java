package com.sun.xml.ws.xmlbeans;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.jws.WebParam;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;

import jws.*;
import jws.faults.*;

import com.bea.wli.sb.transports.ejb.test.ejb3.CheckedException;
import com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.xmlbeans.test.MultiXMLBeansSEB;
import com.sun.xml.ws.xmlbeans.test.MultiXMLBeansSEI;
//import com.sun.xml.ws.DummyAnnotations;

public class XMLBeansPluginTest extends WsDatabindingTestBase {
    static String mode = "xmlbeans";
    
    public void testTypedXmlBeansDOC() throws Exception {
        TypedXmlBeansDOCI proxy = createProxy(TypedXmlBeansDOCI.class, TypedXmlBeansDOC.class, mode, true);
        CountriesDocument countries = CountriesDocument.Factory.newInstance();
        CountriesDocument.Countries c = countries.addNewCountries();
        c.addNewCountry().setName("X");
        c.addNewCountry().setName("Y");
        String res = proxy.getCountryName(countries, "foo");
    }

    public void testJwsArrays() throws Exception {
        Class endpointClass = Arrays.class;
        Class proxySEIClass = ArraysI.class;  
        ArraysI proxy = createProxy(ArraysI.class, endpointClass, mode, true);
        {
        String[] req = {"apple", "banana", "orange", "eggplant" };
        String[] res = proxy.sort1(req);
        assertEquals(req.length,res.length);
        }
        {
            String[] arg0 = {"apple", "banana", "orange" };
            int[] intA = {1,2,3};
            Integer[] integerA = {4,5,6}; 
            double[] d1 = {};
            Double[] d2 ={};
            float[] fA  ={}; 
            Float[] flA ={};
                    long[] l1 ={}; Long[] l2 ={};
                    short[] s1 ={}; Short[] s2 ={};           
                    boolean[] ba ={}; Boolean[] bb ={};
                    byte[] b ={}; Byte[] b2 ={}; byte[][] b3 ={{}};
//                    com.bea.xml.XmlObject[] beaXBeans, 
                    javax.xml.namespace.QName[] qn ={};
                    java.math.BigInteger[] bi ={};
                    java.math.BigDecimal[] bd ={};
                    java.util.Calendar[] cal ={};
                    org.apache.xmlbeans.XmlObject[] apacheXBeans ={}; 
                    org.apache.xmlbeans.XmlObject xb = org.apache.xmlbeans.XmlString.Factory.newValue("xmlstring");
//                    com.bea.wli.sb.transports.ejb.test.xbean.MyAddress address, com.bea.wli.sb.transports.ejb.test.xbean.MyAddress[] aa,
                    com.bea.wli.sb.transports.ejb.test.xbean.CountriesDocument[] countries ={};
                    com.bea.wli.sb.transports.ejb.test.xbean.CountryInfoType[] info = {};
//           String[] res = proxy.arrayTest(arg0, intA, integerA, d1, d2, fA, flA, l1, l2, s1, s2, ba, bb, b, b2, b3, qn, bi, bd, cal, apacheXBeans, xb, countries, info);
        }
    }


    public void testXmlbeansDatabindingMode() throws Exception {
        Class endpointClass = DoNothing.class;
        Class proxySEIClass = DoNothingI.class;
        try {
            DoNothingI hp = createProxy(DoNothingI.class, endpointClass, mode, true);
        } catch (DatabindingException e) {
            e.printStackTrace(System.out);
            // expected exception.
        }
    }

    public void testMultiXMLBeansSEI() throws Exception {        
        try {
            MultiXMLBeansSEI hp = createProxy(MultiXMLBeansSEI.class, MultiXMLBeansSEB.class, mode, true);
        } catch (DatabindingException e) {
            e.printStackTrace(System.out);
            // expected exception.
        }
    }


    public void testAppFaults() throws Exception {
        Class endpointClass = AppFaultsSEB.class;
        Class proxySEIClass = AppFaultsSEI.class;  
        AppFaultsSEI proxy = createProxy(AppFaultsSEI.class, endpointClass, mode, true);
        {
        MyBean mybean = new MyBean();
        mybean.setIntCode(123);
        mybean.setIntegerCode(321);
        float[] f = {1f, 2f, 3f};
        mybean.setFloatArray(f);
        mybean.setChildren(new MyBean());
        proxy.echoBean(mybean);
        }
        try {
            proxy.chcEx("AppStrArrayException");
            fail();
        } catch(AppStrArrayException ce) {
        }
        try {
            proxy.chcEx("AppException");
            fail();
        } catch(AppException ce) {
        }
    }


    public void xtestEjbXbean() throws Exception {
        Class endpointClass = com.bea.wli.sb.transports.ejb.Jws.class;
        Class proxySEIClass = com.bea.wli.sb.transports.ejb.JwsI.class;        
        try {
            com.bea.wli.sb.transports.ejb.JwsI hp = createProxy(com.bea.wli.sb.transports.ejb.JwsI.class, endpointClass, mode, true);
        } catch (DatabindingException e) {
            e.printStackTrace(System.out);
            // expected exception.
        }
    }
    public void XtestEjbXbean2() throws Exception {
        Class endpointClass = com.bea.wli.sb.transports.ejb.Jws2.class;
        Class proxySEIClass = com.bea.wli.sb.transports.ejb.Jws2I.class;        
        try {
            com.bea.wli.sb.transports.ejb.Jws2I hp = createProxy(com.bea.wli.sb.transports.ejb.Jws2I.class, endpointClass, mode, true);
        } catch (DatabindingException e) {
            e.printStackTrace(System.out);
            // expected exception.
        }
    }

    public void testXmlObject() throws Exception {
        XmlString xstr = XmlString.Factory.newValue("hello");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        xstr.save(os);
        System.out.println(new String(os.toByteArray()));

//        XmlObject xo = XmlObject.Factory.newInstance();
//        xo.changeType(XmlString.type);
////        xo.set(xstr);
//        XmlOptions xopt = new XmlOptions();
//        xopt.setDocumentType(XmlString.type);
//        xopt.setSaveSyntheticDocumentElement(new QName("tagName"));
//        os = new ByteArrayOutputStream();
//        xo.save(os, xopt);
//        System.out.println(new String(os.toByteArray()));
        String xml = "<a><b1/><b2/></a>";
        ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        XmlObject xo = XmlObject.Factory.parse(is);
        System.out.println(xo.schemaType().getName());
        os = new ByteArrayOutputStream();
        xo.save(os);
        System.out.println(new String(os.toByteArray()));
    }
}
