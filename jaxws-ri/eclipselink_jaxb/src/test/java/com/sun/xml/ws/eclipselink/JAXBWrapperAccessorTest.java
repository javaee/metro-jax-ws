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

package com.sun.xml.ws.eclipselink;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.oracle.webservices.api.databinding.Databinding;
import com.oracle.webservices.api.databinding.DatabindingMode;
import com.oracle.webservices.api.databinding.DatabindingFactory;
import com.oracle.webservices.api.databinding.DatabindingModeFeature;

import com.sun.xml.ws.binding.WebServiceFeatureList;
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

    @DatabindingMode("eclipselink.jaxb")
    static class SEB {}       
    public void testDatabindingModeAnnotationToFeature() throws Exception {
        DatabindingMode a = SEB.class.getAnnotation(DatabindingMode.class); 
        DatabindingModeFeature f = (DatabindingModeFeature) WebServiceFeatureList.getFeature(a);
        assertEquals(f.getMode(), a.value());
    }
}
