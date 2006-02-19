
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Binding;
import javax.xml.transform.Source;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.xml.ws.WebServicePermission;


/**
 *
 * @author WS Development Team
 */
public class EndpointImpl extends Endpoint {
    
    private static final WebServicePermission ENDPOINT_PUBLISH_PERMISSION =
        new WebServicePermission("publishEndpoint");
    private Object actualEndpoint;        // Don't declare as HttpEndpoint type
    private Executor executor;
    private boolean published;
    private boolean stopped;
    private final com.sun.xml.ws.spi.runtime.Binding binding;
    private final Object implementor;
    private Map<String, Object> properties;
    private java.util.List<Source> metadata;
   
    public EndpointImpl(String bindingId, Object impl) {
        this.implementor = impl;
        this.binding = BindingImpl.getBinding(bindingId, impl.getClass(), null, false);
    }
    
    public Binding getBinding() {
        return binding;
    }

    public Object getImplementor() {
        return implementor;
    }

    public void publish(String address) {
        canPublish();
        URL url;
        try {
            url = new URL(address);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Cannot create URL for this address "+address);
        }
        if (!url.getProtocol().equals("http")) {
            throw new IllegalArgumentException(url.getProtocol()+" protocol based address is not supported");
        }
        checkPlatform();
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(implementor, binding, metadata, properties);
        ((HttpEndpoint)actualEndpoint).publish(address);
        published = true;
    }

    public void publish(Object serverContext) {
        canPublish();
        checkPlatform();
        if (!com.sun.net.httpserver.HttpContext.class.isAssignableFrom(serverContext.getClass())) {
            throw new IllegalArgumentException(serverContext.getClass()+" is not a supported context.");
        }
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(implementor, binding, metadata, properties);
        ((HttpEndpoint)actualEndpoint).publish(serverContext);
        published = true;
    }

    public void stop() {
        if (published) {
            ((HttpEndpoint)actualEndpoint).stop();
            published = false;
            stopped = true;
        }
    }

    public boolean isPublished() {
        return published;
    }
    
    private void canPublish() {
        if (published) {
            throw new IllegalStateException(
                "Cannot publish this endpoint. Endpoint has been already published.");
        }
        if (stopped) {
            throw new IllegalStateException(
                "Cannot publish this endpoint. Endpoint has been already stopped.");
        }
    }

    public java.util.List<Source> getMetadata() {
        return metadata;
    }

    public void setMetadata(java.util.List<Source> metadata) {
        if (published) {
            throw new IllegalStateException("Cannot set Metadata. Endpoint is already published");
        }
        this.metadata = metadata;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        // Not used in our implementation
        this.executor = executor;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> map) {
        this.properties = map;
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
            Class.forName("com.sun.net.httpserver.HttpServer");
        } catch(Exception e) {
            throw new UnsupportedOperationException("NOT SUPPORTED");
        }
        
    }
    
}
