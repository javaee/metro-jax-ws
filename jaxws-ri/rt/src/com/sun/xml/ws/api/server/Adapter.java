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

import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.WSEndpoint.PipeHead;
import com.sun.xml.ws.util.Pool;

/**
 * Receives incoming messages from a transport (such as HTTP, JMS, etc)
 * in a transport specific way, and delivers it to {@link WSEndpoint.PipeHead#process}.
 *
 * <p>
 * Since this class mostly concerns itself with converting a
 * transport-specific message representation to a {@link Packet},
 * the name is the "adapter".
 *
 * <p>
 * The purpose of this class is twofolds:
 *
 * <ol>
 * <li>
 * To hide the logic of converting a transport-specific connection
 * to a {@link Packet} and do the other way around.
 *
 * <li>
 * To manage thread-unsafe resources, such as {@link WSEndpoint.PipeHead},
 * and {@link Codec}.
 * </ol>
 *
 * <p>
 * {@link Adapter}s are extended to work with each kind of transport,
 * and therefore {@link Adapter} class itself is not all that
 * useful by itself --- it merely provides a design template
 * that can be followed.
 *
 * <p>
 * For managing resources, an adapter uses an object called {@link Toolkit}
 * (think of it as a tray full of tools that a dentist uses ---
 * trays are identical, but each patient has to get one. You have
 * a pool of them and you assign it to a patient.)
 *
 * {@link Adapter.Toolkit} can be extended by derived classes.
 * That actual type is the {@code TK} type parameter this class takes.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Adapter<TK extends Adapter.Toolkit> {

    protected final WSEndpoint<?> endpoint;

    /**
     * Object that groups all thread-unsafe resources.
     */
    public class Toolkit {
        /**
         * For encoding/decoding infoset to/from the byte stream.
         */
        public final Codec codec;
        /**
         * This object from {@link WSEndpoint} serves the request.
         */
        public final PipeHead head;

        public Toolkit() {
            this.codec = endpoint.createCodec();
            this.head = endpoint.createPipeHead();
        }
    }

    /**
     * Pool of {@link Toolkit}s.
     */
    protected final Pool<TK> pool = new Pool<TK>() {
        protected TK create() {
            return createToolkit();
        }
    };

    /**
     * Creates an {@link Adapter} that delivers
     * messages to the given endpoint.
     */
    protected Adapter(WSEndpoint endpoint) {
        assert endpoint!=null;
        this.endpoint = endpoint;
    }

    /**
     * Gets the endpoint that this {@link Adapter} is serving.
     *
     * @return
     *      always non-null.
     */
    public WSEndpoint<?> getEndpoint() {
        return endpoint;
    }

    /**
     * Creates a {@link Toolkit} instance.
     *
     * <p>
     * If the derived class doesn't have to add any per-thread state
     * to {@link Toolkit}, simply implement this as {@code new Toolkit()}.
     */
    protected abstract TK createToolkit();
}
