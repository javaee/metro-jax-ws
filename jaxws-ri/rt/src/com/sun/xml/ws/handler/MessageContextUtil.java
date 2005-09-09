/*
 * $Id: MessageContextUtil.java,v 1.1 2005-09-09 02:41:30 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import com.sun.xml.ws.spi.runtime.MessageContext;
import java.lang.reflect.Method;


/**
 * Utility to manipulate MessageContext internal properties
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    private static final String JAVA_METHOD = "com.sun.xml.ws.java.method";
    private static final String BINDING_ID = "com.sun.xml.ws.binding.id";
    
    public static String getBindingId(MessageContext ctxt) {
        return (String)((MessageContextImpl)ctxt).getInternalMap().get(BINDING_ID);
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
}
