/*
 * $Id: Identifiable.java,v 1.1 2005-05-24 14:04:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities which have an ID.
 *
 * @author JAX-RPC Development Team
 */
public interface Identifiable extends Elemental {
    public String getID();
}
