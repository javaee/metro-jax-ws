/**
 * $Id: StreamingHandler.java,v 1.1 2005-05-23 22:54:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.lang.reflect.Method;

import javax.xml.soap.SOAPMessage;

public interface StreamingHandler {
    /**
     * Body's first child QName is mapped to endpoint method. If it cannot be
     * mapped it returns null
     */
    public Method getMethod(SOAPMessage request);
}
