/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.databinding;

import javax.jws.WebService;
import javax.xml.namespace.QName;

import com.oracle.webservices.api.databinding.DatabindingFactory;
import com.oracle.webservices.api.databinding.WSDLGenerator;

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
