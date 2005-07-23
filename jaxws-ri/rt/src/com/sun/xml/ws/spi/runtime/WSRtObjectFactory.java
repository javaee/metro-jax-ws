/**
 * $Id: WSRtObjectFactory.java,v 1.3 2005-07-23 04:10:13 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.xml.ws.util.WSRtObjectFactoryImpl;

/**
 * Singleton abstract factory used to produce JAX-WS runtime related objects.
 */
public abstract class WSRtObjectFactory {

    private static WSRtObjectFactory factory;

    /**
     * Obtain an instance of a factory. Don't worry about synchronization(at
     * the most, one more factory object is created).
     *
     */
    public static WSRtObjectFactory newInstance() {
        if (factory == null) {
            factory = new WSRtObjectFactoryImpl();
        }
        return factory;
    }

    /**
     * Delete it ??
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
     * @param type The type of ClientTransportFactory
     * @see com.sun.xml.ws.spi.runtime.ClientTransportFactoryTypes
     */
    public abstract ClientTransportFactory createClientTransportFactory(
        int type,
        OutputStream logStream);

    /**
     * Delete it ?? 
     *
    public abstract CompileTool createCompileTool(
        OutputStream out,
        String program);
     */

    /**
     * Delete it? not used
     */
    public abstract ServletDelegate createServletDelegate();
    
    /**
     * creates the Tie object, entry point to JAXWS runtime.
     */
    public abstract Tie createTie();
    
    /**
     * creates the Binding object implementation. Set the object on
     * RuntimeEndpointInfo.
     * bindingId should be one of these values:
     * javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
     * javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING,
     * javax.xml.ws.http.HTTPBinding.HTTP_BINDING
     */
    public abstract Binding createBinding(String bindingId);

}
