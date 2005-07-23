/*
 * $Id: XMLStreamReaderException.java,v 1.4 2005-07-23 04:10:13 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * <p> XMLStream ReaderException represents an exception that occurred while reading an
 * XML document. </p>
 * 
 * @see JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class XMLStreamReaderException extends JAXWSExceptionBase {

    public XMLStreamReaderException(String key) {
        super(key);
    }

    public XMLStreamReaderException(String key, String arg) {
        super(key, arg);
    }

    public XMLStreamReaderException(String key, Object[] args) {
        super(key, args);
    }

    public XMLStreamReaderException(String key, Localizable arg) {
        super(key, arg);
    }

    public XMLStreamReaderException(Localizable arg) {
        super("xmlreader.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.streaming";
    }
}
