/**
 * $Id: Style.java,v 1.1 2005-05-23 22:42:09 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

/**
 * @author Vivek Pandey
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public enum Style {
    DOCUMENT(0), RPC(1);

    private Style(int style){
        this.style = style;
    }

    public int value() {
        return style;
    }
    private final int style;
}
