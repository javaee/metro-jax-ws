/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.xml.ws.db;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.DatabindingProvider;

/**
 * WsFactoryImpl
 * 
 * @author shih-chang.chen@oracle.com
 */
public class DatabindingFactoryImpl extends DatabindingFactory {

	static final String WsRuntimeFactoryProperties = DatabindingProvider.class.getName() + ".properties";
	static final String WsRuntimeFactoryDefaultImpl = "com.sun.xml.ws.db.DatabindingProviderImpl";

	protected Map<String, Object> properties = new HashMap<String, Object>();
	protected DatabindingProvider defaultRuntimeFactory;
	protected Map<String, DatabindingProvider> runtimeFactories = new HashMap<String, DatabindingProvider>();
	protected Properties wsRuntimeFactoryMap;

	public DatabindingFactoryImpl() {
	}

	public Map<String, Object> properties() {
		return properties;
	}

	<T> T property(Class<T> propType, String propName) {
		if (propName == null) propName = propType.getName();
		return propType.cast(properties.get(propName));
	}
//TODO use services/provider 
	public Databinding createRuntime(DatabindingConfig config) {
		String mode = databindingMode(config);
		DatabindingProvider factory = runtimeFactories.get(mode);
		if (factory == null) {
			//TODO use ServiceFinder?
			Properties map = (Properties) properties.get(WsRuntimeFactoryProperties);
			if (map == null && wsRuntimeFactoryMap == null) {
				wsRuntimeFactoryMap = loadPropertiesFile(WsRuntimeFactoryProperties);
				if (map == null) map = wsRuntimeFactoryMap;
			}
			String facName = (mode != null) ? map.getProperty(mode) : null;
			if (facName == null) {
				if (mode != null) {
					throw new DatabindingException("Unknown Databinding Mode: " + mode);
				} else {
					if (defaultRuntimeFactory == null) {
						defaultRuntimeFactory = newRuntimeFactory(WsRuntimeFactoryDefaultImpl);
					}
					factory = defaultRuntimeFactory;
				}					
			} else {
				factory = newRuntimeFactory(facName);
				runtimeFactories.put(mode, factory);
			}			
		}
		return factory.create(config);
	}
	
	DatabindingProvider newRuntimeFactory(String name) {
		ClassLoader classLoader = classLoader();
		DatabindingProvider factory = null;
		try {
			Class cls = (classLoader != null) ? classLoader.loadClass(name) : Class.forName(name);
			factory = DatabindingProvider.class.cast(cls.newInstance());
		} catch (Exception e) {
			throw new DatabindingException("Unknown Databinding WsRuntimeFactory: " + name, e);
		}
		factory.init(properties);
		return factory;
	}

	String databindingMode(DatabindingConfig config) {
//		if ( config.getOverrideMappingInfo() != null && 
//		     config.getOverrideMappingInfo().getDatabindingMode() != null)
//			return config.getOverrideMappingInfo().getDatabindingMode();
//		if ( config.getDefaultMappingInfo() != null && 
//		     config.getDefaultMappingInfo().getDatabindingMode() != null)
//			return config.getDefaultMappingInfo().getDatabindingMode();

		if ( config.getMappingInfo() != null && 
		     config.getMappingInfo().getDatabindingMode() != null)
			return config.getMappingInfo().getDatabindingMode();
		return null;
	}
	
	ClassLoader classLoader() {
		ClassLoader classLoader = property(ClassLoader.class, null);
		if (classLoader == null) classLoader = Thread.currentThread().getContextClassLoader();
		return classLoader;
	}

	Properties loadPropertiesFile(String fileName) {
		ClassLoader classLoader = classLoader();
		Properties p = new Properties();
		try {
			InputStream is = null;
			if (classLoader == null) {
				is = ClassLoader.getSystemResourceAsStream(fileName);
			} else {
				is = classLoader.getResourceAsStream(fileName);
			}
			if (is != null) {
				p.load(is);
			}
		} catch (Exception e) {
			throw new WebServiceException(e);
		}
		return p;
	}
}
