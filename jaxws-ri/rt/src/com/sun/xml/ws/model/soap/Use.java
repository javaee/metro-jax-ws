/**
 * $Id: Use.java,v 1.1 2005-05-23 22:42:09 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model.soap;

/**
 * @author Vivek Pandey
 *
 */
public enum Use {
    LITERAL(0), ENCODED(1);

    private Use(int use){
        this.use = use;
    }

    public int value() {
        return use;
    }
    private final int use;
}
