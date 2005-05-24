/*
 * $Id: Kind.java,v 1.1 2005-05-24 14:04:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * A kind of entity.
 *
 * @author JAX-RPC Development Team
 */
public final class Kind {

    public Kind(String s) {
        _name = s;
    }

    public String getName() {
        return _name;
    }

    private String _name;
}
