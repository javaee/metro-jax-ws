/**
 * $Id: ServerRtException.java,v 1.3 2005-07-23 04:10:11 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.util.exception.JAXWSExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 */
public class ServerRtException extends JAXWSExceptionBase {

    public ServerRtException(String key) {
        super(key);
    }

    public ServerRtException(String key, String arg) {
        super(key, arg);
    }

    public ServerRtException(String key, Object[] args) {
        super(key, args);
    }

    public ServerRtException(String key, Localizable arg) {
        super(key, arg);
    }

    public ServerRtException(Localizable arg) {
        super("server.rt.err", arg);
    }

    /* (non-Javadoc)
     * @see JAXWSExceptionBase#getResourceBundleName()
     */
    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.server";
    }

}
