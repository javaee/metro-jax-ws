/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.util.pipe;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;

import junit.framework.TestCase;

public class AbstractSchemaValidationTubeTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testCreateSameTnsPseudoSchema() throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException, TransformerException {
		BindingID bindingId = BindingID.SOAP11_HTTP;
		WSBinding binding = bindingId.createBinding();
		
		Class<AbstractSchemaValidationTube> clazz = AbstractSchemaValidationTube.class;
		Object instance = new StractSchemaValidationTubeMock(binding, new TubeMock());
		Method method = clazz.getDeclaredMethod("createSameTnsPseudoSchema", new Class[]{String.class,Collection.class,String.class});
		method.setAccessible(true);  
		
		String tns="null";
		List<String> docs = new ArrayList<>();
		docs.add("a.xsd");
		docs.add("b.xsd");
		String pseudoSystemId = "file:x-jax-ws-include-0";
		Object result = method.invoke(instance, new Object[]{tns, docs, pseudoSystemId});
		StreamSource schemaStream = (StreamSource)result;
		
		StringWriter writer = new StringWriter();
    StreamResult StreamResult = new StreamResult(writer);
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();
    transformer.transform(schemaStream,StreamResult);
    String strResult = writer.toString();
    
    StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n");
		sb.append("<xsd:include schemaLocation=\"a.xsd\"/>\n");
		sb.append("<xsd:include schemaLocation=\"b.xsd\"/>\n");
		sb.append("</xsd:schema>");
		
		assertEquals(sb.toString(), strResult);
		assertEquals(-1, strResult.indexOf("targetNamespace"));
	}
	
	class StractSchemaValidationTubeMock extends AbstractSchemaValidationTube {
		public StractSchemaValidationTubeMock(WSBinding binding, Tube next) {
			super(binding, next);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Validator getValidator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected boolean isNoValidation() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public AbstractTubeImpl copy(TubeCloner cloner) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	class TubeMock implements Tube{

		@Override
		public NextAction processRequest(Packet request) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NextAction processResponse(Packet response) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public NextAction processException(Throwable t) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void preDestroy() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Tube copy(TubeCloner cloner) {
			// TODO Auto-generated method stub
			return null;
		}
	}

}
