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

/**
 * JAX-WS RI extension of JAX-WS API.
 *
 * <p>
 * This package hosts classes/interfaces that directly extend
 * the JAX-WS API. Sometimes objects of these types are passed
 * to external components from higher layers, only to be passed
 * back into other parts of the JAX-WS RI.
 * By defining these types, we improve the type-safety in
 * this scenario, while isolating the actual implementation classes.
 *
 * <p>
 * Sometimes these types also define additional methods.
 *
 * <p>
 * Types defined in package can only be implemented by the JAX-WS RI.
 * The code internal to the JAX-WS RI may safely case instances
 * of these types to their implementation classes. This warning doesn't
 * apply to subpackages.
 */
package com.sun.xml.ws.api;