/**
 * $Id: WSToolsObjectFactory.java,v 1.1 2005-07-14 01:43:39 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.spi;

import com.sun.tools.ws.util.WSToolsObjectFactoryImpl;
import java.io.OutputStream;


/**
 * Singleton abstract factory used to produce JAX-WS tools related objects.
 *
 * @author JAX-WS Development Team
 */
public abstract class WSToolsObjectFactory {

    private static WSToolsObjectFactory factory;

    /**
     * Obtain an instance of a factory. Don't worry about synchronization(at the
     * most, one more factory is created).
     */
    public static WSToolsObjectFactory newInstance() {
        if (factory == null) {
            factory = new WSToolsObjectFactoryImpl();
        }
        return factory;
    }

    /**
     * Invokes wsimport on the wsdl URL argument, and generates the necessary
     * portable artifacts like SEI, Service, Bean classes etc.
     *
     * return true if there is no error, otherwise false
     */
    public abstract boolean wsimport(OutputStream logStream, String[] args);
    
    /**
     * Invokes wsgen on the endpoint implementation, and generates the necessary
     * artifacts like wrapper, exception bean classes etc.
     *
     * return true if there is no error, otherwise false
     */
    public abstract boolean wsgen(OutputStream logStream, String[] args);

}
