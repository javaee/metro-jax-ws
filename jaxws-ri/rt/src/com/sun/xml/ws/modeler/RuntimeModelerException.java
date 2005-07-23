/*
 * $Id: RuntimeModelerException.java,v 1.3 2005-07-23 04:10:10 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.modeler;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * RuntimeModelerException represents an exception that occurred while
 * serializing a Java value as XML.
 * 
 * @see JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class RuntimeModelerException extends JAXWSExceptionBase {

    public RuntimeModelerException(String key) {
        super(key);
    }

    public RuntimeModelerException(String key, String arg) {
        super(key, arg);
    }

    public RuntimeModelerException(String key, Object[] args) {
        super(key, args);
    }

    public RuntimeModelerException(String key, Localizable arg) {
        super(key, arg);
    }

    public RuntimeModelerException(Localizable arg) {
        super("nestedModelerError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.modeler";
    }

}
