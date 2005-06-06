/*
 * $Id: RuntimeEndpointInfo.java,v 1.17 2005-06-06 22:01:12 vivekp Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.HandlerAnnotationProcessor;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;
import com.sun.xml.ws.client.BindingImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.handler.Handler;
import javax.xml.transform.Source;


/**
 * @author JAX-RPC Development Team
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
    private HandlerChainCaller handlerChainCaller;
    private List<Source> metadata;
    private Binding binding;
    private RuntimeModel runtimeModel;
    private Object implementor;
    private Map<String, DocInfo> docs;      // /WEB-INF/wsdl/xxx.wsdl -> DocInfo
    private Map<String, DocInfo> query2Doc;     // (wsdl=a) --> DocInfo
    private boolean enableMtom;
    private String bindingId = SOAPBinding.SOAP11HTTP_BINDING;

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
     * @param enable
     */
    public void setMtomEnabled(boolean enable){
        this.enableMtom = enable;
    }

    public boolean isDeployed() {
        return deployed;
    }
    
    public void createModel() {
        //how will j2ee pass the binding id?
        if(binding != null && binding instanceof BindingImpl) {
            bindingId = ((BindingImpl)binding).getBindingId();
        }

        // Create runtime model for non Provider endpoints            
        RuntimeModeler rap = new RuntimeModeler(getImplementor().getClass(), bindingId);
        runtimeModel = rap.buildRuntimeModel();     
    }
    
    public void deploy() {
        if (implementor == null) {
            // TODO throw exception
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
                WSDLGenerator wsdlGen = new WSDLGenerator(runtimeModel, wsdlResolver, bindingId);
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
            
            if (getHandlerChain() == null) {
                HandlerAnnotationInfo chainInfo =
                    HandlerAnnotationProcessor.buildHandlerInfo(
                        getImplementor().getClass());
                if (chainInfo != null) {
                    setHandlerChainCaller(new HandlerChainCaller(chainInfo));
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

    public List<Handler> getHandlerChain() {
        return (handlerChainCaller == null ? null :
            handlerChainCaller.getHandlerChain());
    }

    public void setHandlerChain(List<Handler> chain) {
        if (chain != null) {
            handlerChainCaller = new HandlerChainCaller(chain);
        } else {
            handlerChainCaller = null;
        }
    }
    
    // used internally in deploy() method. may be set to null
    private void setHandlerChainCaller(HandlerChainCaller caller) {
        handlerChainCaller = caller;
    }
    
    public HandlerChainCaller getHandlerChainCaller() {
        return handlerChainCaller;
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

}
