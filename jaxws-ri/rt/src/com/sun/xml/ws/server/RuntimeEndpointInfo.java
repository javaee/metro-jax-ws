/*
 * $Id: RuntimeEndpointInfo.java,v 1.1 2005-05-23 22:50:24 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.modeler.RuntimeAnnotationProcessor;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.Provider;
import javax.xml.ws.handler.Handler;
import javax.xml.transform.Source;
import com.sun.xml.ws.server.*;

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

    public boolean isDeployed() {
        return deployed;
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
                    } else {
                        // WSD
                    }
                }
            }
            RuntimeAnnotationProcessor rap = new RuntimeAnnotationProcessor(null,
                getImplementor().getClass());
            runtimeModel = rap.buildRuntimeModel();
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
        }
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

}
