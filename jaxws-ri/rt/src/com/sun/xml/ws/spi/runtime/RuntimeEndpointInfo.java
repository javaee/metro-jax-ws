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
package com.sun.xml.ws.spi.runtime;

import java.net.URL;
import javax.xml.namespace.QName;
import org.xml.sax.EntityResolver;

/**
 * This captures all the required information (e.g: handlers, binding, endpoint
 * object, proxy for endpoint object etc.) about the endpoint.
 */
public interface RuntimeEndpointInfo {
    
    /**
     * Returns the name of the endpoint
     * @return name of the endpoint
     */
    public String getName();
    
    /**
     * sets the name of the endpoint
     */
    public void setName(String name);
        
    /**
     * Builds runtime model from implementor object.
     */
    public void init();
    
    /**
     * Destroys any state in this object
     */
    public void destroy();
    
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
     * Returns actual Endpoint Object where method invocation is done
     *
     * @return Object Gets the endpoint implementation object or a proxy
     */
    public Object getImplementor();
    
    /**
     * Returns the set implementorClass
     *
     * @return implementor's class that has the annotations
     */
    public Class getImplementorClass();

    /**
     * Returns the binding for this endpoint
     *
     * @return Binding Returns the binding for this endpoint.
     */
    public Binding getBinding();
    
    /**
     * sets the binding for this endpoint. If there are handlers, set them on
     * the binding object.
     */
    public void setBinding(Binding binding);
    
    /**
     * Returns the WebServiceContext of this endpoint
     *
     * @return WebServiceContext Returns the WebServiceContext of this endpoint.
     */
    public WebServiceContext getWebServiceContext();
    
    /**
     * sets the WebServiceContext for this endpoint.
     */
    public void setWebServiceContext(WebServiceContext wsContext);
    
    /**
     * set the URL for primary WSDL, and a resolver to resolve entities like
     * WSDL, imports/references. A resolver for XML catalog can be created using
     * WSRtObjectFactory.createResolver(URL catalogURL).
     */
    public void setWsdlInfo(URL wsdlUrl, EntityResolver resolver);
    
    /**
     * Set service name from DD. If it is null, @WebService, @WebServiceProvider
     * annotations are used to get service name
     */
    public void setServiceName(QName name);
    
    /**
     * Set port name from DD. If it is null, @WebService, @WebServiceProvider
     * annotations are used to get port name
     */
    public void setPortName(QName name);

}