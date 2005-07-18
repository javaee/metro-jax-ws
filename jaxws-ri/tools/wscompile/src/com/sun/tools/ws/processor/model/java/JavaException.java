/*
 * $Id: JavaException.java,v 1.2 2005-07-18 18:14:01 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.model.java;

/**
 *
 * @author WS Development Team
 */
public class JavaException extends JavaStructureType {

    public JavaException() {}

    public JavaException(String name, boolean present, Object owner) {
        super(name, present, owner);
    }
}
