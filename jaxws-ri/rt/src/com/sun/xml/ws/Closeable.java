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

package com.sun.xml.ws;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.Dispatch;
import java.io.IOException;

/**
 * Closeable JAX-WS proxy object.
 *
 * @author Kohsuke Kawaguchi
 * @since JAX-WS 2.0.2
 */
// this interface is exposed to applications.
public interface Closeable extends java.io.Closeable {
    /**
     * Closes this object and cleans up any resources
     * it holds, such as network connections.
     *
     * <p>
     * This interface is implemented by a port proxy
     * or {@link Dispatch}. In particular, this signals
     * the implementation of certain specs (like WS-ReliableMessaging
     * and WS-SecureConversation) to terminate sessions that they
     * create during the life time of a proxy object.
     *
     * <p>
     * This is not a mandatory operation, so the application
     * does not have to call this method.
     *
     *
     * @throws WebServiceException
     *      If clean up fails unexpectedly, this exception
     *      will be thrown (instead of {@link IOException}.
     */
    public void close() throws WebServiceException;
}
