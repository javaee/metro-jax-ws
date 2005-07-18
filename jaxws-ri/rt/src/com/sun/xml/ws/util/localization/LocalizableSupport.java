/*
 * $Id: LocalizableSupport.java,v 1.2 2005-07-18 16:52:34 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * @author WS Development Team
 */
public class LocalizableSupport {
    protected String key;
    protected Object[] arguments;

    public LocalizableSupport(String key) {
        this(key, (Object[]) null);
    }

    public LocalizableSupport(String key, String argument) {
        this(key, new Object[] { argument });
    }

    public LocalizableSupport(String key, Localizable localizable) {
        this(key, new Object[] { localizable });
    }

    public LocalizableSupport(String key, Object[] arguments) {
        this.key = key;
        this.arguments = arguments;
    }

    public String getKey() {
        return key;
    }
    public Object[] getArguments() {
        return arguments;
    }

    //abstract public String getResourceBundleName();
}
