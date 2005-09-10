/**
 * $Id: MessageContextUtil.java,v 1.3 2005-09-10 01:52:10 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.spi.runtime;

import java.lang.reflect.Method;

/**
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    
    /**
     * Returns binding id defined in API
     * bindingId is one of these values:
     * javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
     * javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING,
     * javax.xml.ws.http.HTTPBinding.HTTP_BINDING
     */
    public static String getBindingId(MessageContext ctxt) {
        return com.sun.xml.ws.handler.MessageContextUtil.getBindingId(ctxt);
    }
    
    /**
     * Returns the invocation method.
     *
     * @return invocation method, null if the model doesn't know
     */
    public static Method getMethod(MessageContext ctxt) {
        return com.sun.xml.ws.handler.MessageContextUtil.getMethod(ctxt);
    }
    
    /**
     * Sets cannonicalization algorithm that is used while writing JAXB objects
     *
     */
    public static void setCanonicalization(MessageContext ctxt, String algorithm) {
        com.sun.xml.ws.handler.MessageContextUtil.setCanonicalization(ctxt,
            algorithm);
    }
    
    /**
     * Returns the Invoker
     *
     * @return Invoker
     */
    public static Invoker getInvoker(MessageContext ctxt) {
        return com.sun.xml.ws.handler.MessageContextUtil.getInvoker(ctxt);
    }
}
