/*
 * $Id: EntityAction.java,v 1.2 2005-07-18 18:14:19 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An action operating on an entity.
 *
 * @author WS Development Team
 */
public interface EntityAction {
    public void perform(Entity entity);
}
