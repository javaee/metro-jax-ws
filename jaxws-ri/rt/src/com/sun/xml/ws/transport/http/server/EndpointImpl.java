
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
    private RuntimeEndpointInfo rtEndpointInfo;
   
    public EndpointImpl(String bindingId, Object impl) {
        rtEndpointInfo = new RuntimeEndpointInfo();
        rtEndpointInfo.setImplementor(impl);
        rtEndpointInfo.setImplementorClass(impl.getClass());
        com.sun.xml.ws.spi.runtime.Binding binding =
            BindingImpl.getBinding(bindingId, impl.getClass(), false);
        rtEndpointInfo.setBinding(binding);
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
            Class.forName("com.sun.net.httpserver.HttpServer");
        } catch(Exception e) {
            throw new UnsupportedOperationException("NOT SUPPORTED");
        }
        
        // Don't load HttpEndpoint class before as it may load HttpServer classes
        actualEndpoint = new HttpEndpoint(rtEndpointInfo);
    }
    
}
