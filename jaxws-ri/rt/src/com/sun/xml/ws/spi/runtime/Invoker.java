/**
 * $Id: Invoker.java,v 1.1 2005-09-10 01:52:09 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.lang.reflect.Method;
import javax.xml.namespace.QName;

/**
 *
 * @author WS Development Team
 */
public interface Invoker {
    public void invoke() throws Exception;
    public Method getMethod(QName name);
}
