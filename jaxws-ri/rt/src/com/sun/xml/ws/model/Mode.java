/**
 * $Id: Mode.java,v 1.2 2005-07-12 23:32:50 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.model;

/**
 * Defines parameter mode, IN, OUT or INOUT
 *
 * @author Vivek Pandey
 */

public enum Mode {
    IN(0), OUT(1), INOUT(2);

    private Mode(int mode){
        this.mode = mode;
    }
    private final int mode;
}
