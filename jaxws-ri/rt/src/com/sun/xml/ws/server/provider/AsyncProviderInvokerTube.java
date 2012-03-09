/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.server.provider;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.server.AbstractWebServiceContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Tube} is used to invoke the {@link AsyncProvider} endpoints.
 *
 * @author Jitendra Kotamraju
 */
public // TODO needed by factory
class AsyncProviderInvokerTube<T> extends ProviderInvokerTube<T> {

    private static final Logger LOGGER = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.AsyncProviderInvokerTube");

    public AsyncProviderInvokerTube(Invoker invoker, ProviderArgumentsBuilder<T> argsBuilder) {
        super(invoker, argsBuilder);
    }

   /*
    * This binds the parameter for Provider endpoints and invokes the
    * invoke() method of {@linke Provider} endpoint. The return value from
    * invoke() is used to create a new {@link Message} that traverses
    * through the Pipeline to transport.
    */
    public @NotNull NextAction processRequest(@NotNull Packet request) {
        T param = argsBuilder.getParameter(request);
        NoSuspendResumer resumer = new NoSuspendResumer();
        @SuppressWarnings({ "rawtypes", "unchecked" })
		AsyncProviderCallbackImpl callback = new AsyncProviderInvokerTube.AsyncProviderCallbackImpl(request, resumer);
        AsyncWebServiceContext ctxt = new AsyncWebServiceContext(getEndpoint(),request);

        AsyncProviderInvokerTube.LOGGER.fine("Invoking AsyncProvider Endpoint");
        try {
            getInvoker(request).invokeAsyncProvider(request, param, callback, ctxt);
        } catch(Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return doThrow(e);
        }
        
        synchronized(callback) {
        	if (resumer.response != null)
        		return doReturnWith(resumer.response);
        
	        // Suspend the Fiber. AsyncProviderCallback will resume the Fiber after
	        // it receives response.
        	callback.resumer = new FiberResumer();
	        return doSuspend();
        }
    }
    
    private interface Resumer {
    	public void onResume(Packet response);
    }
    
    /*private*/ public class FiberResumer implements Resumer { // TODO public for DISI
    	private final Fiber fiber;
    	
    	public FiberResumer() {
            this.fiber = Fiber.current();
    	}
    	
    	public void onResume(Packet response) {
    		fiber.resume(response);
    	}
    }
    
    private class NoSuspendResumer implements Resumer {
    	protected Packet response = null;

		public void onResume(Packet response) {
			this.response = response;
		}
    }

    /*private*/ public class AsyncProviderCallbackImpl implements AsyncProviderCallback<T> { // TODO public for DISI
        private final Packet request;
        private Resumer resumer;

        public AsyncProviderCallbackImpl(Packet request, Resumer resumer) {
            this.request = request;
            this.resumer = resumer;
        }

        public void send(@Nullable T param) {
            if (param == null) {
                if (request.transportBackChannel != null) {
                    request.transportBackChannel.close();
                }
            }
            Packet packet = argsBuilder.getResponse(request, param, getEndpoint().getPort(), getEndpoint().getBinding());
            synchronized(this) {
            	resumer.onResume(packet);
            }
        }

        public void sendError(@NotNull Throwable t) {
            Exception e;
            if (t instanceof RuntimeException) {
                e = (RuntimeException)t;
            } else {
                e = new RuntimeException(t);
            }
            Packet packet = argsBuilder.getResponse(request, e, getEndpoint().getPort(), getEndpoint().getBinding());
            synchronized(this) {
            	resumer.onResume(packet);
            }
        }
    }

    /**
     * The single {@link javax.xml.ws.WebServiceContext} instance injected into application.
     */
    /*private static final*/ public class AsyncWebServiceContext extends AbstractWebServiceContext { // TODO public for DISI
        final Packet packet;

        public AsyncWebServiceContext(WSEndpoint endpoint, Packet packet) { // TODO public for DISI
            super(endpoint);
            this.packet = packet;
        }

        public @NotNull Packet getRequestPacket() {
            return packet;
        }
    }

    public @NotNull NextAction processResponse(@NotNull Packet response) {
        return doReturnWith(response);
    }

    public @NotNull NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException("AsyncProviderInvokerTube's processException shouldn't be called.");
    }

}
