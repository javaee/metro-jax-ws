/*
 * $Id: EntityAction.java,v 1.1 2005-05-24 14:04:11 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An action operating on an entity.
 *
 * @author JAX-RPC Development Team
 */
public interface EntityAction {
    public void perform(Entity entity);
}
