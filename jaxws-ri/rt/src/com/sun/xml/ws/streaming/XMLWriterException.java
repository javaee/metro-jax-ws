/*
 * $Id: XMLWriterException.java,v 1.1 2005-05-23 22:59:37 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * <p> XMLWriterException represents an exception that occurred while writing
 * an XML document. </p>
 *
 * @see XMLWriter
 * @see com.sun.xml.rpc.util.exception.JAXRPCExceptionBase
 *
 * @author JAX-RPC Development Team
 */
public class XMLWriterException extends JAXRPCExceptionBase {

    public XMLWriterException(String key) {
        super(key);
    }

    public XMLWriterException(String key, String arg) {
        super(key, arg);
    }

    public XMLWriterException(String key, Object[] args) {
        super(key, args);
    }

    public XMLWriterException(String key, Localizable arg) {
        super(key, arg);
    }

    public XMLWriterException(Localizable arg) {
        super("xmlwriter.nestedError", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.streaming";
    }
}
