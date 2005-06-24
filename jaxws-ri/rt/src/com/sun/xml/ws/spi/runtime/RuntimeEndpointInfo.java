/**
 * $Id: RuntimeEndpointInfo.java,v 1.2 2005-06-24 18:04:33 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.transform.Source;
import com.sun.xml.ws.spi.runtime.*;

/**
 * This class is implemented by
 * com.sun.xml.rpc.server.http.RuntimeEndpointInfo
 */
public interface RuntimeEndpointInfo {

    public void setName(String s);
    
    /*
     * Builds runtime model from implementor object. It also generates required
     * WSDL, schema documents if there is no corresponding metadata. 
     */
    public void deploy();
    public void setPortName(QName n);
    public void setServiceName(QName n);
    public void setUrlPattern(String s);
    
    /**
     * Sets the endpoint implementation object
     */
    public void setImplementor(Object servant);
    
    /**
     * Gets the endpoint implementation object
     */
    public Object getImplementor();

    
    /**
     * Returns the binding for this endpoint.
     *
     */
    public Binding getBinding();
    
    /**
     * Returns a list of metadata documents for the service.
     */
    public List<javax.xml.transform.Source> getMetadata();

    /**
     * Sets the metadata for this endpoint.
     *
     * @param metadata A list of XML document sources containing
     *           metadata information for the endpoint (e.g.
     *           WSDL or XML Schema documents)
     *
     * @throws java.lang.IllegalStateException If the endpoint
     *         has already been published.
     */
    public void setMetadata(List<Source> metadata);

}
