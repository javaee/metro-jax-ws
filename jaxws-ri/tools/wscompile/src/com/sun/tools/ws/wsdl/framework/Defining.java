/*
 * $Id: Defining.java,v 1.1 2005-05-24 14:04:10 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities that define target namespaces.
 *
 * @author JAX-RPC Development Team
 */
public interface Defining extends Elemental {
    public String getTargetNamespaceURI();
}
