/*
 * $Id: Extensible.java,v 1.2 2005-07-18 18:14:19 kohlert Exp $
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
 * @author WS Development Team
 */
public interface Extensible extends Elemental {
    public void addExtension(Extension e);
    public Iterator extensions();
}
