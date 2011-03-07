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
package com.sun.xml.ws.api.databinding;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

/**
 * WsRuntimeConfig contains the initial states for WsRuntime. After a WsRuntime 
 * instance is created, all it's internal states should be considered 
 * 'immutable' and therefore the operations on WsRuntime can be thread-safe.
 *
 * @author shih-chang.chen@oracle.com
 */
public class DatabindingConfig {
    protected Class contractClass;
	protected Class endpointClass;
	protected Set<Class> additionalValueTypes = new HashSet<Class>();
//	protected Set<SchemaInfo> schemaInfo;
//	protected MappingInfo defaultMappingInfo = new MappingInfo();
//	protected MappingInfo overrideMappingInfo = new MappingInfo();
	protected MappingInfo mappingInfo = new MappingInfo();
//	protected Definition wsdl;
	protected URL wsdlURL;
	protected ClassLoader classLoader;
//	protected QName serviceName;
//	protected BindingID bindingId;
	protected WebServiceFeature[] features;
	//TODO WSBinding isn't it BindingID + features? 
	//On the EndpointFactory.createEndpoint path, WSBinding could be created from DeploymentDescriptorParser.createBinding
	protected WSBinding wsBinding;
	protected WSDLPort wsdlPort;
	protected MetadataReader metadataReader;
	protected Map<String, Object> properties = new HashMap<String, Object>();
	
//	public MappingInfo getDefaultMappingInfo() {
//		return defaultMappingInfo;
//	}
//	public void setDefaultMappingInfo(MappingInfo defaultMappingInfo) {
//		this.defaultMappingInfo = defaultMappingInfo;
//	}
//	public MappingInfo getOverrideMappingInfo() {
//		return overrideMappingInfo;
//	}
//	public void setOverrideMappingInfo(MappingInfo overrideMappingInfo) {
//		this.overrideMappingInfo = overrideMappingInfo;
//	}
	
	public Class getContractClass() {
		return contractClass;
	}
	public void setContractClass(Class contractClass) {
		this.contractClass = contractClass;
	}
	public Class getEndpointClass() {
		return endpointClass;
	}
	public void setEndpointClass(Class implBeanClass) {
		this.endpointClass = implBeanClass;
	}
	public MappingInfo getMappingInfo() {
		return mappingInfo;
	}
	public void setMappingInfo(MappingInfo mappingInfo) {
		this.mappingInfo = mappingInfo;
	}
	public URL getWsdlURL() {
		return wsdlURL;
	}
	public void setWsdlURL(URL wsdlURL) {
		this.wsdlURL = wsdlURL;
	}
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
//	public QName getServiceName() {
//		return serviceName;
//	}
//	public void setServiceName(QName serviceName) {
//		this.serviceName = serviceName;
//	}
//	public BindingID getBindingId() {
//		return bindingId;
//	}
//	public void setBindingId(BindingID bindingId) {
//		this.bindingId = bindingId;
//	}
	public WebServiceFeature[] getFeatures() {
		return features;
	}
	public void setFeatures(WebServiceFeature[] features) {
		this.features = features;
	}
	public WSDLPort getWsdlPort() {
		return wsdlPort;
	}
	public void setWsdlPort(WSDLPort wsdlPort) {
		this.wsdlPort = wsdlPort;
	}
	public Set<Class> additionalValueTypes() {
		return additionalValueTypes;
	}
	public Map<String, Object> properties() {
		return properties;
	}  
	public WSBinding getWSBinding() {
		return wsBinding;
	}
	public void setWSBinding(WSBinding wsBinding) {
		this.wsBinding = wsBinding;
	}
	public MetadataReader getMetadataReader() {
		return metadataReader;
	}
	public void setMetadataReader(MetadataReader  reader) {
		this.metadataReader = reader;
	}
}
