/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.model;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.model.wsdl.WSDLBoundOperationImpl;
import com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl;
import com.sun.xml.ws.model.wsdl.WSDLPartImpl;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.resources.ModelerMessages;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.spi.db.BindingInfo;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.util.Pool;
import com.sun.xml.ws.developer.UsesJAXBContextFeature;
import com.sun.xml.ws.developer.JAXBContextFactory;
import com.sun.xml.ws.binding.WebServiceFeatureList;

import javax.jws.WebParam.Mode;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * model of the web service.  Used by the runtime marshall/unmarshall
 * web service invocations
 *
 * @author JAXWS Development Team
 */
public abstract class AbstractSEIModelImpl implements SEIModel {

    protected AbstractSEIModelImpl(WebServiceFeature[] features) {
        this.features = features;
        databindingInfo = new BindingInfo();
        databindingInfo.setSEIModel(this);
    }

    void postProcess() {
        // should be called only once.
        if (jaxbContext != null)
            return;
        populateMaps();
        createJAXBContext();
    }

    /**
     * Link {@link SEIModel} to {@link WSDLModel}.
     * Merge it with {@link #postProcess()}.
     */
    public void freeze(WSDLPortImpl port) {
        this.port = port;
        for (JavaMethodImpl m : javaMethods) {
            m.freeze(port);
            putOp(m.getOperationQName(),m);

        }
        if (databinding != null) ((com.sun.xml.ws.db.DatabindingImpl)databinding).freeze(port);
    }

    /**
     * Populate methodToJM and nameToJM maps.
     */
    abstract protected void populateMaps();

    public Pool.Marshaller getMarshallerPool() {
        return marshallers;
    }

    /**
     * @return the <code>JAXBRIContext</code>
     * @deprecated
     */
    public JAXBRIContext getJAXBContext() {
    	JAXBContext jc = bindingContext.getJAXBContext();
    	if (jaxbContext == null && jc instanceof JAXBRIContext) jaxbContext = (JAXBRIContext) bindingContext.getJAXBContext();
        return jaxbContext;
    }

    public BindingContext getBindingContext() {
        return bindingContext;
    }

    /**
     * @return the known namespaces from JAXBRIContext
     */
    public List<String> getKnownNamespaceURIs() {
        return knownNamespaceURIs;
    }

    /**
     * @return the <code>Bridge</code> for the <code>type</code>
     * @deprecated use getBond
     */
    public final Bridge getBridge(TypeReference type) {
        Bridge b = bridgeMap.get(type);
        assert b!=null; // we should have created Bridge for all TypeReferences known to this model
        return b;
    }
    
    public final XMLBridge getXMLBridge(TypeInfo type) {
        XMLBridge b = xmlBridgeMap.get(type);
        assert b!=null; // we should have created Bridge for all TypeReferences known to this model
        return b;
    }

    private JAXBRIContext createJAXBContext() {
        final List<TypeInfo> types = getAllTypeInfos();
        final List<Class> cls = new ArrayList<Class>(types.size() + additionalClasses.size());

        cls.addAll(additionalClasses);
        for (TypeInfo type : types)
            cls.add((Class) type.type);

        try {
            //jaxbContext = JAXBRIContext.newInstance(cls, types, targetNamespace, false);
            // Need to avoid doPriv block once JAXB is fixed. Afterwards, use the above
            bindingContext = AccessController.doPrivileged(new PrivilegedExceptionAction<BindingContext>() {
                public BindingContext run() throws Exception {
                    if(LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE,"Creating JAXBContext with classes="+cls+" and types="+types);
                    }
                    UsesJAXBContextFeature f = WebServiceFeatureList.getFeature(features, UsesJAXBContextFeature.class);
                    DatabindingModeFeature dbf = WebServiceFeatureList.getFeature(features, DatabindingModeFeature.class);
                    JAXBContextFactory factory = f!=null ? f.getFactory() : null;
                    if(factory==null)   factory=JAXBContextFactory.DEFAULT;
//                    return factory.createJAXBContext(AbstractSEIModelImpl.this,cls,types);

                    databindingInfo.properties().put(JAXBContextFactory.class.getName(), factory);
                	if (dbf != null) databindingInfo.setDatabindingMode(dbf.getMode());
//                	else 
//                		bi.setDatabindingMode(BindingContextFactory.EclipseLinkJAXB);
//                		bi.setDatabindingMode(BindingContextFactory.GlassfishJAXB);   
                	if (f!=null) databindingInfo.setDatabindingMode(BindingContextFactory.DefaultDatabindingMode);
                	databindingInfo.setClassLoader(classLoader);
                	databindingInfo.contentClasses().addAll(cls);
                	databindingInfo.typeInfos().addAll(types);
                	databindingInfo.properties().put("c14nSupport", Boolean.FALSE);
                	databindingInfo.setDefaultNamespace(AbstractSEIModelImpl.this.getTargetNamespace());
                	BindingContext bc =  BindingContextFactory.create(databindingInfo);
//                	System.out.println("---------------------- databinding " + bc);
                	return bc;
                }
            });
//          createBridgeMap(types);
            createBondMap(types);
        } catch (PrivilegedActionException e) {
            throw new WebServiceException(ModelerMessages.UNABLE_TO_CREATE_JAXB_CONTEXT(), e);
        }
        knownNamespaceURIs = new ArrayList<String>();
        for (String namespace : bindingContext.getKnownNamespaceURIs()) {
            if (namespace.length() > 0) {
                if (!namespace.equals(SOAPNamespaceConstants.XSD) && !namespace.equals(SOAPNamespaceConstants.XMLNS))
                    knownNamespaceURIs.add(namespace);
            }
        }

