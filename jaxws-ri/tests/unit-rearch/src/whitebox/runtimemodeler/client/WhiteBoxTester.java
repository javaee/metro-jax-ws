/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package whitebox.runtimemodeler.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.xml.namespace.QName;

import com.sun.xml.ws.model.RuntimeModeler;

/**
 * @author WS Development Team
 */
public class WhiteBoxTester extends TestCase {

    public WhiteBoxTester(String name) {
        super(name);
    }

    private static final String NS="http://client.runtimemodeler.whitebox/";

    public void testRuntimeModeler() {
        System.out.println("testing RuntimeModeler");
//        System.out.println("serviceName: " + RuntimeModeler.getServiceName(whitebox.runtimemodeler.client.RpcLitEndpoint.class));
        String packageName = "com.example.ws";
        assertTrue(RuntimeModeler.getNamespace("") == null);
        assertTrue(RuntimeModeler.getNamespace(packageName).equals("http://ws.example.com/"));
        packageName = "this.is.a.test.of.the.package.to.namespace.util";
        assertTrue(RuntimeModeler.getNamespace(packageName).equals("http://util.namespace.to.package.the.of.test.a.is.this/"));        

        packageName = RpcLitEndpoint.class.getPackage().getName();
        String ns = RuntimeModeler.getNamespace(packageName);
        System.out.println(ns);
        assertTrue(ns.equals(NS));
        assertTrue(RuntimeModeler.getServiceName(DefaultRpcLitEndpoint.class).equals(
                   new QName(NS, "DefaultRpcLitEndpointService")));
        assertTrue(RuntimeModeler.getPortName(DefaultRpcLitEndpoint.class, null).equals(
                   new QName(NS, "DefaultRpcLitEndpointPort")));
        assertTrue(RuntimeModeler.getPortTypeName(DefaultRpcLitEndpoint.class).equals(
                   new QName(NS, "DefaultRpcLitEndpoint")));


        assertTrue(RuntimeModeler.getServiceName(EndpointNoPortName.class).equals(
                   new QName(NS, "EndpointNoPortNameService")));
        assertTrue(RuntimeModeler.getPortName(EndpointNoPortName.class, null).equals(
                   new QName(NS, "RpcLitPort")));
        assertTrue(RuntimeModeler.getPortTypeName(EndpointNoPortName.class).equals(
                   new QName(NS, "RpcLit")));
        
        
        assertTrue(RuntimeModeler.getServiceName(RpcLitEndpoint.class).equals(
                   new QName("http://echo.org/", "RpcLitEndpoint")));
        assertTrue(RuntimeModeler.getPortName(RpcLitEndpoint.class, null).equals(
                   new QName("http://echo.org/", "RpcLitPort")));
        assertTrue(RuntimeModeler.getPortTypeName(RpcLitEndpoint.class).equals(
                   new QName("http://echo.org/", "RpcLit")));
        
        
        
//        RuntimeModeler modeler = new RuntimeModeler(whitebox.runtimemodeler.client.RpcLitEndpoint.class, null, null);
     
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(WhiteBoxTester.class);
        return suite;
    }
}

