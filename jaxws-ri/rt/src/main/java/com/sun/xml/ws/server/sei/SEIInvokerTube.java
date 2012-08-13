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

package com.sun.xml.ws.server.sei;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.client.sei.MethodHandler;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.server.InvokerTube;
import com.sun.xml.ws.wsdl.DispatchException;
import org.jvnet.ws.databinding.JavaCallInfo;
import java.lang.reflect.InvocationTargetException;

/**
 * This pipe is used to invoke SEI based endpoints.
 *
 * @author Jitendra Kotamraju
 */
public class SEIInvokerTube extends InvokerTube {

    /**
     * For each method on the port interface we have
     * a {@link MethodHandler} that processes it.
     */
    private final WSBinding binding;
    private final AbstractSEIModelImpl model;

    public SEIInvokerTube(AbstractSEIModelImpl model,Invoker invoker, WSBinding binding) {
        super(invoker);
        this.binding = binding;
        this.model = model;
    }

    /**
     * This binds the parameters for SEI endpoints and invokes the endpoint method. The
     * return value, and response Holder arguments are used to create a new {@link Message}
     * that traverses through the Pipeline to transport.
     */
    public @NotNull NextAction processRequest(@NotNull Packet req) {
        	JavaCallInfo call = model.getDatabinding().deserializeRequest(req);
        	if (call.getException() == null) {
	        	try {
	        		if (req.getMessage().isOneWay(model.getPort()) && req.transportBackChannel != null) {
	        			req.transportBackChannel.close();
	        		}
	        		Object ret = getInvoker(req).invoke(req, call.getMethod(), call.getParameters());
	        		call.setReturnValue(ret);
				} catch (InvocationTargetException e) {
					call.setException(e);
				} catch (Exception e) {
					call.setException(e);
				}
			} else if (call.getException() instanceof DispatchException) {
			    DispatchException e = (DispatchException)call.getException();
			    return doReturnWith(req.createServerResponse(e.fault, model.getPort(), null, binding));
			}
                        Packet res = (Packet) model.getDatabinding().serializeResponse(call);        	
			res = req.relateServerResponse(res, req.endpoint.getPort(), model, req.endpoint.getBinding());
            assert res != null;
            return doReturnWith(res);
    }

    public @NotNull NextAction processResponse(@NotNull Packet response) {
        return doReturnWith(response);
    }

    public @NotNull NextAction processException(@NotNull Throwable t) {
        return doThrow(t);
    }

}
