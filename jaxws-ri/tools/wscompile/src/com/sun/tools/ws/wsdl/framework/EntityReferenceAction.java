/*
 * $Id: EntityReferenceAction.java,v 1.1 2005-05-24 14:04:11 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

import javax.xml.namespace.QName;

/**
 * An action operating on an entity reference composed of a kind and a QName.
 *
 * @author JAX-RPC Development Team
 */
public interface EntityReferenceAction {
    public void perform(Kind kind, QName name);
}
