/**
 * $Id: XMLMessageException.java,v 1.1 2005-07-27 01:31:23 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.protocol.xml;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 * @author WS Development Team
 */
public class XMLMessageException extends JAXWSExceptionBase {

    public XMLMessageException(String key) {
        super(key);
    }

    public XMLMessageException(String key, String arg) {
        super(key, arg);
    }

    public XMLMessageException(String key, Object[] args) {
        super(key, args);
    }

    public XMLMessageException(String key, Localizable arg) {
        super(key, arg);
    }

    public XMLMessageException(Localizable arg) {
        super("server.rt.err", arg);
    }

    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.xmlmessage";
    }

}
