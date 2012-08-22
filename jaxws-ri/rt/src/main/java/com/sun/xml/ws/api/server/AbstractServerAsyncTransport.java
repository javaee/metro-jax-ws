/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.util.Pool;

import java.io.IOException;

import org.jvnet.ws.message.PropertySet;

/**
 * Partial server side async transport implementation. It manages pooling of
 * {@link Codec} and other details.
 *
 * @author Jitendra Kotamraju
 */
public abstract class AbstractServerAsyncTransport<T> {
    
    private final WSEndpoint endpoint;
    private final CodecPool codecPool;

    /**
     * {@link WSEndpoint#setExecutor} should be called before creating the
     * transport
     *
     * @param endpoint webservices requests are directed towards this endpoint
     */
    public AbstractServerAsyncTransport(WSEndpoint endpoint) {
        this.endpoint = endpoint;
        codecPool = new CodecPool(endpoint);
    }

    /**
     * decodes the transport data to Packet
     *
     * @param connection that carries the web service request
     * @param codec for encoding/decoding {@link Message}
     * @return decoded {@link Packet}
     * @throws IOException if an i/o error happens while encoding/decoding
     */
    protected Packet decodePacket(T connection, @NotNull Codec codec) throws IOException {
        Packet packet = new Packet();
        packet.acceptableMimeTypes = getAcceptableMimeTypes(connection);
        packet.addSatellite(getPropertySet(connection));
        packet.transportBackChannel = getTransportBackChannel(connection);
        return packet;
    }

    /**
     * Encodes the {@link Packet} to infoset and writes on the connection.
     *
     * @param connection that carries the web service request
     * @param packet that needs to encoded to infoset
     * @param codec that does the encoding of Packet
     * @throws IOException if an i/o error happens while encoding/decoding
     */
    protected abstract void encodePacket(T connection, @NotNull Packet packet, @NotNull Codec codec) throws IOException;

    /**
     * If the request has Accept header, return that value
     *
     * @param connection that carries the web service request
     * @return Accept MIME types
     */
    protected abstract @Nullable String getAcceptableMimeTypes(T connection);

    /**
     * {@link TransportBackChannel} used by jax-ws runtime to close the connection
     * while the processing of the request is still continuing. In oneway HTTP case, a
     * response code needs to be sent before invoking the endpoint.
     *
     * @param connection that carries the web service request
     * @return TransportBackChannel instance using the connection
     */
    protected abstract @Nullable TransportBackChannel getTransportBackChannel(T connection);

    /**
     * If there are any properties associated with the connection, those will
     * be added to {@link Packet}
     *
     * @param connection that carries the web service request
     * @return {@link PropertySet} for the connection
     */
    protected abstract @NotNull PropertySet getPropertySet(T connection);

    /**
     * Return a {@link WebServiceContextDelegate} using the underlying connection.
     *
     * @param connection that carries the web service request
     * @return non-null WebServiceContextDelegate instance
     */
    protected abstract @NotNull WebServiceContextDelegate getWebServiceContextDelegate(T connection);

    /**
     * Reads and decodes infoset from the connection and invokes the endpoints. The
     * response is encoded and written to the connection. The response could be
     * written using a different thread.
     *
     * @param connection that carries the web service request
     * @throws IOException if an i/o error happens while encoding/decoding
     */
    protected void handle(final T connection) throws IOException {
        final Codec codec = codecPool.take();
        Packet request = decodePacket(connection, codec);
        if (!request.getMessage().isFault()) {
            endpoint.schedule(request, new WSEndpoint.CompletionCallback() {
                public void onCompletion(@NotNull Packet response) {
                    try {
                        encodePacket(connection, response, codec);
                    } catch(IOException ioe) {
                        ioe.printStackTrace();
                    }
                    codecPool.recycle(codec);
                }
            });
        }
    }

    private static final class CodecPool extends Pool<Codec> {
        WSEndpoint endpoint;

        CodecPool(WSEndpoint endpoint) {
            this. endpoint = endpoint;
        }

        protected Codec create() {
            return endpoint.createCodec();
        }
    }

}
