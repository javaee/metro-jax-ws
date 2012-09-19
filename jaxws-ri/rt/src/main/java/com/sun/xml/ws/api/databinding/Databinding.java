/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

import com.sun.xml.ws.api.message.MessageContextFactory;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.wsdl.DispatchException;

/**
 * {@code Databinding} is the entry point for all the WebService databinding
 * runtime functionality. Primarily, a Databinding is to serialize/deserialize an
 * XML(SOAP) message to/from a JAVA method invocation and return value which 
 * are represented as <code>JavaCallInfo</code> instances.
 * <p>
 * </p>
 * Each Databinding is associated with a <code>MessageFactory</code> instance
 * which can be used to create <code>Message</code> instances that can be
 * deserialized by the Databinding. The <code>MessageFactory</code> also supports
 * the conversion of Oracle Fabric Normalized messages.
 * <p>
 * </p>
 * <blockquote> Following is an example that creates a {@code Databinding} which
 * provides the operations to serialize/deserialize a JavaCallInfo to/from a
 * SOAP message:<br />
 * 
 * <pre>
 * DatabindingFactory wsfac = DatabindingFactory();
 * Databinding rt = wsfac.createDatabinding(DatabindingConfig);
 * </pre>
 * 
 * </blockquote>
 * 
 * @author shih-chang.chen@oracle.com
 */
public interface Databinding extends org.jvnet.ws.databinding.Databinding {

	/**
	 * Gets the MessageFactory instance associated with this WsRuntime
	 * 
	 * @return the MessageFactory instance associated with this WsRuntime
	 */
//	MessageFactory getMessageFactory();

	/**
	 * Deserializes a request XML(SOAP) message to a JavaCallInfo instance
	 * representing a JAVA method call.
	 * 
	 * @param soap
	 *            the request message
	 * 
	 * @return the JavaCallInfo representing a method call
	 */
//	JavaCallInfo deserializeRequest(Packet req);

	EndpointCallBridge getEndpointBridge(Packet soap) throws DispatchException;
	
	ClientCallBridge getClientBridge(Method method);

	/**
	 * Serializes a JavaCallInfo instance representing a JAVA method call to a
	 * request XML(SOAP) message.
	 * 
	 * @param call
	 *            the JavaCallInfo representing a method call
	 * 
	 * @return the request XML(SOAP) message
	 */
//	Packet serializeRequest(JavaCallInfo call);

	/**
	 * Serializes a JavaCallInfo instance representing the return value or
	 * exception of a JAVA method call to a response XML(SOAP) message.
	 * 
	 * @param call
	 *            the JavaCallInfo representing the return value or exception of
	 *            a JAVA method call
	 * 
	 * @return the response XML(SOAP) message
	 */
//	Packet serializeResponse(JavaCallInfo call);

	/**
	 * Deserializes a response XML(SOAP) message to a JavaCallInfo instance
	 * representing the return value or exception of a JAVA method call.
	 * 
	 * @param soap
	 *            the response message
	 * 
	 * @param call
	 *            the JavaCallInfo instance to be updated
	 * 
	 * @return the JavaCallInfo updated with the return value or exception of a
	 *         JAVA method call
	 */
//	JavaCallInfo deserializeResponse(Packet res, JavaCallInfo call);

	/**
	 * Gets the WSDL operation metadata of the specified JAVA method.
	 * 
	 * @param method
	 *            the JAVA method
	 * @return the operationMetadata
	 */
//	OperationMetadata getOperationMetadata(java.lang.reflect.Method method);

	/**
	 * Gets the WebServiceFeatures of this webservice endpoint.
	 * 
	 * @return the features
	 */
//	WebServiceFeature[] getFeatures();

	void generateWSDL(WSDLGenInfo info);

	/**
	 * @deprecated use MessageContextFactory
	 */
	public ContentType encode( Packet packet, OutputStream out ) throws IOException ;

    /**
     * @deprecated use MessageContextFactory
     */
	public void decode( InputStream in, String ct, Packet packet ) throws IOException;
	
	public MessageContextFactory getMessageContextFactory();
}
