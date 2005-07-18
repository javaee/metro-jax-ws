/*
 * $Id: CDATA.java,v 1.2 2005-07-18 16:52:35 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util.xml;

/**
 * @author WS Development Team
 */
public final class CDATA {

    public CDATA(String text) {
        _text = text;
    }

    public String getText() {
        return _text;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (!(obj instanceof CDATA))
            return false;

        CDATA cdata = (CDATA) obj;

        return this._text.equals(cdata._text);
    }

    public int hashCode() {
        return _text.hashCode();
    }

    private String _text;
}
