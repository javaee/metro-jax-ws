/**
 * $Id: RuntimeModel.java,v 1.16 2005-08-21 23:32:36 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.JAXWSAttachmentUnmarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.wsdl.parser.Binding;
import com.sun.xml.ws.model.soap.SOAPBinding;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * model of the web service.  Used by the runtime marshall/unmarshall 
 * web service invocations
 *
 * $author: JAXWS Development Team
 */
public abstract class RuntimeModel {

    /**
     *
     */
    public RuntimeModel() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void postProcess() {
        // should be called only once.
        if (jaxbContext != null)
            return;
        populateMaps();
        createJAXBContext();
        createDecoderInfo();
    }

    /**
     * Populate methodToJM and nameToJM maps.
     */
    protected void populateMaps() {
        for (JavaMethod jm : getJavaMethods()) {
            put(jm.getMethod(), jm);
            for (Parameter p : jm.getRequestParameters()) {
                put(p.getName(), jm);
            }
        }
    }

    /**
     * @return the <code>BridgeContext</code> for this <code>RuntimeModel</code>
     */
    public BridgeContext getBridgeContext() {
        if (jaxbContext == null)
            return null;
        BridgeContext bc = bridgeContext.get();
        if (bc == null) {
            bc = jaxbContext.createBridgeContext();
            bc.setAttachmentMarshaller(new JAXWSAttachmentMarshaller(enableMtom));
            bc.setAttachmentUnmarshaller(new JAXWSAttachmentUnmarshaller());
            bridgeContext.set(bc);
        }
        return bc;
    }

    /**
     * @return the <code>JAXBRIContext</code>
     */
    public JAXBRIContext getJAXBContext() {
        return jaxbContext;
    }

    /**
     * @param type
     * @return the <code>Bridge</code> for the <code>type</code>
     */
    public Bridge getBridge(TypeReference type) {
        return bridgeMap.get(type);
    }

    /**
     * @param name
     * @return either a <code>RpcLitpayload</code> or a <code>JAXBBridgeInfo</code> for 
     * an operation named <code>name</code>
     */
    public Object getDecoderInfo(QName name) {
        Object obj = payloadMap.get(name);
        if (obj instanceof RpcLitPayload) {
            return RpcLitPayload.copy((RpcLitPayload) obj);
        } else if (obj instanceof JAXBBridgeInfo) {
            return JAXBBridgeInfo.copy((JAXBBridgeInfo) obj);
        }
        return null;
    }

    /**
     * @param name
     * @param payload
     */
    public void addDecoderInfo(QName name, Object payload) {
        payloadMap.put(name, payload);
    }

