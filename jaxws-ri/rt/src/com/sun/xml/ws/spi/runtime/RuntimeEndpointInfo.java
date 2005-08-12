/**
 * $Id: RuntimeEndpointInfo.java,v 1.7 2005-08-12 02:55:14 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

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
     * This object is used for method invocations. It could be actual
     * implementor or a proxy object. This must be set before calling deploy().
     */
    public void setImplementor(Object implementor);
    
    /**
     * implementorClass should have <code>@WebService</code> or
     * <code>@WebServiceProvider</code> annotation.
     * Dynamic model is created using this object. If this is not set, implementor's
     * class is used to create the model.
     */
    public void setImplementorClass(Class implementorClass);
    
    /**
     * @return Object Gets the endpoint implementation object or a proxy
     */
    public Object getImplementor();
    
    /**
     * @return implementor's class that has the annotations
     */
    public Class getImplementorClass();

    /**
     * @return Binding Returns the binding for this endpoint.
     */
    public Binding getBinding();
    
    /**
     * sets the binding for this endpoint. If there are handlers, set them on
     * the binding object.
     */
    public void setBinding(Binding binding);
    
    /**
     * @return WebServiceContext Returns the WebServiceContext of this endpoint.
     */
    public WebServiceContext getWebServiceContext();
    
    /**
     * sets the WebServiceContext for this endpoint.
     */
    public void setWebServiceContext(WebServiceContext wsContext);

}
