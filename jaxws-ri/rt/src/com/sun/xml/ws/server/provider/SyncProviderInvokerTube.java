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

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.server.Invoker;

import javax.xml.ws.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This tube is used to invoke the {@link Provider} endpoints.
 *
 * @author Jitendra Kotamraju
 */
class SyncProviderInvokerTube<T> extends ProviderInvokerTube<T> {

    private static final Logger LOGGER = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.SyncProviderInvokerTube");

    public SyncProviderInvokerTube(Invoker invoker, ProviderArgumentsBuilder<T> argsBuilder) {
        super(invoker, argsBuilder);
    }

    /*
    * This binds the parameter for Provider endpoints and invokes the
    * invoke() method of {@linke Provider} endpoint. The return value from
    * invoke() is used to create a new {@link Message} that traverses
    * through the Pipeline to transport.
    */
    public NextAction processRequest(Packet request) {
        WSDLPort port = getEndpoint().getPort();
        WSBinding binding = getEndpoint().getBinding();
        T param = argsBuilder.getParameter(request);

        LOGGER.fine("Invoking Provider Endpoint");

        T returnValue;
        try {
            returnValue = getInvoker(request).invokeProvider(request, param);
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Packet response = argsBuilder.getResponse(request,e,port,binding);
            return doReturnWith(response);
        }
        if (returnValue == null) {
            // Oneway. Send response code immediately for transports(like HTTP)
            // Don't do this above, since close() may generate some exceptions
            if (request.transportBackChannel != null) {
                request.transportBackChannel.close();
            }
        }
        Packet response = argsBuilder.getResponse(request,returnValue,port,binding);
        return doReturnWith(response);
    }

    public NextAction processResponse(Packet response) {
        throw new IllegalStateException("InovkerPipe's processResponse shouldn't be called.");
    }

    public NextAction processException(@NotNull Throwable t) {
        throw new IllegalStateException("InovkerPipe's processException shouldn't be called.");
    }

}
