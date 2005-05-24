/*
 * $Id: ActionConstants.java,v 1.1 2005-05-24 13:51:44 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wscompile;

/**
 * @author JAX-RPC Development Team
 */
public interface ActionConstants {
    public static final String ACTION_SERVICE_INTERFACE_GENERATOR =
        "service.interface.generator";
    public static final String ACTION_REMOTE_INTERFACE_GENERATOR  =
        "remote.interface.impl.generator";
    public static final String ACTION_REMOTE_INTERFACE_IMPL_GENERATOR  =
        "remote.interface.impl.generator";
    public static final String ACTION_JAXB_TYPE_GENERATOR =
        "jaxb.type.generator";
    public static final String ACTION_CUSTOM_EXCEPTION_GENERATOR = 
        "custom.exception.generator";
    public static final String ACTION_WSDL_GENERATOR = 
        "wsdl.generator";
}