        marshallers = new Pool.Marshaller(jaxbContext);

        return getJAXBContext();
    }

    /**
     * @return returns non-null list of TypeReference
     */
    private List<TypeInfo> getAllTypeInfos() {
        List<TypeInfo> types = new ArrayList<TypeInfo>();
        Collection<JavaMethodImpl> methods = methodToJM.values();
        for (JavaMethodImpl m : methods) {
            m.fillTypes(types);
        }
        return types;
    }

    private void createBridgeMap(List<TypeReference> types) {
        for (TypeReference type : types) {
            Bridge bridge = jaxbContext.createBridge(type);
            bridgeMap.put(type, bridge);
        }
    }
    private void createBondMap(List<TypeInfo> types) {
        for (TypeInfo type : types) {
            XMLBridge binding = bindingContext.createBridge(type);
            xmlBridgeMap.put(type, binding);
        }
    }


    /**
     * @return true if <code>name</code> is the name
     * of a known fault name for the <code>Method method</code>
     */
    public boolean isKnownFault(QName name, Method method) {
        JavaMethodImpl m = getJavaMethod(method);
        for (CheckedExceptionImpl ce : m.getCheckedExceptions()) {
            if (ce.getDetailType().tagName.equals(name))
                return true;
        }
        return false;
    }

    /**
     * @return true if <code>ex</code> is a Checked Exception
     * for <code>Method m</code>
     */
    public boolean isCheckedException(Method m, Class ex) {
        JavaMethodImpl jm = getJavaMethod(m);
        for (CheckedExceptionImpl ce : jm.getCheckedExceptions()) {
            if (ce.getExceptionClass().equals(ex))
                return true;
        }
        return false;
    }

    /**
     * @return the <code>JavaMethod</code> representing the <code>method</code>
     */
    public JavaMethodImpl getJavaMethod(Method method) {
        return methodToJM.get(method);
    }

    /**
     * @return the <code>JavaMethod</code> associated with the
     * operation named name
     */
    public JavaMethodImpl getJavaMethod(QName name) {
        return nameToJM.get(name);
    }

    public JavaMethod getJavaMethodForWsdlOperation(QName operationName) {
        return wsdlOpToJM.get(operationName);
    }


    /**
     * @return the <code>QName</code> associated with the
     * JavaMethod jm.
     *
     * @deprecated
     *      Use {@link JavaMethod#getOperationName()}.
     */
    public QName getQNameForJM(JavaMethodImpl jm) {
        for (QName key : nameToJM.keySet()) {
            JavaMethodImpl jmethod = nameToJM.get(key);
            if (jmethod.getOperationName().equals(jm.getOperationName())){
               return key;
            }
        }
        return null;
    }

    /**
     * @return a <code>Collection</code> of <code>JavaMethods</code>
     * associated with this <code>RuntimeModel</code>
     */
    public final Collection<JavaMethodImpl> getJavaMethods() {
        return Collections.unmodifiableList(javaMethods);
    }

    void addJavaMethod(JavaMethodImpl jm) {
        if (jm != null)
            javaMethods.add(jm);
    }

    /**
     * Applies binding related information to the RpcLitPayload. The payload map is populated correctl
     * @return
     * Returns attachment parameters if/any.
     */
    private List<ParameterImpl> applyRpcLitParamBinding(JavaMethodImpl method, WrapperParameter wrapperParameter, WSDLBoundPortTypeImpl boundPortType, Mode mode) {
        QName opName = new QName(boundPortType.getPortTypeName().getNamespaceURI(), method.getOperationName());
        WSDLBoundOperationImpl bo = boundPortType.get(opName);
        Map<Integer, ParameterImpl> bodyParams = new HashMap<Integer, ParameterImpl>();
        List<ParameterImpl> unboundParams = new ArrayList<ParameterImpl>();
        List<ParameterImpl> attachParams = new ArrayList<ParameterImpl>();
        for(ParameterImpl param : wrapperParameter.wrapperChildren){
            String partName = param.getPartName();
            if(partName == null)
                continue;

            ParameterBinding paramBinding = boundPortType.getBinding(opName,
                    partName, mode);
            if(paramBinding != null){
                if(mode == Mode.IN)
                    param.setInBinding(paramBinding);
                else if(mode == Mode.OUT || mode == Mode.INOUT)
                    param.setOutBinding(paramBinding);

                if(paramBinding.isUnbound()){
                        unboundParams.add(param);
                } else if(paramBinding.isAttachment()){
                    attachParams.add(param);
                }else if(paramBinding.isBody()){
                    if(bo != null){
                        WSDLPartImpl p = bo.getPart(param.getPartName(), mode);
                        if(p != null)
                            bodyParams.put(p.getIndex(), param);
                        else
                            bodyParams.put(bodyParams.size(), param);
                    }else{
                        bodyParams.put(bodyParams.size(), param);
                    }
                }
            }

        }
        wrapperParameter.clear();
        for(int i = 0; i <  bodyParams.size();i++){
            ParameterImpl p = bodyParams.get(i);
            wrapperParameter.addWrapperChild(p);
        }

        //add unbounded parts
        for(ParameterImpl p:unboundParams){
            wrapperParameter.addWrapperChild(p);
        }
        return attachParams;
    }


    void put(QName name, JavaMethodImpl jm) {
        nameToJM.put(name, jm);
    }

    void put(Method method, JavaMethodImpl jm) {
        methodToJM.put(method, jm);
    }

    void putOp(QName opName, JavaMethodImpl jm) {
        wsdlOpToJM.put(opName, jm);
    }
    public String getWSDLLocation() {
        return wsdlLocation;
    }

    void setWSDLLocation(String location) {
        wsdlLocation = location;
    }

    public QName getServiceQName() {
        return serviceName;
    }

    public WSDLPort getPort() {
        return port;
    }

    public QName getPortName() {
        return portName;
    }

    public QName getPortTypeName() {
        return portTypeName;
    }

    void setServiceQName(QName name) {
        serviceName = name;
    }

    void setPortName(QName name) {
        portName = name;
    }

    void setPortTypeName(QName name) {
        portTypeName = name;
    }

    /**
     * This is the targetNamespace for the WSDL containing the PortType
     * definition
     */
    void setTargetNamespace(String namespace) {
        targetNamespace = namespace;
    }

    /**
     * This is the targetNamespace for the WSDL containing the PortType
     * definition
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    @NotNull
    public QName getBoundPortTypeName() {
        assert portName != null;
        return new QName(portName.getNamespaceURI(), portName.getLocalPart()+"Binding");
    }

    /**
     * Adds additional classes obtained from {@link XmlSeeAlso} annotation. In starting
     * from wsdl case these classes would most likely be JAXB ObjectFactory that references other classes.
     */
    public void addAdditionalClasses(Class... additionalClasses) {
        for(Class cls : additionalClasses)
            this.additionalClasses.add(cls);
    }
    
    public Databinding getDatabinding() {
		return databinding;
	}

	public void setDatabinding(Databinding wsRuntime) {
		this.databinding = wsRuntime;
	}
	
	public WSBinding getWSBinding() {
		return wsBinding;
	}
	
    public Class getContractClass() {
		return contractClass;
	}

	public Class getEndpointClass() {
		return endpointClass;
	}

	private List<Class> additionalClasses = new ArrayList<Class>();

    private Pool.Marshaller marshallers;
    /**
     * @deprecated
     */
    protected JAXBRIContext jaxbContext;
    protected BindingContext bindingContext;
    private String wsdlLocation;
    private QName serviceName;
    private QName portName;
    private QName portTypeName;
    private Map<Method,JavaMethodImpl> methodToJM = new HashMap<Method, JavaMethodImpl>();
    /**
     * Payload QName to the method that handles it.
     */
    private Map<QName,JavaMethodImpl> nameToJM = new HashMap<QName, JavaMethodImpl>();
    /**
     * Wsdl Operation QName to the method that handles it.
     */
    private Map<QName, JavaMethodImpl> wsdlOpToJM = new HashMap<QName, JavaMethodImpl>();

    private List<JavaMethodImpl> javaMethods = new ArrayList<JavaMethodImpl>();
    private final Map<TypeReference, Bridge> bridgeMap = new HashMap<TypeReference, Bridge>();
    private final Map<TypeInfo, XMLBridge> xmlBridgeMap = new HashMap<TypeInfo, XMLBridge>();
    protected final QName emptyBodyName = new QName("");
    private String targetNamespace = "";
    private List<String> knownNamespaceURIs = null;
    private WSDLPortImpl port;
    private final WebServiceFeature[] features;
    private Databinding databinding;
    BindingID bindingId;
    protected Class contractClass;
	protected Class endpointClass;
	protected ClassLoader classLoader = null;
	protected WSBinding wsBinding;
	protected BindingInfo databindingInfo;
	private static final Logger LOGGER = Logger.getLogger(AbstractSEIModelImpl.class.getName());
}
