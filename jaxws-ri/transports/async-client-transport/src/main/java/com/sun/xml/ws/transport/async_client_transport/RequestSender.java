/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.transport.async_client_transport;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.util.Pool;
import com.sun.istack.NotNull;

import javax.xml.ws.WebServiceException;
import java.io.Closeable;

/**
 * @author Rama.Pulavarthi@sun.com
 */
public class RequestSender implements Closeable {
    private final Tube masterTubeline;
    private Pool<Tube> tubelinePool;
    private volatile Engine engine;

    public RequestSender(String name, Tube tubeline) {
        this.masterTubeline = tubeline;
        this.engine = new Engine(name);
        this.tubelinePool = new Pool.TubePool(masterTubeline);

    }

    /**
     * Sends the request {@link com.sun.xml.ws.api.message.Packet} and returns the corresponding response {@link com.sun.xml.ws.api.message.Packet}.
     * This method should be used for Req-Resp MEP
     *
     * @param request {@link com.sun.xml.ws.api.message.Packet} containing the message to be send
     * @return response {@link com.sun.xml.ws.api.message.Message} wrapped in a response {@link com.sun.xml.ws.api.message.Packet} received
     */
    public Packet send(Packet request) {
        if (tubelinePool == null)
                        throw new WebServiceException("close method has already been invoked"); // TODO: i18n
        
        final Tube tubeline = tubelinePool.take();
        try {
            return engine.createFiber().runSync(tubeline, request);
        } finally {
            tubelinePool.recycle(tubeline);
        }
    }

    public void sendAsync(Packet request, final Fiber.CompletionCallback completionCallback) {
        if (tubelinePool == null)
                    throw new WebServiceException("close method has already been invoked"); // TODO: i18n

        Fiber fiber = engine.createFiber();
        final Tube tube = tubelinePool.take();
        fiber.start(tube, request, new Fiber.CompletionCallback() {
            public void onCompletion(@NotNull Packet response) {
                tubelinePool.recycle(tube);
                completionCallback.onCompletion(response);
            }

            public void onCompletion(@NotNull Throwable error) {
                // let's not reuse tubes as they might be in a wrong state, so not
                // calling tubePool.recycle()
                completionCallback.onCompletion(error);
            }
        });
    }

    public void close() {
       if (tubelinePool != null) {
            // multi-thread safety of 'close' needs to be considered more carefully.
            // some calls might be pending while this method is invoked. Should we
            // block until they are complete, or should we abort them (but how?)
            Tube p = tubelinePool.take();
            tubelinePool = null;
            p.preDestroy();
        }
    }
}

