/*
 * $Id: NullLocalizable.java,v 1.1 2005-05-23 23:05:07 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.localization;

/**
 * NullLocalizable
 *
 * @author JAX-RPC Development Team
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