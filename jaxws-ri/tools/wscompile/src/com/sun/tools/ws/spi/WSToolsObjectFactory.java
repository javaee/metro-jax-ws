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
     * @return true if there is no error, otherwise false
     */
    public abstract boolean wsimport(OutputStream logStream, String[] args);
    
    /**
     * Invokes wsgen on the endpoint implementation, and generates the necessary
     * artifacts like wrapper, exception bean classes etc.
     *
     * @return true if there is no error, otherwise false
     */
    public abstract boolean wsgen(OutputStream logStream, String[] args);

}
