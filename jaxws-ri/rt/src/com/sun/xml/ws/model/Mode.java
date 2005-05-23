/**
 * $Id: Mode.java,v 1.1 2005-05-23 22:42:10 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.model;

/**
 * @author Vivek Pandey
 *
 * Defines parameter mode, IN, OUT or INOUT
 */

public enum Mode {
    IN(0), OUT(1), INOUT(2);

    private Mode(int mode){
        this.mode = mode;
    }
    private final int mode;
}
