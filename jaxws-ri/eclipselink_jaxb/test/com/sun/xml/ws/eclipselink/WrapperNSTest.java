package com.sun.xml.ws.eclipselink;

import static javax.jws.soap.SOAPBinding.Style.RPC;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;

import java.lang.reflect.Method;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceFeature;

import junit.framework.TestCase;

import org.jvnet.ws.databinding.Databinding;
import org.jvnet.ws.databinding.DatabindingFactory;
import org.jvnet.ws.databinding.JavaCallInfo;
import org.jvnet.ws.message.MessageContext;
import org.w3c.dom.Node;

import com.sun.xml.ws.api.databinding.DatabindingModeFeature;

public class WrapperNSTest extends TestCase {

    @WebService(targetNamespace = "http://echo.org/")
    @SOAPBinding(style = RPC, use = LITERAL)
    static public interface MyHelloRPC {
        public String echoString(String str);
    }

    public void testWrapperNS() throws Exception {
        Class<?> sei = MyHelloRPC.class;
        DatabindingFactory fac = DatabindingFactory.newInstance();
        Databinding.Builder b = fac.createBuilder(sei, null);
        DatabindingModeFeature dbf = new DatabindingModeFeature(
                "eclipselink.jaxb");
        WebServiceFeature[] f = { dbf };
        b.feature(f);
        b.serviceName(new QName("http://echo.org/", "helloService"));
        b.portName(new QName("http://echo.org/", "helloPort"));
        Databinding db = b.build();

        {
            Method method = findMethod(sei, "echoString");
            Object[] args = { "test" };
            JavaCallInfo call = db.createJavaCallInfo(method, args);
            MessageContext mc = db.serializeRequest(call);
            SOAPMessage msg = mc.getSOAPMessage();
            // System.out.println("------------------ eclipselink");
            // msg.writeTo(System.out);
            // System.out.println();

            Node n = msg.getSOAPBody().getChildNodes().item(0);
            // System.out.println("num of attributes is: "+
            // n.getAttributes().getLength());
            assertTrue(n.getAttributes().getLength() == 1);
        }

    }

    public void testWrapperNS_JAXBRI() throws Exception {
        Class<?> sei = MyHelloRPC.class;
        DatabindingFactory fac = DatabindingFactory.newInstance();
        Databinding.Builder b = fac.createBuilder(sei, null);
        DatabindingModeFeature dbf = new DatabindingModeFeature(
                "glassfish.jaxb");
        WebServiceFeature[] f = { dbf };
        b.feature(f);
        b.serviceName(new QName("http://echo.org/", "helloService"));
        b.portName(new QName("http://echo.org/", "helloPort"));
        Databinding db = b.build();

        {

            Method method = findMethod(sei, "echoString");
            Object[] args = { "test" };
            JavaCallInfo call = db.createJavaCallInfo(method, args);
            MessageContext mc = db.serializeRequest(call);
            SOAPMessage msg = mc.getSOAPMessage();
            // System.out.println("------------------ glassfish");
            // msg.writeTo(System.out);
            // System.out.println();
            Node n = msg.getSOAPBody().getChildNodes().item(0);
            // System.out.println("num of attributes is: "+
            // n.getAttributes().getLength());
            assertTrue(n.getAttributes().getLength() == 1);
        }

    }

    static public Method findMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}
