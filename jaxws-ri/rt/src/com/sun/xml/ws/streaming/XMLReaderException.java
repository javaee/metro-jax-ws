/*
 * $Id: XMLReaderException.java,v 1.2 2005-05-25 20:16:31 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * <p> XMLReaderException represents an exception that occurred while reading an
 * XML document. </p>
 * 
 * @see XMLReader
 * @see com.sun.xml.rpc.util.exception.JAXWSExceptionBase
 * 
 * @author JAX-RPC Development Team
 */
public class XMLReaderException extends JAXWSExceptionBase {

    public XMLReaderException(String key) {
        super(key);
    }

    public XMLReaderException(String key, String arg) {
        super(key, arg);
    }

    public XMLReaderException(String key, Object[] args) {
        super(key, args);
    }

    public XMLReaderException(String key, Localizable arg) {
        super(key, arg);
    }

    public XMLReaderException(Localizable arg) {
        super("xmlreader.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.streaming";
    }
}
