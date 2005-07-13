/**
 * $Id: JaxRpcRtObjectFactory.java,v 1.4 2005-07-13 21:21:15 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.sun.xml.ws.util.JaxRpcRtObjectFactoryImpl;


/**
 * Singleton abstract factory used to produce jaxrpc related objects.
 */
public abstract class JaxRpcRtObjectFactory {

    private static JaxRpcRtObjectFactory factory;

    public JaxRpcRtObjectFactory() {
    }

    /**
     * Obtain an instance of a factory.
     *
     * <p> The implementation class to be used can be overridden by setting a
     * system property (name TBD). </p>
     *
     */
    public static JaxRpcRtObjectFactory newInstance() {
        if (factory == null) {
            //XXX FIXME Make it configurable by property
            try {
                factory = new JaxRpcRtObjectFactoryImpl();
            } catch (Exception e) {
                //XXX FIXME  i18n.  Better Handling of the Error
                e.printStackTrace();
            }
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
     * @see com.sun.xml.rpc.spi2.runtime.ClientTransportFactoryTypes
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

    public abstract ServletDelegate createServletDelegate();

    /**
     * Names provides utility methods used by other wscompile classes
     * for dealing with identifiers.  This is not the most obvious/intuitive
     * method name.  Any suggestion is welcome.
     *
    public abstract Names createNames();
     */
    
    public abstract Tie createTie();

}
