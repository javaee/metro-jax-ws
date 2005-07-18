/*
 * $Id: Defining.java,v 1.2 2005-07-18 18:14:18 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities that define target namespaces.
 *
 * @author WS Development Team
 */
public interface Defining extends Elemental {
    public String getTargetNamespaceURI();
}