    /**
     * @return
     */
    private JAXBRIContext createJAXBContext() {
        List<TypeReference> types = getAllTypeReferences();
        Class[] cls = new Class[types.size()];
        int i = 0;
        for (TypeReference type : types) {
            cls[i++] = (Class) type.type;
        }
        try {
            jaxbContext = JAXBRIContext.newInstance(cls, types, targetNamespace, false);
            createBridgeMap(types);
        } catch (JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
        return jaxbContext;
    }

    /**
     * @return returns non-null list of TypeReference
     */
    public List<TypeReference> getAllTypeReferences() {
        List<TypeReference> types = new ArrayList<TypeReference>();
        Collection<JavaMethod> methods = methodToJM.values();
        for (JavaMethod m : methods) {
            fillTypes(m, types);
            fillFaultDetailTypes(m, types);
        }
        return types;
    }

    private void fillFaultDetailTypes(JavaMethod m, List<TypeReference> types) {
        for (CheckedException ce : m.getCheckedExceptions()) {
            types.add(ce.getDetailType());
//            addGlobalType(ce.getDetailType());
        }
    }

    protected void fillTypes(JavaMethod m, List<TypeReference> types) {
        addTypes(m.getRequestParameters(), types);
        addTypes(m.getResponseParameters(), types);
    }

    private void addTypes(List<Parameter> params, List<TypeReference> types) {
        for (Parameter p : params) {
            types.add(p.getTypeReference());
        }
    }

    private void createBridgeMap(List<TypeReference> types) {
        for (TypeReference type : types) {
            Bridge bridge = jaxbContext.createBridge(type);
            bridgeMap.put(type, bridge);
        }
    }

    /**
     * @param qname
     * @return the <code>Method</code> for a given Operation <code>qname</code>
     */
    public Method getDispatchMethod(QName qname) {
        //handle the empty body
        if (qname == null)
            qname = emptyBodyName;
        JavaMethod jm = getJavaMethod(qname);
        if (jm != null) {
            return jm.getMethod();
        }
        return null;
    }

    /**
     * @param name
     * @param method
     * @return true if <code>name</code> is the name
     * of a known fault name for the <code>Method method</code>
     */
    public boolean isKnownFault(QName name, Method method) {
        JavaMethod m = getJavaMethod(method);
        for (CheckedException ce : m.getCheckedExceptions()) {
            if (ce.getDetailType().tagName.equals(name))
                return true;
        }
        return false;
    }

    /**
     * @param m
     * @param ex
     * @return true if <code>ex</code> is a Checked Exception
     * for <code>Method m</code>
     */
    public boolean isCheckedException(Method m, Class ex) {
        JavaMethod jm = getJavaMethod(m);
        for (CheckedException ce : jm.getCheckedExceptions()) {
            if (ce.getExcpetionClass().equals(ex))
                return true;
        }
        return false;
    }

    /**
     * @param method
     * @return the <code>JavaMethod</code> representing the <code>method</code>
     */
    public JavaMethod getJavaMethod(Method method) {
        return methodToJM.get(method);
    }

    /**
     * @param name
     * @return the <code>JavaMethod</code> associated with the 
     * operation named name
     */
    public JavaMethod getJavaMethod(QName name) {
        return nameToJM.get(name);
    }

    /**
     * @return a <code>Collection</code> of <code>JavaMethods</code>
     * associated with this <code>RuntimeModel</code>
     */
    public Collection<JavaMethod> getJavaMethods() {
        return Collections.unmodifiableList(javaMethods);
    }

    public void addJavaMethod(JavaMethod jm) {
        if (jm != null)
            javaMethods.add(jm);
    }

    public void applyParameterBinding(Binding wsdlBinding){
        for(JavaMethod method : javaMethods){
            if(method.isAsync())
                continue;
            boolean isRpclit = ((SOAPBinding)method.getBinding()).isRpcLit();
            List<Parameter> reqParams = method.getRequestParameters();
            for(Parameter param:reqParams){
                if(param.isWrapperStyle()){
                    if(isRpclit)
                        applyRpcLitParamBinding(method.getOperationName(), (WrapperParameter)param, wsdlBinding);
                    continue;
                }

                String partName = param.getPartName();
                if(partName == null)
                    continue;
                ParameterBinding paramBinding = wsdlBinding.getBinding(method.getOperationName(),
                        partName, param.mode);
                if(paramBinding != null)
                    param.setBinding(paramBinding);
            }

            List<Parameter> resParams = method.getResponseParameters();
            for(Parameter param:resParams){
                if(param.isWrapperStyle()){
                    if(isRpclit)
                        applyRpcLitParamBinding(method.getOperationName(), (WrapperParameter)param, wsdlBinding);
                    continue;
                }
                String partName = param.getPartName();
                if(partName == null)
                    continue;
                ParameterBinding paramBinding = wsdlBinding.getBinding(method.getOperationName(),
                        partName, param.mode);
                if(paramBinding != null)
                    param.setBinding(paramBinding);
            }

        }
    }

    private void applyRpcLitParamBinding(String opName, WrapperParameter wrapperParameter, Binding wsdlBinding) {
        RpcLitPayload payload = new RpcLitPayload(wrapperParameter.getName());
        for(Parameter param:wrapperParameter.getWrapperChildren()){
            String partName = param.getPartName();
                if(partName == null)
                    continue;
                ParameterBinding paramBinding = wsdlBinding.getBinding(opName,
                        partName, param.mode);
                if(paramBinding != null){
                    param.setBinding(paramBinding);
                    if(paramBinding.isBody()){
                        JAXBBridgeInfo bi = new JAXBBridgeInfo(getBridge(param.getTypeReference()), null);
                        payload.addParameter(bi);
                    }
                }
        }
        payloadMap.put(wrapperParameter.getName(), payload);
    }

    /**
     * @param name
     * @param jm
     */
    protected void put(QName name, JavaMethod jm) {
        nameToJM.put(name, jm);
    }

    /**
     * @param method
     * @param jm
     */
    protected void put(Method method, JavaMethod jm) {
        methodToJM.put(method, jm);
    }

    public String getWSDLLocation() {
        return wsdlLocation;
    }

    public void setWSDLLocation(String location) {
        wsdlLocation = location;
    }

    public QName getServiceQName() {
        return serviceQName;
    }

    public QName getPortQName() {
        return portQName;
    }

    public void setServiceQName(QName name) {
        serviceQName = name;
    }

    public void setPortQName(QName name) {
        portQName = name;
    }
    
    public void setTargetNamespace(String namespace) {
        targetNamespace = namespace;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    /**
     * Add a global type.  Global types will be used to generate global
     * elements in the generated schema's
     * @param typeReference
     */
/*    public void addGlobalType(TypeReference typeReference) {
        
    }*/

    /**
     * Add a global type.  Global types will be used to generate global
     * elements in the generated schema's
     * @return 
     */
/*    public Collection<TypeReference> getGlobalTypes() {
        return globalTypes;
    }*/
    
    
    /**
     * Mtom processing is disabled by default. To enable it the RuntimeModel creator must call it to enable it.
     * @param enableMtom
     */
    public void enableMtom(boolean enableMtom){
        this.enableMtom = enableMtom;
    }

    protected abstract void createDecoderInfo();

    private boolean enableMtom = false;
    private ThreadLocal<BridgeContext> bridgeContext = new ThreadLocal<BridgeContext>();
    private JAXBRIContext jaxbContext;
    private String wsdlLocation;
    private QName serviceQName;
    private QName portQName;
    private Map<Method, JavaMethod> methodToJM = new HashMap<Method, JavaMethod>();
    private Map<QName, JavaMethod> nameToJM = new HashMap<QName, JavaMethod>();
    private List<JavaMethod> javaMethods = new ArrayList<JavaMethod>();
    private final Map<TypeReference, Bridge> bridgeMap = new HashMap<TypeReference, Bridge>();
    private final Map<QName, Object> payloadMap = new HashMap<QName, Object>();
    protected final QName emptyBodyName = new QName("");
    private String targetNamespace = "";
//    protected Collection<TypeReference> globalTypes = new ArrayList<TypeReference>();
}
