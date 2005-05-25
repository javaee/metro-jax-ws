/**
 * $Id: RuntimeModel.java,v 1.3 2005-05-25 19:57:20 vivekp Exp $
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
import com.sun.xml.ws.encoding.JAXRPCAttachmentMarshaller;
import com.sun.xml.ws.encoding.JAXRPCAttachmentUnmarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * $author: JAXRPC Development Team
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
     * @return
     */
    public BridgeContext getBridgeContext() {
        if (jaxbContext == null)
            return null;
        BridgeContext bc = bridgeContext.get();
        if (bc == null) {
            bc = jaxbContext.createBridgeContext();
            //for now, always create XOP package for binary data
            bc.setAttachmentMarshaller(new JAXRPCAttachmentMarshaller(true));
            bc.setAttachmentUnmarshaller(new JAXRPCAttachmentUnmarshaller());
            bridgeContext.set(bc);
        }
        return bc;
    }

    /**
     * @return
     */
    public JAXBRIContext getJAXBContext() {
        return jaxbContext;
    }

    /**
     * @param type
     * @return
     */
    public Bridge getBridge(TypeReference type) {
        return bridgeMap.get(type);
    }

    /**
     * @param name
     * @return
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
            jaxbContext = JAXBRIContext.newInstance(cls, types, "");
            createBridgeMap(types);
        } catch (JAXBException e) {
            throw new WebServiceException(e.getMessage(), e);
        }
        return jaxbContext;
    }

    /**
     * @return returns non-null list of TypeReference
     */
    private List<TypeReference> getAllTypeReferences() {
        List<TypeReference> types = new ArrayList<TypeReference>();
        Collection<JavaMethod> methods = methodToJM.values();
        for (JavaMethod m : methods) {
            filleTypes(m, types);
            fillFaultDetailTypes(m, types);
        }
        return types;
    }

    private void fillFaultDetailTypes(JavaMethod m, List<TypeReference> types) {
        for (CheckedException ce : m.getCheckedExceptions()) {
            types.add(ce.getDetailType());
        }
    }

    protected void filleTypes(JavaMethod m, List<TypeReference> types) {
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
     * @return
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
     * @return
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
     * @return
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
     * @return
     */
    public JavaMethod getJavaMethod(Method method) {
        return methodToJM.get(method);
    }

    /**
     * @param name
     * @return
     */
    public JavaMethod getJavaMethod(QName name) {
        return nameToJM.get(name);
    }

    /**
     * @return
     */
    public Collection<JavaMethod> getJavaMethods() {
        return Collections.unmodifiableList(javaMethods);
    }

    public void addJavaMethod(JavaMethod jm) {
        if (jm != null)
            javaMethods.add(jm);
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

    /**
     * Add a Handler. It should be called from RuntimeAnnotationProcessor while parsing @HandlerChain annotation.
     * @param handler
     */
    public void addHandler(Handler handler) {
        if (handlers == null)
            handlers = new ArrayList<Handler>();
        handlers.add(handler);
    }

    /**
     * @return null if there are no handlerChain defined thru @HandlerChain annotation. Otherwise returns List of Handlers.
     */
    public List<Handler> getHandlers() {
        if (handlers == null)
            return null;
        return Collections.unmodifiableList(handlers);
    }

    protected abstract void createDecoderInfo();

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
    private List<Handler> handlers;
    private final QName emptyBodyName = new QName("");
}
