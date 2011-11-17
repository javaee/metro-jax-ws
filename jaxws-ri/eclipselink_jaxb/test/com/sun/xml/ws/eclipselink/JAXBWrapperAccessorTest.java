package com.sun.xml.ws.eclipselink;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.databinding.Databinding;
import org.jvnet.ws.databinding.DatabindingFactory;
import org.jvnet.ws.databinding.DatabindingModeFeature;

import com.sun.xml.ws.spi.db.JAXBWrapperAccessor;
import com.sun.xml.ws.test.*;
import junit.framework.TestCase;

public class JAXBWrapperAccessorTest extends TestCase {
    public void testJAXBWrapperAccessorCreation() {
        JAXBWrapperAccessor jwa = null;
        jwa = new JAXBWrapperAccessor(com.sun.xml.ws.test.BaseStruct.class);
        assertNotNull(jwa.getPropertyAccessor("", "floatMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "floatMessage"));

        jwa = new JAXBWrapperAccessor(com.sun.xml.ws.test.ExtendedStruct.class);
        assertNotNull(jwa.getPropertyAccessor("", "shortMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "floatMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "anotherIntMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "intMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "stringMessage"));

        jwa = new JAXBWrapperAccessor(
                com.sun.xml.ws.test.MoreExtendedStruct.class);
        assertNotNull(jwa.getPropertyAccessor("", "shortMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "floatMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "anotherIntMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "intMessage"));
        assertNotNull(jwa.getPropertyAccessor("", "stringMessage"));
    }

    public void testDatabindingCreation() {
        Class<?> sei = DocServicePortType.class;
        DatabindingFactory fac = DatabindingFactory.newInstance();
        Databinding.Builder b = fac.createBuilder(sei, null);
        DatabindingModeFeature dbf = new DatabindingModeFeature(
                "eclipselink.jaxb");
        WebServiceFeature[] f = { dbf };
        b.feature(f);
        String ns = "http://performance.bea.com";
        b.serviceName(new QName(ns, "DocService"));
        b.portName(new QName(ns, "DocServicePortTypePort"));
        assertNotNull(b.build());

    }
}
