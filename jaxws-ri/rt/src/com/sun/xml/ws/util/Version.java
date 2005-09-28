/*
 * $Id: Version.java,v 1.14 2005-09-28 20:53:10 kohlert Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
	public static final String BUILD_NUMBER = "EA3_B9";
}
