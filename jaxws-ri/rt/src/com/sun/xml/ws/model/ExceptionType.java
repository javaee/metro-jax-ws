/**
 * $Id: ExceptionType.java,v 1.1 2005-05-23 22:42:09 bbissett Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;
/**
 * @author Vivek Pandey
 *
 * Type of java exception
 */
public enum ExceptionType {
    WSDLException(0), UserDefined(1);

    ExceptionType(int exceptionType){
        this.exceptionType = exceptionType;
    }

    public int value() {
        return exceptionType;
    }
    private final int exceptionType;
}
