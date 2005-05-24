/*
 * $Id: SOAPUse.java,v 1.1 2005-05-24 13:58:15 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

/**
 * A SOAP "use" enumeration.
 *
 * @author JAX-RPC Development Team
 */
public final class SOAPUse {

    public static final SOAPUse LITERAL = new SOAPUse();
    public static final SOAPUse ENCODED = new SOAPUse();

    private SOAPUse() {
    }
}
