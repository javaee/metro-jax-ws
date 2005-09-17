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
package com.sun.xml.ws.handler;

import com.sun.xml.ws.spi.runtime.Invoker;
import com.sun.xml.ws.spi.runtime.MessageContext;
import java.lang.reflect.Method;


/**
 * Utility to manipulate MessageContext properties
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    private static final String JAVA_METHOD = "com.sun.xml.ws.java.method";
    private static final String BINDING_ID = "com.sun.xml.ws.binding.id";
    private static final String INVOKER = "com.sun.xml.ws.invoker";
    
    public static String getBindingId(MessageContext ctxt) {
        return (String)((MessageContextImpl)ctxt).getInternalMap().get(BINDING_ID);
    }

     public static void setBindingId(MessageContext ctxt, String bindingId) {
        ((MessageContextImpl)ctxt).getInternalMap().put(BINDING_ID, bindingId);
    }
    
    /**
     * Sets the invocation method.
     *
     * @return invocation method, null if the model doesn't know
     */
    public static void setMethod(MessageContext ctxt, Method method) {
        ((MessageContextImpl)ctxt).getInternalMap().put(JAVA_METHOD, method);
    }
    
    /**
     * Returns the invocation method.
     *
     * @return invocation method, null if the model doesn't know
     */
    public static Method getMethod(MessageContext ctxt) {
        return (Method)((MessageContextImpl)ctxt).getInternalMap().get(JAVA_METHOD);
    }
    
    public static void setCanonicalization(MessageContext ctxt, String algorithm) {
        // TODO
    }
    
    public static Invoker getInvoker(MessageContext ctxt) {
        return (Invoker)((MessageContextImpl)ctxt).getInternalMap().get(INVOKER);
    }
    
    public static void setInvoker(MessageContext ctxt, Invoker invoker) {
        ((MessageContextImpl)ctxt).getInternalMap().put(INVOKER, invoker);
    }
    
    public static Integer getHttpStatusCode(javax.xml.ws.handler.MessageContext ctxt) {
        return (Integer)ctxt.get(MessageContext.HTTP_RESPONSE_CODE);
    }
    
    public static void setHttpStatusCode(javax.xml.ws.handler.MessageContext ctxt, Integer code) {
        ctxt.put(MessageContext.HTTP_RESPONSE_CODE, code);
    }
}
