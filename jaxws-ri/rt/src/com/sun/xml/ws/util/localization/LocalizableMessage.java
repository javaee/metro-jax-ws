/*
 * $Id: LocalizableMessage.java,v 1.2 2005-07-18 16:52:34 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * @author WS Development Team
 */
public class LocalizableMessage implements Localizable {

    protected String _bundlename;
    protected String _key;
    protected Object[] _args;

    public LocalizableMessage(String bundlename, String key) {
        this(bundlename, key, (Object[]) null);
    }

    public LocalizableMessage(String bundlename, String key, String arg) {
        this(bundlename, key, new Object[] { arg });
    }

    public LocalizableMessage(
        String bundlename,
        String key,
        Object[] args) {
        _bundlename = bundlename;
        _key = key;
        _args = args;
    }

    public String getKey() {
        return _key;
    }

    public Object[] getArguments() {
        return _args;
    }

    public String getResourceBundleName() {
        return _bundlename;
    }
}
