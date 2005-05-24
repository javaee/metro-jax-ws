/*
 * $Id: Kinds.java,v 1.1 2005-05-24 14:00:46 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import com.sun.tools.ws.wsdl.framework.Kind;

/**
 * Enumeration of the kind of entities that can be defined in a WSDL "definitions" element.
 *
 * @author JAX-RPC Development Team
 */
public class Kinds {
    public static final Kind BINDING = new Kind("wsdl:binding");
    public static final Kind MESSAGE = new Kind("wsdl:message");
    public static final Kind PORT = new Kind("wsdl:port");
    public static final Kind PORT_TYPE = new Kind("wsdl:portType");
    public static final Kind SERVICE = new Kind("wsdl:service");

    private Kinds() {
    }
}
