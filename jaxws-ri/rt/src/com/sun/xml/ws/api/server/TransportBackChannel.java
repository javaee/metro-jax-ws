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

package com.sun.xml.ws.api.server;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSEndpoint.PipeHead;

/**
 * Represents a transport back-channel.
 *
 * <p>
 * When the JAX-WS runtime finds out that the request
 * {@link Packet} being processed is known not to produce
 * a response, it invokes the {@link #close()} method
 * to indicate that the transport does not need to keep
 * the channel for the response message open.
 *
 * <p>
 * This allows the transport to close down the communication
 * channel sooner than wainting for
 * {@link PipeHead#process}
 * method to return, thereby improving the overall throughput
 * of the system.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitu
 */
public interface TransportBackChannel {
    /**
     * See the class javadoc for the discussion.
     *
     * <p>
     * JAX-WS is not guaranteed to call this method for all
     * operations that do not have a response. This is merely
     * a hint.
     *
     * <p>
     * When the implementation of this method fails to close
     * the connection successfuly, it should record the error,
     * and return normally. Do not throw any exception.
     */
    void close();
}
