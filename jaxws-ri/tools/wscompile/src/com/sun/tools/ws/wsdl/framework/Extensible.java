/*
 * $Id: Extensible.java,v 1.1 2005-05-24 14:04:12 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import java.util.Iterator;

/**
 * An entity that can be extended.
 *
 * @author JAX-RPC Development Team
 */
public interface Extensible extends Elemental {
    public void addExtension(Extension e);
    public Iterator extensions();
}
