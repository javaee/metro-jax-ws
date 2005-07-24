/*
 * $Id: Exception.java,v 1.1 2005-07-24 01:48:49 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.document.jaxws;

/**
 * @author Vivek Pandey
 *
 * class representing jaxws:exception
 *
 */
public class Exception {

    public Exception(){}

    public Exception(CustomName name){
        this.className = name;
    }

    private CustomName className;
    /**
     * @return Returns the className.
     */
    public CustomName getClassName() {
        return className;
    }
    /**
     * @param className The className to set.
     */
    public void setClassName(CustomName className) {
        this.className = className;
    }
}
