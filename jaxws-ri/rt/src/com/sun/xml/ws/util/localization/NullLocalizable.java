/*
 * $Id: NullLocalizable.java,v 1.2 2005-07-18 16:52:34 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * NullLocalizable
 *
 * @author WS Development Team
 */

public class NullLocalizable implements Localizable {
    protected static NullLocalizable instance = null;

    public NullLocalizable(String key) {
        _key = key;
    }

    public String getKey() {
        return _key;
    }
    public Object[] getArguments() {
        return null;
    }
    public String getResourceBundleName() {
        return "";
    }

    private String _key;

    public static NullLocalizable instance() {
        if (instance == null) {
            instance = new NullLocalizable(null);
        }
        return instance;
    }
}