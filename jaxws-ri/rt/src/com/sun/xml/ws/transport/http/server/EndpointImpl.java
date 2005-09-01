
/**
 * $Id: EndpointImpl.java,v 1.9 2005-09-01 02:46:05 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.RuntimeEndpointInfo;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Binding;
import javax.xml.transform.Source;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServicePermission;
import javax.xml.ws.WebServiceProvider;

/**
 *
 * @author WS Development Team
 */
public class EndpointImpl extends Endpoint {
    
    private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION =
        new WebServicePermission("publishEndpoint");
    private Object actualEndpoint;        // Don't declare as HttpEndpoint type
    private RuntimeEndpointInfo rtEndpointInfo;
   
    public EndpointImpl(String bindingId, Object impl) {
        this(impl);
        // TODO set binding
    }
    
    public EndpointImpl(Object impl) {
        rtEndpointInfo = new RuntimeEndpointInfo();
        rtEndpointInfo.setImplementor(impl);
        rtEndpointInfo.setImplementorClass(impl.getClass());
    }
    
    public Binding getBinding() {
        return rtEndpointInfo.getBinding();
    }

    public Object getImplementor() {
        return rtEndpointInfo.getImplementor();
    }

    public void publish(String address) {
        checkPlatform();
        ((HttpEndpoint)actualEndpoint).publish(address);
    }

    public void publish(Object serverContext) {
        checkPlatform();
        ((HttpEndpoint)actualEndpoint).publish(serverContext);
    }

    public void stop() {
        ((HttpEndpoint)actualEndpoint).stop();
    }

    public boolean isPublished() {
        return rtEndpointInfo.isDeployed();
    }

    public java.util.List<Source> getMetadata() {
        return rtEndpointInfo.getMetadata();
    }

    public void setMetadata(java.util.List<Source> metadata) {
        if (isPublished()) {
            throw new IllegalStateException("Cannot set Metadata. Already published");
        }
        rtEndpointInfo.setMetadata(metadata);
        
    }

    public Executor getExecutor() {
        return rtEndpointInfo.getExecutor();
    }

    public void setExecutor(Executor executor) {
        rtEndpointInfo.setExecutor(executor);
    }

    public Map<String, Object> getProperties() {
        return rtEndpointInfo.getProperties();
    }

    public void setProperties(Map<String, Object> map) {
        rtEndpointInfo.setProperties(map);
    }
    
    /*
     * Checks the permission of "publishEndpoint" before accessing HTTP classes.
     * Also it checks if there is an available HTTP server implementation.
     */
    private void checkPlatform() {
        
        // Checks permission for "publishEndpoint"
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(ENDPOINT_PUBLISH_PERMISSION);
        }
        
        // See if HttpServer implementation is available
        try {
            Class clazz = Class.forName("com.sun.net.httpserver.HttpServer");
        } catch(Exception e) {
            throw new UnsupportedOperationException("NOT SUPPORTED");
        }
        
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(rtEndpointInfo);
    }
    
}
