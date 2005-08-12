/*
 * $Id: RuntimeEndpointInfo.java,v 1.29 2005-08-12 02:55:13 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import javax.annotation.Resource;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import com.sun.xml.ws.spi.runtime.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.Source;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.xml.ws.BeginService;
import javax.xml.ws.EndService;



/**
 * modeled after the javax.xml.ws.Endpoint class in API. 
 * Contains all the information about Binding, handler chain, Implementor object, 
 * WSDL & Schema Metadata
 * @author WS Development Team
 */
public class RuntimeEndpointInfo
    implements com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo {
    
    private String name;
    private Exception exception;
    private QName portName;
    private QName serviceName;
    private String wsdlFileName;
    private boolean deployed;
    private String urlPattern;
    private List<Source> metadata;
    private Binding binding;
    private RuntimeModel runtimeModel;
    private Object implementor;
    private Class implementorClass;
    private Map<String, DocInfo> docs;      // /WEB-INF/wsdl/xxx.wsdl -> DocInfo
    private Map<String, DocInfo> query2Doc;     // (wsdl=a) --> DocInfo
    private boolean enableMtom;
    private WebServiceContext wsContext;
    private boolean beginServiceDone;
    private boolean endServiceDone;
    private boolean injectedContext;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception e) {
        exception = e;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getWSDLFileName() {
        return wsdlFileName;
    }

    public void setWSDLFileName(String s) {
        wsdlFileName = s;
    }

    /**
     * Enable Mtom processing
     * @param enable enables the use of MTOM
     */
    public void setMtomEnabled(boolean enable){
        this.enableMtom = enable;
    }

    public boolean isDeployed() {
        return deployed;
    }
    
    public void createModel() {
        // Create runtime model for non Provider endpoints            
        RuntimeModeler rap = new RuntimeModeler(implementorClass,
            getImplementor(), ((BindingImpl)binding).getBindingId());
        runtimeModel = rap.buildRuntimeModel();
    }
    
    /**
     * creates a RuntimeModel using @link com.sun.xml.ws.modeler.RuntimeModeler. 
     * The modeler creates the model by reading annotations on Implementor object. 
     * RuntimeModel is read only and is accessed from multiple threads afterwards.
     * Generates the WSDL and XML Schemam for the endpoint if necessary
     */
    public void deploy() {
        if (implementor == null) {
            // TODO throw exception
        }
        
        if (implementorClass == null) {
            setImplementorClass(getImplementor().getClass());
        }
        
        // setting a default binding
        if (binding == null) {
            setBinding(new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING));
        }
        
        if (implementor instanceof Provider) {
            // No runtime model is required
            if (getWSDLFileName() == null) {
                // TODO throw exception
            } else {
                if (serviceName == null) {
                    // If WSDL has only one service, then it is okay
                    // else error
                } else {
                    if (portName == null) {
                        // If service has only one port, then it is okay
                        // else error
                    }
                }
            }
        } else {
            // Create runtime model for non Provider endpoints    
            createModel();
            if (getWSDLFileName() == null) {
                // Generate WSDL and schema documents using runtime model
                WSDLGenResolver wsdlResolver = new WSDLGenResolver();
                WSDLGenerator wsdlGen = new WSDLGenerator(runtimeModel, wsdlResolver,
                        ((BindingImpl)binding).getBindingId());
                try {
                    wsdlGen.doGeneration();
                } catch(Exception e) {
                    // TODO
                    e.printStackTrace();
                }
                setMetadata(wsdlResolver.getDocs());
                setWSDLFileName(wsdlResolver.getWSDLFile());      
                setServiceName(runtimeModel.getServiceQName());       
                setPortName(runtimeModel.getPortQName());
            } else {
                if (serviceName == null) {
                    // If WSDL has only one service, then it is okay
                    // else error
                } else {
                    if (portName == null) {
                        // If service has only one port, then it is okay
                        // else error
                    } else {
                        // WSD
                    }
                }
            }
            
            if (getBinding().getHandlerChain() == null) {
                HandlerAnnotationInfo chainInfo =
                    HandlerAnnotationProcessor.buildHandlerInfo(
                        implementorClass);
                if (chainInfo != null) {
                    getBinding().setHandlerChain(chainInfo.getHandlers());
                    if (getBinding() instanceof SOAPBinding) {
                        ((SOAPBinding) getBinding()).setRoles(
                            chainInfo.getRoles());
                    }
                }
            }
            
            //set Tmomt processing
            runtimeModel.enableMtom(enableMtom);
        }
        deployed = true;
    }

    public QName getPortName() {
        return portName;
    }

    public void setPortName(QName n) {
        portName = n;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName n) {
        serviceName = n;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String s) {
        urlPattern = s;
    }

    public void setBinding(Binding binding){
        this.binding = binding;
    }

    public Binding getBinding() {
        return binding;
    }
    
    public java.util.List<Source> getMetadata() {
        return metadata;
    }
        
    public void setMetadata(java.util.List<Source> metadata) {

        this.metadata = metadata;
    }

    public RuntimeModel getRuntimeModel() {
        return runtimeModel;
    }
    
    public Object getImplementor() {
        return implementor;
    }
    
    public void setImplementor(Object implementor) {
        this.implementor = implementor;
    }
    
    public Class getImplementorClass() {
        if (implementorClass == null) {
            implementorClass = implementor.getClass();
        }
        return implementorClass;
    }
    
    public void setImplementorClass(Class implementorClass) {
        this.implementorClass = implementorClass;
    }
    
    public void setMetadata(Map<String, DocInfo> docs) {
        this.docs = docs;
        // update uri-->DocInfo map
        if (query2Doc != null) {
            query2Doc.clear();
        } else {
            query2Doc = new HashMap<String, DocInfo>();
        }
        Set<Map.Entry<String, DocInfo>> entries = docs.entrySet();
        for(Map.Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = entry.getValue();
            query2Doc.put(docInfo.getQueryString(), docInfo);
        }
    }
    
    public WebServiceContext getWebServiceContext() {
        return wsContext;
    }
    
    public void setWebServiceContext(WebServiceContext wsContext) {
        this.wsContext = wsContext;
    }
     
    
    /*
     * key - /WEB-INF/wsdl/xxx.wsdl
     */
    public Map<String, DocInfo> getDocMetadata() {
        return docs;
    }
    
    /*
     * path - /WEB-INF/wsdl/xxx.wsdl
     * return - xsd=a | wsdl | wsdl=b etc
     */
    public String getQueryString(String path) {
        DocInfo docInfo = docs.get(path);
        return (docInfo == null) ? null : docInfo.getQueryString();
    }
    
    /*
     * queryString - xsd=a | wsdl | wsdl=b etc
     * return - /WEB-INF/wsdl/xxx.wsdl
     */
    public String getPath(String queryString) {
        DocInfo docInfo = query2Doc.get(queryString);
        return (docInfo == null) ? null : docInfo.getPath();
    }
    
    /*
     * Injects the WebServiceContext. Called from Servlet.init(), or
     * Endpoint.publish(). Used synchronized because multiple servlet
     * instances may call this in their init()
     */
    public synchronized void injectContext()
    throws IllegalAccessException, InvocationTargetException {
        if (injectedContext) {
            return;
        }
        try {
            doFieldsInjection();
            doMethodsInjection();
        } finally {
            injectedContext = true;
        }
    }
    
    private void doFieldsInjection() {
        Class c = getImplementorClass();
        Field[] fields = c.getDeclaredFields();
        for(final Field field: fields) {
            Resource resource = field.getAnnotation(Resource.class);
            if (resource != null) {
                Class resourceType = resource.type();
                Class fieldType = field.getType();                  
                if (resourceType.equals(Object.class)) {                    
                    if (fieldType.equals(javax.xml.ws.WebServiceContext.class)) {      
                        injectField(field);
                    }
                } else if (resourceType.equals(javax.xml.ws.WebServiceContext.class)) {
                    if (fieldType.isAssignableFrom(javax.xml.ws.WebServiceContext.class)) {
                        injectField(field);
                    } else {
                        throw new ServerRtException("wrong.field.type",
                            field.getName());
                    }
                }
            }
        }
    }
    
    private void doMethodsInjection() {
        Class c = getImplementorClass();
        Method[] methods = c.getDeclaredMethods();
        for(final Method method : methods) {
            Resource resource = method.getAnnotation(Resource.class);
            if (resource != null) {
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    throw new ServerRtException("wrong.no.parameters",
                        method.getName());
                }
                Class resourceType = resource.type();
                Class argType = paramTypes[0];
                if (resourceType.equals(Object.class)
                    && argType.equals(javax.xml.ws.WebServiceContext.class)) {
                    invokeMethod(method, new Object[] { wsContext });
                } else if (resourceType.equals(javax.xml.ws.WebServiceContext.class)) {
                    if (argType.isAssignableFrom(javax.xml.ws.WebServiceContext.class)) {
                        invokeMethod(method, new Object[] { wsContext });
                    } else {
                        throw new ServerRtException("wrong.parameter.type",
                            method.getName());
                    }
                }
            }
        }
    }
    
    /*
     * injects a resource into a Field
     */
    private void injectField(final Field field) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException,
                    InvocationTargetException {
                    if (!field.isAccessible()) {                        
                        field.setAccessible(true);
                    }
                    field.set(implementor, wsContext);
                    return null;
                }
            });
        } catch(PrivilegedActionException e) {
            throw new ServerRtException("server.rt.err",
                new LocalizableExceptionAdapter(e.getException()));
        }
    }
    
    /*
     * Helper method to invoke a Method
     */
    private void invokeMethod(final Method method, final Object[] args) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException,
                InvocationTargetException {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    method.invoke(implementor, args);
                    return null;
                }
            });
        } catch(PrivilegedActionException e) {
            throw new ServerRtException("server.rt.err",
                new LocalizableExceptionAdapter(e.getException()));
        }
    }
    
    /*
     * Calls the first method in the implementor object that has @BeginService
     * annotation. Servlet.init(), or Endpoint.publish() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * init()
     */
    public synchronized void beginService() {
        if (beginServiceDone) {
            return;                 // Already called for this endpoint object
        }
        try {
            invokeLifeCycleMethod(BeginService.class);
        } finally {
            beginServiceDone = true;
        }
    }
    
    /*
     * Calls the first method in the implementor object that has @EndService
     * annotation. Servlet.destory(), or Endpoint.stop() may call this. Used
     * synchronized because multiple servlet instances may call this in their
     * destroy()
     */
    public synchronized void endService() {
        if (endServiceDone) {
            return;                 // Already called for this endpoint object
        }
        try {
            invokeLifeCycleMethod(EndService.class);
        } finally {
            endServiceDone = true;
        }
    }
    
    /*
     * Helper method to invoke any lifecycle method
     */
    private void invokeLifeCycleMethod(Class annType) {
        Class c = getImplementorClass();
        Method[] methods = c.getDeclaredMethods();
        boolean once = false;
        for(final Method method : methods) {
            if (method.getAnnotation(annType) != null) {
                if (once) {
                    // Err: Multiple methods have @xxxService annotation
                    throw new ServerRtException("more.begin.or.end.service");
                }
                if (method.getParameterTypes().length != 0) {              
                    throw new ServerRtException("not.zero.parameters",
                        method.getName());
                }
                invokeMethod(method, new Object[]{ });
                once = true;
            } 
        }
    }

}
