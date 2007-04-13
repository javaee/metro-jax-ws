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

package com.sun.xml.ws.server.sei;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.JavaMethodImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link EndpointMethodDispatcher} that uses
 * WS-Addressing Action Message Addressing Property, <code>wsa:Action</code>,
 * as the key for dispatching.
 * <p/>
 * A map of all wsa:Actions on the port and the corresponding {@link EndpointMethodHandler}
 * is initialized in the constructor. The wsa:Action value is extracted from
 * the request {@link Packet} and used as the key to return the correct
 * handler.
 *
 * @author Arun Gupta
 */
final class ActionBasedDispatcher implements EndpointMethodDispatcher {
    private final WSBinding binding;
    private final Map<String, EndpointMethodHandler> actionMethodHandlers;
    private final @NotNull AddressingVersion av;

    public ActionBasedDispatcher(AbstractSEIModelImpl model, WSBinding binding, SEIInvokerTube invokerTube) {
        this.binding = binding;
        assert binding.getAddressingVersion()!=null;    // this dispatcher can be only used when addressing is on.
        av = binding.getAddressingVersion();
        actionMethodHandlers = new HashMap<String, EndpointMethodHandler>();

        for( JavaMethodImpl m : model.getJavaMethods() ) {
            EndpointMethodHandler handler = new EndpointMethodHandler(invokerTube,m,binding);
            String action = m.getInputAction();
            //first look at annotations and then in wsdlmodel
            if(action != null && !action.equals("")) {
                actionMethodHandlers.put(action, handler);
            } else {
                action = m.getOperation().getOperation().getInput().getAction();
                if (action != null)
                    actionMethodHandlers.put(action, handler);
            }    
        }
    }

    public EndpointMethodHandler getEndpointMethodHandler(Packet request) throws DispatchException {

        HeaderList hl = request.getMessage().getHeaders();

        String action = hl.getAction(av, binding.getSOAPVersion());

        if (action == null)
            // this message doesn't contain addressing headers, which is legal.
            // this happens when the server is capable of processing addressing but the client didn't send them
            return null;

        EndpointMethodHandler h = actionMethodHandlers.get(action);
        if (h != null)
            return h;

        // invalid action header
        Message result = Messages.create(action, av, binding.getSOAPVersion());

        throw new DispatchException(result);
    }
}
