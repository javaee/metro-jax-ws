/*
 *$Id: SOAPVersion.java,v 1.3 2005-06-01 00:15:32 vivekp Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;

import javax.xml.ws.soap.SOAPBinding;


/**
 * @author JAX-RPC Development Team
 */

public class SOAPVersion {

    private final String version;

    private SOAPVersion(String ver) {
        this.version = ver;
    }

    public String toString() {
        return this.version;
    }

    public boolean equals(String strVersion) {
        return version.equals(strVersion);
    }

    public static final SOAPVersion SOAP_11 = new SOAPVersion(SOAPBinding.SOAP11HTTP_BINDING);
    public static final SOAPVersion SOAP_12 = new SOAPVersion(SOAPBinding.SOAP12HTTP_BINDING);
}
