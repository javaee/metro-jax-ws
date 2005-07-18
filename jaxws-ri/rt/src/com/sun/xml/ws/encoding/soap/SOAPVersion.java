/*
 *$Id: SOAPVersion.java,v 1.4 2005-07-18 16:52:14 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap;

import javax.xml.ws.soap.SOAPBinding;


/**
 * @author WS Development Team
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
