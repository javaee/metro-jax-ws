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
package com.sun.xml.ws.server.sei;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.client.sei.MethodHandler;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.server.InvokerTube;

import java.util.List;

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
    private final SOAPVersion soapVersion;
    private final WSBinding binding;
    private final AbstractSEIModelImpl model;
    private final List<EndpointMethodDispatcher> dispatcherList;

    public SEIInvokerTube(AbstractSEIModelImpl model,Invoker invoker, WSBinding binding) {
        super(invoker);
        this.soapVersion = binding.getSOAPVersion();
        this.binding = binding;
        this.model = model;
        EndpointMethodDispatcherGetter methodDispatcherGetter = new EndpointMethodDispatcherGetter(model, binding, this);
        dispatcherList = methodDispatcherGetter.getDispatcherList();
    }

    /**
     * This binds the parameters for SEI endpoints and invokes the endpoint method. The
     * return value, and response Holder arguments are used to create a new {@link Message}
     * that traverses through the Pipeline to transport.
     */
    public @NotNull NextAction processRequest(@NotNull Packet req) {
        Packet res = null;

        try {
            for (EndpointMethodDispatcher dispatcher : dispatcherList) {
                EndpointMethodHandler handler = dispatcher.getEndpointMethodHandler(req);
                if (handler != null) {
                    res = handler.invoke(req);
                    break;
                }
            }
        } catch (DispatchException e) {
            return doReturnWith(req.createServerResponse(e.fault, model.getPort(), null, binding));
        }

        // PayloadQNameBasedDispatcher should throw DispatchException
        assert res!=null;

        return doReturnWith(res);
    }

    public @NotNull NextAction processResponse(@NotNull Packet response) {
        throw new IllegalStateException("InovkerPipe's processResponse shouldn't be called.");
    }

    public @NotNull NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException("InovkerPipe's processException shouldn't be called.");
    }

}
