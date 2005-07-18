/*
 * $Id: Localizable.java,v 1.2 2005-07-18 16:52:33 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * @author WS Development Team
 */
public interface Localizable {

    public String getKey();
    public Object[] getArguments();
    public String getResourceBundleName();
}
