/*
 * $Id: Version.java,v 1.4 2005-06-21 19:15:09 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util;

/**
 * This interface holds version information for the whole JAX-RPC RI.
 *
 * @author JAX-RPC Development Team
 */

public interface Version {

	/**
	 * JAX-RPC RI product name
	 */
	public static final String PRODUCT_NAME = "JAX-WS Standard Implementation";

	/**
	 * JAX-RPC RI version number
	 */
	public static final String VERSION_NUMBER = "2.0";

	/**
	 * JAX-RPC RI build number
	 */
        // TODO change this back to R11 for the FCS release.
//	public static final String BUILD_NUMBER = "R11";
	public static final String BUILD_NUMBER = "EA2_B3";
}
