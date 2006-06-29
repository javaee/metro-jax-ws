/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
