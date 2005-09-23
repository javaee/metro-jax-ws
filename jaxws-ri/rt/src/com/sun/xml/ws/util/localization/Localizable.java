/*
 * $Id: Localizable.java,v 1.4 2005-09-23 22:05:38 kohsuke Exp $
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

package com.sun.xml.ws.util.localization;

/**
 * Localizable message.
 *
 * @author WS Development Team
 */
public interface Localizable {
    /**
     * Gets the key in the resource bundle.
     *
     * @return
     *      if this method returns {@link #NOT_LOCALIZABLE},
     *      that means the message is not localizable, and
     *      the first item of {@link #getArguments()} array
     *      holds a String.
     */
    public String getKey();

    /**
     * Returns the arguments for message formatting.
     *
     * @return
     *      can be an array of length 0 but never be null.
     */
    public Object[] getArguments();
    public String getResourceBundleName();


    /**
     * Special constant that represents a message that
     * is not localizable.
     *
     * <p>
     * Use of "new" is to create an unique instance.
     */
    public static final String NOT_LOCALIZABLE = new String("\u0000");
}
