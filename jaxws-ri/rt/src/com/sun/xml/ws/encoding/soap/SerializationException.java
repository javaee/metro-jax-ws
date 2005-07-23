/*
 * $Id: SerializationException.java,v 1.4 2005-07-23 04:10:03 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;
import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * SerializationException represents an exception that occurred while
 * serializing a Java value as XML.
 * 
 * @see JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class SerializationException extends JAXWSExceptionBase {

    public SerializationException(String key) {
        super(key);
    }

    public SerializationException(String key, String arg) {
        super(key, arg);
    }

    public SerializationException(String key, Object[] args) {
        super(key, args);
    }

    public SerializationException(String key, Localizable arg) {
        super(key, arg);
    }

    public SerializationException(Localizable arg) {
        super("nestedSerializationError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.encoding";
    }

}
