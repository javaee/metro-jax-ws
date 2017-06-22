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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.resources.ServerMessages;
import com.sun.xml.ws.server.provider.ProviderInvokerTube;
import com.sun.xml.ws.server.sei.SEIInvokerTube;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base code for {@link ProviderInvokerTube} and {@link SEIInvokerTube}.
 *
 * <p>
 * This hides {@link InstanceResolver} and performs a set up
 * necessary for {@link WebServiceContext} to correctly.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InvokerTube<T> extends com.sun.xml.ws.server.sei.InvokerTube<Invoker> implements EndpointAwareTube {

    private WSEndpoint endpoint;

    protected InvokerTube(Invoker invoker) {
        super(invoker);
    }

    public void setEndpoint(WSEndpoint endpoint) {
        this.endpoint = endpoint;
        WSWebServiceContext webServiceContext = new AbstractWebServiceContext(endpoint) {
            public @Nullable Packet getRequestPacket() {
                Packet p = packets.get();
                return p;
            }
        };
        invoker.start(webServiceContext,endpoint);
    }

    protected WSEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the application object that serves the request.
     *
    public final @NotNull T getServant(Packet request) {
        // this allows WebServiceContext to find this packet
        packets.set(request);
        return invoker.resolve(request);
    }
     */

    /**
     * Returns the {@link Invoker} object that serves the request.
     */
    public final @NotNull Invoker getInvoker(Packet request) {
        return wrapper;
    }

    /**
     * processRequest() and processResponse() do not share any instance variables
     * while processing the request. {@link InvokerTube} is stateless and terminal,
     * so no need to create copies.
     */
    public final AbstractTubeImpl copy(TubeCloner cloner) {
        cloner.add(this,this);
        return this;
    }

    public void preDestroy() {
        invoker.dispose();
    }

    /**
     * Heart of {@link WebServiceContext}.
     * Remembers which thread is serving which packet.
     */
    private static final ThreadLocal<Packet> packets = new ThreadLocal<Packet>();

    /**
     * This method can be called while the user service is servicing the request
     * synchronously, to obtain the current request packet.
     *
     * <p>
     * This is primarily designed for {@link StatefulInstanceResolver}. Use with care.
     */
    public static @NotNull Packet getCurrentPacket() {
        Packet packet = packets.get();
        if(packet==null)
            throw new WebServiceException(ServerMessages.NO_CURRENT_PACKET());
        return packet;
    }

    /**
     * {@link Invoker} filter that sets and restores the current packet.
     */
    private final Invoker wrapper = new Invoker() {
        @Override
        public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException {
            Packet old = set(p);
            try {
                return invoker.invoke(p, m, args);
            } finally {
                set(old);
            }
        }

        @Override
        public <T>T invokeProvider(Packet p, T arg) throws IllegalAccessException, InvocationTargetException {
            Packet old = set(p);
            try {
                return invoker.invokeProvider(p, arg);
            } finally {
                set(old);
            }
        }

        @Override
        public <T>void invokeAsyncProvider(Packet p, T arg, AsyncProviderCallback cbak, WebServiceContext ctxt) throws IllegalAccessException, InvocationTargetException {
            Packet old = set(p);
            try {
                invoker.invokeAsyncProvider(p, arg, cbak, ctxt);
            } finally {
                set(old);
            }
        }

        private Packet set(Packet p) {
            Packet old = packets.get();
            packets.set(p);
            return old;
        }
    };

}
