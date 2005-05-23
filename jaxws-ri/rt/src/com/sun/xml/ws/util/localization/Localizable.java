/*
 * $Id: Localizable.java,v 1.1 2005-05-23 23:05:06 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * @author JAX-RPC Development Team
 */
public interface Localizable {

    public String getKey();
    public Object[] getArguments();
    public String getResourceBundleName();
}
