/*
 * $Id: Kind.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * A kind of entity.
 *
 * @author WS Development Team
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
