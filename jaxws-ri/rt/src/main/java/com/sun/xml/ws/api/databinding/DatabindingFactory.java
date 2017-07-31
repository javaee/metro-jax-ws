/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.databinding;

import java.util.Map;

/**
 * WsFactory is the entry point of all the ws-databinding APIs. A WsFactory
 * instance can be used to create <code>WsTool</code>, <code>WsRuntime</code>,
 * <code>XsTool</code>, and <code>XsRuntime</code> instances.
 * <p>
 * </P>
 * <blockquote>
 * Following is an example that creates a {@code WsTool} which provides the
 * operations for "WSDL to JAVA" and "JAVA to WSDL":<br />
 * <pre>
 *       WsFactory wsfac = WsFactory.newInstance();
 *       WsTool tool = wsfac.createTool();
 *       GenerationStatus status = tool.generateWsdl(javaToWsdkInfo);
 * </pre>
 * </blockquote>
 * 
 * <blockquote>
 * Following is an example that creates a {@code WsRuntime} which provides the
 * operations to serialize/deserialize a JavaCallInfo to/from a SOAP message:<br />
 * <pre>
 *       WsFactory wsfac = WsFactory.newInstance();
 *       WsRuntime rt = wsfac.createRuntime(wsRuntimeConfig);
 * </pre>
 * </blockquote>
 * 
 * @see com.sun.xml.ws.api.databinding.Databinding
 * 
 * @author shih-chang.chen@oracle.com
 */
public abstract class DatabindingFactory extends com.oracle.webservices.api.databinding.DatabindingFactory {

  /**
   * Creates a new instance of a <code>WsTool</code>. 
   * 
   * @return New instance of a <code>WsTool</code>
   */
//	abstract public WsTool createTool();

  /**
   * Creates a new instance of a <code>WsRuntime</code> which is initialized 
   * with the specified configuration object. 
   * 
   * @param config
   *          the EndpointRuntimeConfig to init this WsRuntime
   * @return New instance of a <code>WsRuntime</code>
   */
  abstract public com.oracle.webservices.api.databinding.Databinding createRuntime(DatabindingConfig config);
	
  /**
   * Creates a new instance of a <code>XsTool</code>. 
   * 
   * @return New instance of a <code>XsTool</code>
   */
//	abstract public XsTool createXsTool(String mode);

  /**
   * Creates a new instance of a <code>XsRuntime</code>. 
   * 
   * @return New instance of a <code>XsRuntime</code>
   */
//	abstract public XsRuntime createXsRuntime(String mode);

  /**
   * Access properties on the <code>WsFactory</code> instance.
   *
   * @return properties of this WsFactory
   */
	abstract public Map<String, Object> properties();

	/**
	 * The default implementation class name.
	 */
	static final String ImplClass = com.sun.xml.ws.db.DatabindingFactoryImpl.class.getName();

  /**
   * Create a new instance of a <code>WsFactory</code>. This static method 
   * creates a new factory instance.
   * 
   * Once an application has obtained a reference to a <code>WsFactory</code> 
   * it can use the factory to configure and obtain <code>WsTool</code> and
   * <code>WsRuntime</code> instances.
   * 
   * @return New instance of a <code>WsFactory</code>
   */
	static public DatabindingFactory newInstance() {
		try {
			Class<?> cls = Class.forName(ImplClass);
			return (DatabindingFactory) cls.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
