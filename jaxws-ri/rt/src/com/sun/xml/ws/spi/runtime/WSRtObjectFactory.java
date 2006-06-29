/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.spi.runtime;

import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.xml.ws.util.WSRtObjectFactoryImpl;
import java.net.URL;
import java.util.List;
import javax.servlet.ServletContext;
import org.xml.sax.EntityResolver;
import javax.xml.ws.handler.MessageContext;

/**
 * Singleton abstract factory used to produce JAX-WS runtime related objects.
 */
public abstract class WSRtObjectFactory {

    private static final WSRtObjectFactory factory = new WSRtObjectFactoryImpl();

    /**
     * Obtain an instance of a factory. Don't worry about synchronization(at
     * the most, one more factory object is created).
     *
     */
    public static WSRtObjectFactory newInstance() {
        return factory;
    }

    /**
     * Creates SOAPMessageContext
     */
    public abstract SOAPMessageContext createSOAPMessageContext();


    /**
     * Creates an object with all endpoint info
     */
    public abstract RuntimeEndpointInfo createRuntimeEndpointInfo();
    
    /**
     * Creates a connection for servlet transport
     */
    public abstract WSConnection createWSConnection(
            HttpServletRequest req, HttpServletResponse res);
    
    /**
     * @return List of endpoints
     */
    public abstract List<RuntimeEndpointInfo> getRuntimeEndpointInfos(
            ServletContext ctxt);
    
    /**
     * creates a Tie object, entry point to JAXWS runtime.
     */
    public abstract Tie createTie();
    
    /**
     * creates a MesageContext object. Create it for each MEP.
     */
    public abstract MessageContext createMessageContext();
    
    /**
     * creates the Binding object implementation. Set the object on
     * RuntimeEndpointInfo.
     * bindingId should be one of these values:
     * javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
     * javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING,
     * javax.xml.ws.http.HTTPBinding.HTTP_BINDING
     */
    public abstract Binding createBinding(String bindingId);
    
    /**
     * creates an EntityResolver for the XML Catalog URL
     */
    public abstract EntityResolver createEntityResolver(URL catalogUrl);

}
