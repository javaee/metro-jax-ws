/*
 * $Id: SOAPUse.java,v 1.2 2005-07-18 18:14:18 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

/**
 * A SOAP "use" enumeration.
 *
 * @author WS Development Team
 */
public final class SOAPUse {

    public static final SOAPUse LITERAL = new SOAPUse();
    public static final SOAPUse ENCODED = new SOAPUse();

    private SOAPUse() {
    }
}
