/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.spi.db;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;


import junit.framework.TestCase;

public class JAXBWrapperAccessorTest  extends TestCase {
    public void testXmlElementWrapper() {
    	JAXBWrapperAccessor jaxbWrapperAccessor = new JAXBWrapperAccessor(HelloRequest.class);
    	assertNotNull(jaxbWrapperAccessor.getPropertySetter(new QName("names")));
    }
    
    public void testAccessor() throws Exception {

        try {
            JAXBWrapperAccessor jaxbWrapperAccessor = new JAXBWrapperAccessor(System.class);;
            fail();
        } catch (WebServiceException e) {
            e.printStackTrace(System.out);
        }
        
        for(Field f : System.class.getDeclaredFields()) {
//            System.out.println("try " + f);
            try {
                FieldGetter getter = new FieldGetter(f);
                fail();
            } catch (WebServiceException e) {
            }
            try {
                FieldSetter setter = new FieldSetter(f);
                fail();
            } catch (WebServiceException e) {
            }
        }
        for(Method m : System.class.getDeclaredMethods()) {
//            System.out.println("try " + m);
            try {
                MethodGetter getter = new MethodGetter(m);
                fail();
            } catch (WebServiceException e) {
            }
            try {
                MethodSetter setter = new MethodSetter(m);
                fail();
            } catch (WebServiceException e) {
            }
        }
    }
    
@XmlAccessorType(XmlAccessType.FIELD)
class HelloRequest {
	@XmlElementWrapper(name = "names")
	@XmlElement(name = "name")
	private List<String> names;

	public List<String> getNames() {
		return this.names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}
}    
}
