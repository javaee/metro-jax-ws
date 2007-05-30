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
