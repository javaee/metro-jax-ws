/*
 * $Id: GloballyKnown.java,v 1.1 2005-05-24 14:04:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities which can be defined in a target namespace.
 *
 * @author JAX-RPC Development Team
 */
public interface GloballyKnown extends Elemental {
    public String getName();
    public Kind getKind();
    public Defining getDefining();
}
