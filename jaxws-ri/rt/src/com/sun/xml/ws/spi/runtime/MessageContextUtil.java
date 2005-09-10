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
