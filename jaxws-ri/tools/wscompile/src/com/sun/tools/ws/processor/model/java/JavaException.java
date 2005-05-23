/*
 * $Id: JavaException.java,v 1.1 2005-05-23 23:15:59 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

/**
 *
 * @author JAX-RPC Development Team
 */
public class JavaException extends JavaStructureType {

    public JavaException() {}

    public JavaException(String name, boolean present, Object owner) {
        super(name, present, owner);
    }
}
