/**
 * $Id: RuntimeEndpointInfo.java,v 1.4 2005-07-14 23:39:49 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import com.sun.xml.ws.spi.runtime.Binding;

/**
 * This captures all the required information (e.g: handlers, binding, endpoint
 * object, proxy for endpoint object etc.) about the endpoint.
 */
public interface RuntimeEndpointInfo {
    
    /**
     * TODO: remove this method
     * Not used.
     */
    public void setUrlPattern(String s);
        
    /*
     * Builds runtime model from implementor object. It also generates required
     * WSDL, schema documents if there is no corresponding metadata. 
     */
    public void deploy();
    
    /**
     * Sets the endpoint implementation object. This servant object should have
     * @WebService annotation. Provider endpoints need not have @WebService
     * annotation. Dynamic model is created using this object.
     */
    public void setImplementor(Object servant);
    
    /**
     * Sets a proxy object for actual implementor. If the proxy object is set,
     * it is used for method invocation. Otherwise, actual implementor is used
     * for method invocation.
     */
    public void setImplementorProxy(Object servantProxy);
    
    /**
     * Gets the endpoint implementation object
     */
    public Object getImplementor();
    
    /**
     * @return proxy object for the actual implementor object
     */
    public Object getImplementorProxy();

    /**
     * Returns the binding for this endpoint.
     */
    public Binding getBinding();
    
    /**
     * sets the binding for this endpoint. If there are handlers, set them on
     * the binding object.
     */
    public void setBinding(Binding binding);

}
