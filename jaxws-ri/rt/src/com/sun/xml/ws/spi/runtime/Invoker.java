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

import java.lang.reflect.Method;
import javax.xml.namespace.QName;

/**
 * Complete invocation of webservice can be done using this object. So this
 * object can be wrapped in other blocks to provide certain context(for e.g.
 * can be wrapped in doAsPrivileged())
 *
 * @author WS Development Team
 */
public interface Invoker {
    /**
     * Invokes request handler chain, endpoint, response handler chain
     */
    public void invoke() throws Exception;
    
    /**
     * It gives java methods for a give operation name
     *
     * @return corresponding java method for operation name, otherwise null
     */
    public Method getMethod(QName name);
}
