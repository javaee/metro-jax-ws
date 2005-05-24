/*
 * $Id: SOAPStyle.java,v 1.1 2005-05-24 13:58:14 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

/**
 * A SOAP "style" enumeration.
 *
 * @author JAX-RPC Development Team
 */
public final class SOAPStyle {

    public static final SOAPStyle RPC = new SOAPStyle();
    public static final SOAPStyle DOCUMENT = new SOAPStyle();

    private SOAPStyle() {
    }
}
