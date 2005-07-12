/**
 * $Id: ExceptionType.java,v 1.2 2005-07-12 23:32:50 kohlert Exp $
 */
/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.model;
/**
 * Type of java exception
 *
 * @author Vivek Pandey
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
