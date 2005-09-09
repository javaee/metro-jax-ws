/**
 * $Id: MessageContextUtil.java,v 1.1 2005-09-09 02:41:31 jitu Exp $
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
}
