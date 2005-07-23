/*
 * $Id: XMLStreamWriterException.java,v 1.3 2005-07-23 04:10:14 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * <p> XMLWriterException represents an exception that occurred while writing
 * an XML document. </p>
 * 
 * @see JAXWSExceptionBase
 * 
 * @author WS Development Team
 */
public class XMLStreamWriterException extends JAXWSExceptionBase {

    public XMLStreamWriterException(String key) {
        super(key);
    }

    public XMLStreamWriterException(String key, String arg) {
        super(key, arg);
    }

    public XMLStreamWriterException(String key, Object[] args) {
        super(key, args);
    }

    public XMLStreamWriterException(String key, Localizable arg) {
        super(key, arg);
    }

    public XMLStreamWriterException(Localizable arg) {
        super("xmlwriter.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.streaming";
    }
}
