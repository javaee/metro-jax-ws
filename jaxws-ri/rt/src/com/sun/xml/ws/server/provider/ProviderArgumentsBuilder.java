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
package com.sun.xml.ws.server.provider;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;

import javax.xml.ws.soap.SOAPBinding;

/**
 * @author Jitendra Kotamraju
 */

abstract class ProviderArgumentsBuilder<T> {

    /**
     * Creates a fault {@link Message} from method invocation's exception
     */
    protected abstract Message getResponseMessage(Exception e);

    /**
     * Creates {@link Message} from method invocation's return value
     */
    protected Packet getResponse(Packet request, Exception e, WSDLPort port, WSBinding binding) {
        Message message = getResponseMessage(e);
        Packet response = request.createServerResponse(message,port,null,binding);
        return response;
    }

    /**
     * Binds {@link com.sun.xml.ws.api.message.Message} to method invocation parameter
     * @param packet
     */
    protected abstract T getParameter(Packet packet);

    protected abstract Message getResponseMessage(T returnValue);

    /**
     * Creates {@link Packet} from method invocation's return value
     */
    protected Packet getResponse(Packet request, @Nullable T returnValue, WSDLPort port, WSBinding binding) {
        Message message = null;
        if (returnValue != null) {
            message = getResponseMessage(returnValue);
        }
        Packet response = request.createServerResponse(message,port,null,binding);
        return response;
    }

    public static ProviderArgumentsBuilder<?> create(ProviderEndpointModel model, WSBinding binding) {
        return (binding instanceof SOAPBinding) ? SOAPProviderArgumentBuilder.create(model, binding.getSOAPVersion())
                : XMLProviderArgumentBuilder.create(model);
    }

}
