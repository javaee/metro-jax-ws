/*
 * $Id: DeserializationException.java,v 1.2 2005-05-25 20:16:25 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * DeserializationException represents an exception that occurred while
 * deserializing a Java value from XML.
 * 
 * @see com.sun.xml.rpc.util.exception.JAXWSExceptionBase
 * 
 * @author JAX-RPC Development Team
 */
public class DeserializationException extends JAXWSExceptionBase {

    public DeserializationException(String key) {
        super(key);
    }

    public DeserializationException(String key, String arg) {
        super(key, arg);
    }

    public DeserializationException(String key, Object[] args) {
        super(key, args);
    }

    public DeserializationException(String key, Localizable arg) {
        super(key, arg);
    }

    public DeserializationException(Localizable arg) {
        super("nestedDeserializationError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.encoding";
    }
}
