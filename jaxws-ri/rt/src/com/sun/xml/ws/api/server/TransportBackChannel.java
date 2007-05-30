/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
