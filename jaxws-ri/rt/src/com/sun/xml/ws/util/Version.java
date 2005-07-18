/*
 * $Id: Version.java,v 1.5 2005-07-18 16:52:31 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util;

/**
 * This interface holds version information for the whole JAX-WS RI.
 *
 * @author WS Development Team
 */

public interface Version {

	/**
	 * JAX-WS RI product name
	 */
	public static final String PRODUCT_NAME = "JAX-WS Standard Implementation";

	/**
	 * JAX-WS RI version number
	 */
	public static final String VERSION_NUMBER = "2.0";

	/**
	 * JAX-WS RI build number
	 */
        // TODO change this back to R11 for the FCS release.
//	public static final String BUILD_NUMBER = "R11";
	public static final String BUILD_NUMBER = "EA3_B1";
}
