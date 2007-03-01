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
package com.sun.xml.ws.protocol.soap;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.client.HandlerConfiguration;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.HashSet;
import java.util.Set;

/**
 * Performs soap mustUnderstand processing for clients.
 *
 * @author Rama Pulavarthi
 */
public class ClientMUTube extends MUTube {

    public ClientMUTube(WSBinding binding, Tube next) {
        super(binding, next);
    }

    protected ClientMUTube(ClientMUTube that, TubeCloner cloner) {
        super(that,cloner);
    }

    /**
     * Do MU Header Processing on incoming message (response)
     *
     * @return
     *         if all the headers in the packet are understood, returns an action to
     *         call the previous pipes with response packet
     * @throws SOAPFaultException
     *         if all the headers in the packet are not understood, throws SOAPFaultException
     */
    @Override @NotNull
    public NextAction processResponse(Packet response) {
        if (response.getMessage() == null) {
            return super.processResponse(response);
        }
        HandlerConfiguration handlerConfig = response.handlerConfig;
        Set<QName> knownHeaders;
        Set<String> roles;
        if (handlerConfig != null) {
            knownHeaders = handlerConfig.getKnownHeaders();
            roles = handlerConfig.getRoles();
        } else {
            roles = soapVersion.implicitRoleSet;
            knownHeaders = new HashSet<QName>();
        }
        Set<QName> misUnderstoodHeaders = getMisUnderstoodHeaders(
                response.getMessage().getHeaders(), roles,
                knownHeaders);
        if((misUnderstoodHeaders == null) || misUnderstoodHeaders.isEmpty()) {
            return super.processResponse(response);
        }
        throw createMUSOAPFaultException(misUnderstoodHeaders);
    }

    public ClientMUTube copy(TubeCloner cloner) {
        return new ClientMUTube(this,cloner);
    }

}
