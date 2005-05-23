/*
 *$Id: SOAPVersion.java,v 1.1 2005-05-23 22:30:15 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;

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

    public static final SOAPVersion SOAP_11 = new SOAPVersion("soap1.1");
    public static final SOAPVersion SOAP_12 = new SOAPVersion("soap1.2");
}
