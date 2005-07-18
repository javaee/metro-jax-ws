/*
 * $Id: GloballyKnown.java,v 1.2 2005-07-18 18:14:21 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.framework;

/**
 * An interface implemented by entities which can be defined in a target namespace.
 *
 * @author WS Development Team
 */
public interface GloballyKnown extends Elemental {
    public String getName();
    public Kind getKind();
    public Defining getDefining();
}
