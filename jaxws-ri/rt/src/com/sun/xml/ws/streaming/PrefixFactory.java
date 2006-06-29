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

package com.sun.xml.ws.streaming;

/**
 * <p> Interface for prefix factories. </p>
 *
 * <p> A prefix factory is able to create a new prefix for a URI that
 * was encountered for the first time when writing a document
 * using an XMLWriter. </p>
 *
 * @author WS Development Team
 */
public interface PrefixFactory {
    /**
     * Return a brand new prefix for the given URI.
     */
    public String getPrefix(String uri);
}
