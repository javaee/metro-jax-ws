/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.eclipselink;

import static javax.jws.soap.SOAPBinding.Style.RPC;
import static javax.jws.soap.SOAPBinding.Use.LITERAL;

import java.lang.reflect.Method;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebServiceFeature;

import junit.framework.TestCase;

import com.oracle.webservices.api.databinding.Databinding;
import com.oracle.webservices.api.databinding.DatabindingFactory;
import com.oracle.webservices.api.databinding.DatabindingMode;
import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.oracle.webservices.api.databinding.JavaCallInfo;

import org.w3c.dom.Node;

import com.oracle.webservices.api.message.MessageContext;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.MappingInfo;
import com.sun.xml.ws.model.RuntimeModeler;

public class WrapperNSTest extends TestCase {

    @WebService(targetNamespace = "http://echo.org/")
    @SOAPBinding(style = RPC, use = LITERAL)
    static public interface MyHelloRPC {
        public String echoString(String str);
    }

    @WebService(targetNamespace = "http://echo.org/")
    @DatabindingMode(value = "eclipselink.jaxb")
    static public class MyHelloClass {
        @ResponseWrapper(className = "response1", localName="gigi")
        @RequestWrapper(className = "request1")
        public String echoString(String str) {return "Hello " + str; }
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
    
    public void testGenerateWsdl() throws Exception {
        Class<?> sei = MyHelloRPC.class;
        DatabindingFactory fac = DatabindingFactory.newInstance();
        Databinding.Builder b = fac.createBuilder(sei, null);
        DatabindingModeFeature dbf = new DatabindingModeFeature(
                "eclipselink.jaxb");
        WebServiceFeature[] f = { dbf };
		DatabindingConfig config = new DatabindingConfig();
		config.setFeatures(f);
		config.setEndpointClass(MyHelloClass.class);
		MappingInfo mi = new MappingInfo();
		mi.setServiceName(new QName("http://echo.org/", "helloService"));
		mi.setPortName(new QName("http://echo.org/", "helloPort"));
		config.setMappingInfo(mi);
		config.setClassLoader(this.getClass().getClassLoader());
		
		RuntimeModeler rtModeler = new RuntimeModeler(config);
		rtModeler.buildRuntimeModel();
    	
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
