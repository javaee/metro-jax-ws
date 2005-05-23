/**
 * $Id: XmlTreeWriterException.java,v 1.1 2005-05-23 22:59:38 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.streaming;

import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author JAX-RPC Development Team
 *
 *
 */
public class XmlTreeWriterException extends XMLWriterException {

    public XmlTreeWriterException(String key) {
        super(key);
    }

    public XmlTreeWriterException(String key, String arg) {
        super(key, arg);
    }

    public XmlTreeWriterException(String key, Object[] args) {
        super(key, args);
    }

    public XmlTreeWriterException(String key, Localizable arg) {
        super(key, arg);
    }
}
