/*
 * $Id: SOAPStyle.java,v 1.2 2005-07-18 18:14:18 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

/**
 * A SOAP "style" enumeration.
 *
 * @author WS Development Team
 */
public final class SOAPStyle {

    public static final SOAPStyle RPC = new SOAPStyle();
    public static final SOAPStyle DOCUMENT = new SOAPStyle();

    private SOAPStyle() {
    }
}
