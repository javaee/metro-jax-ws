/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import org.xml.sax.EntityResolver;

import com.oracle.webservices.api.databinding.Databinding;
import com.oracle.webservices.api.databinding.Databinding.Builder;
import com.oracle.webservices.api.databinding.WSDLGenerator;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.DatabindingFactory;
import com.sun.xml.ws.api.databinding.MetadataReader;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.spi.db.DatabindingProvider;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * DatabindingFactoryImpl
 * 
 * @author shih-chang.chen@oracle.com
 */
public class DatabindingFactoryImpl extends DatabindingFactory {

//	static final String WsRuntimeFactoryProperties = DatabindingProvider.class.getName() + ".properties";
	static final String WsRuntimeFactoryDefaultImpl = "com.sun.xml.ws.db.DatabindingProviderImpl";

	protected Map<String, Object> properties = new HashMap<String, Object>();
	protected DatabindingProvider defaultRuntimeFactory;
//	protected Map<String, DatabindingProvider> runtimeFactories = new HashMap<String, DatabindingProvider>();
//	protected Properties wsRuntimeFactoryMap;
	protected List<DatabindingProvider> providers;

    static private List<DatabindingProvider> providers() {
        List<DatabindingProvider> factories = new java.util.ArrayList<DatabindingProvider>();
        for (DatabindingProvider p : ServiceFinder.find(DatabindingProvider.class)) {
            factories.add(p);
        }
        return factories;
    }

	public DatabindingFactoryImpl() {
	}

	public Map<String, Object> properties() {
		return properties;
	}

	<T> T property(Class<T> propType, String propName) {
		if (propName == null) propName = propType.getName();
		return propType.cast(properties.get(propName));
	}
    
    public DatabindingProvider provider(DatabindingConfig config) {
        String mode = databindingMode(config);
        if (providers == null)
            providers = providers();
        DatabindingProvider provider = null;
        if (providers != null) {
            for (DatabindingProvider p : providers)
                if (p.isFor(mode))
                    provider = p;
        } if (provider == null) {
            // if (defaultRuntimeFactory == null) {
            // defaultRuntimeFactory =
            // newRuntimeFactory(WsRuntimeFactoryDefaultImpl);
            // }
            // provider = defaultRuntimeFactory;
            provider = new DatabindingProviderImpl();
        }
        return provider;
    }
	
	public Databinding createRuntime(DatabindingConfig config) {
	    DatabindingProvider provider = provider(config);
		return provider.create(config);
	}
    
    public WSDLGenerator createWsdlGen(DatabindingConfig config) {
        DatabindingProvider provider = provider(config);
        return provider.wsdlGen(config);
    }
	
//	DatabindingProvider newRuntimeFactory(String name) {
//		ClassLoader classLoader = classLoader();
//		DatabindingProvider factory = null;
//		try {
//			Class cls = (classLoader != null) ? classLoader.loadClass(name) : Class.forName(name);
//			factory = DatabindingProvider.class.cast(cls.newInstance());
//		} catch (Exception e) {
//			throw new DatabindingException("Unknown DatabindingFactory: " + name, e);
//		}
//		factory.init(properties);
//		return factory;
//	}

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
        if ( config.getFeatures() != null) for (WebServiceFeature f : config.getFeatures()) {
            if (f instanceof com.oracle.webservices.api.databinding.DatabindingModeFeature) {
                com.oracle.webservices.api.databinding.DatabindingModeFeature dmf = (com.oracle.webservices.api.databinding.DatabindingModeFeature) f;
                return dmf.getMode();
            }
        }
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

    public Builder createBuilder(Class<?> contractClass, Class<?> endpointClass) {
        return new ConfigBuilder(this, contractClass, endpointClass);
    }
    
    static class ConfigBuilder implements Builder {
        DatabindingConfig config;
        DatabindingFactoryImpl factory;
        
        ConfigBuilder(DatabindingFactoryImpl f, Class<?> contractClass, Class<?> implBeanClass) {
            factory = f;
            config = new DatabindingConfig();
            config.setContractClass(contractClass);
            config.setEndpointClass(implBeanClass);
        }
        public Builder targetNamespace(String targetNamespace) {
            config.getMappingInfo().setTargetNamespace(targetNamespace);
            return this;
        }
        public Builder serviceName(QName serviceName) {
            config.getMappingInfo().setServiceName(serviceName);
            return this;
        }
        public Builder portName(QName portName) {
            config.getMappingInfo().setPortName(portName);
            return this;
        }
        public Builder wsdlURL(URL wsdlURL) {
            config.setWsdlURL(wsdlURL);
            return this;
        }
        public Builder wsdlSource(Source wsdlSource) {
            config.setWsdlSource(wsdlSource);
            return this;
        }
        public Builder entityResolver(EntityResolver entityResolver) {
            config.setEntityResolver(entityResolver);
            return this;
        }
        public Builder classLoader(ClassLoader classLoader) {
            config.setClassLoader(classLoader);
            return this;
        }
        public Builder feature(WebServiceFeature... f) {
            config.setFeatures(f);
            return this;
        }
        public Builder property(String name, Object value) {
            config.properties().put(name, value);
            if (isfor(BindingID.class, name, value)) {
                config.getMappingInfo().setBindingID((BindingID)value);
            }
            if (isfor(WSBinding.class, name, value)) {
                config.setWSBinding((WSBinding)value);
            }
            if (isfor(WSDLPort.class, name, value)) {
                config.setWsdlPort((WSDLPort)value);
            }
            if (isfor(MetadataReader.class, name, value)) {
                config.setMetadataReader((MetadataReader)value);
            }
            return this;
        }
        boolean isfor(Class<?> type, String name, Object value) {
            return type.getName().equals(name) && type.isInstance(value);
        }

        public com.oracle.webservices.api.databinding.Databinding build() {
            return factory.createRuntime(config);
        }
        public com.oracle.webservices.api.databinding.WSDLGenerator createWSDLGenerator() {
            return factory.createWsdlGen(config);
        }       
    }
}
