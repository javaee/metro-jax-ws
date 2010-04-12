/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.xml.ws.api.config.management.Reconfigurable;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.message.Packet;
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
public abstract class Adapter<TK extends Adapter.Toolkit>
        implements Reconfigurable, EndpointComponent {

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
     *
     * Instances of this pool may be replaced at runtime. Therefore, when you take
     * an object out of the pool, you must make sure that it is recycled by the
     * same instance of the pool.
     */
    protected volatile Pool<TK> pool = new Pool<TK>() {
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
        // Enables other components to reconfigure this adapter
        endpoint.getComponentRegistry().add(this);
    }

    /**
     * The pool instance needs to be recreated to prevent reuse of old Toolkit instances.
     */
    public void reconfigure() {
        this.pool = new Pool<TK>() {
            protected TK create() {
                return createToolkit();
            }
        };
    }

    public <T> T getSPI(Class<T> spiType) {
        if (spiType.isAssignableFrom(Reconfigurable.class)) {
            return spiType.cast(this);
        }
        else {
            return null;
        }
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
     * Returns a reference to the pool of Toolkits for this adapter.
     *
     * The pool may be recreated during runtime reconfiguration and this method
     * will then return a reference to a new instance. When you recycle a toolkit,
     * you must make sure that you return it to the same pool instance that you
     * took it from.
     *
     * @return
     */
    protected Pool<TK> getPool() {
        return pool;
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
