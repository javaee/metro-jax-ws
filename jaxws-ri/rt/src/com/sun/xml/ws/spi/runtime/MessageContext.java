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

/**
 * Enhanced API' MessageContext with some extra properties
 */
public interface MessageContext extends javax.xml.ws.handler.MessageContext {
    /**
     * Returns binding id defined in API
     * @return bindingId is one of these values:
     * javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
     * javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING,
     * javax.xml.ws.http.HTTPBinding.HTTP_BINDING
     */
    public String getBindingId();
    
    /**
     * Returns the invocation method.
     *
     * @return invocation method, null if the model doesn't know
     */
    public Method getMethod();
    
    /**
     * Sets cannonicalization algorithm that is used while writing JAXB objects
     *
     */
    public void setCanonicalization(String algorithm);
    
    /**
     * Returns the Invoker
     *
     * @return Invoker
     */
    public Invoker getInvoker();

    /**
     * Returns if MTOM is anbled
     * @return true if MTOM is enabled otherwise returns false;
     */
    public boolean isMtomEnabled();
}
