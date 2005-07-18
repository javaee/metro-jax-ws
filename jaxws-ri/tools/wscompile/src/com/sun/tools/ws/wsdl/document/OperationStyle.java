/*
 * $Id: OperationStyle.java,v 1.2 2005-07-18 18:14:13 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

/**
 * Enumeration of the supported WSDL operation styles.
 *
 * @author WS Development Team
 */
public final class OperationStyle {

    public static final OperationStyle ONE_WAY = new OperationStyle();
    public static final OperationStyle REQUEST_RESPONSE = new OperationStyle();
    public static final OperationStyle SOLICIT_RESPONSE = new OperationStyle();
    public static final OperationStyle NOTIFICATION = new OperationStyle();

    private OperationStyle() {
    }
}
