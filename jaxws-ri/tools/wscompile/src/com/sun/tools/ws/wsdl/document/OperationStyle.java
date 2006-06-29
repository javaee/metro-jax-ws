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
