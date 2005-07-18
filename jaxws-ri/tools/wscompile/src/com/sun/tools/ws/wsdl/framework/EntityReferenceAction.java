/*
 * $Id: EntityReferenceAction.java,v 1.2 2005-07-18 18:14:19 kohlert Exp $
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
 * @author WS Development Team
 */
public interface EntityReferenceAction {
    public void perform(Kind kind, QName name);
}
