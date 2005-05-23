/**
 * $Id: ServerRtException.java,v 1.1 2005-05-23 22:50:26 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.util.exception.JAXRPCExceptionBase;
import com.sun.xml.ws.util.localization.Localizable;

/**
 */
public class ServerRtException extends JAXRPCExceptionBase {

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
     * @see com.sun.xml.rpc.util.exception.JAXRPCExceptionBase#getResourceBundleName()
     */
    public String getResourceBundleName() {
        return "com.sun.xml.ws.resources.server";
    }

}
