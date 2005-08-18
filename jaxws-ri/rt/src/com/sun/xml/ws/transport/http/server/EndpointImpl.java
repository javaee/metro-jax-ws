
/**
 * $Id: EndpointImpl.java,v 1.6 2005-08-18 18:58:45 jitu Exp $
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
import javax.xml.ws.WebServicePermission;

/**
 *
 * @author WS Development Team
 */
public class EndpointImpl implements Endpoint {
    
    private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION =
        new WebServicePermission("publishEndpoint");
    private Endpoint actualEndpoint;
    private RuntimeEndpointInfo rtEndpointInfo;
   
    public EndpointImpl(URI bindingId, Object impl) {
        rtEndpointInfo = new RuntimeEndpointInfo();
        rtEndpointInfo.setImplementor(impl);
    }
    
    public Binding getBinding() {
        return rtEndpointInfo.getBinding();
    }

    public Object getImplementor() {
        return rtEndpointInfo.getImplementor();
    }

    public void publish(String address) {
        checkPermissions();
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(rtEndpointInfo);
        actualEndpoint.publish(address);
    }

    public void publish(Object serverContext) {
        checkPermissions();
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(rtEndpointInfo);
        actualEndpoint.publish(serverContext);
    }

    public void stop() {

    }

    public boolean isPublished() {
        return false;
    }

    public java.util.List<Source> getMetadata() {
        return rtEndpointInfo.getMetadata();
    }

    public void setMetadata(java.util.List<Source> metadata) {
        rtEndpointInfo.setMetadata(metadata);
    }

    public Executor getExecutor() {
        return null;
    }

    public void setExecutor(Executor executor) {

    }

    public Map<String, Object> getProperties() {
        return null;
    }

    public void setProperties(Map<String, Object> map) {
        //TODO
    }
    
    private void checkPermissions() {

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(ENDPOINT_PUBLISH_PERMISSION);
        }
        try {
            Class clazz = Class.forName("com.sun.net.httpserver.HttpServer");
        } catch(Exception e) {
            throw new UnsupportedOperationException("NOT SUPPORTED");
        }
    }
    
}
