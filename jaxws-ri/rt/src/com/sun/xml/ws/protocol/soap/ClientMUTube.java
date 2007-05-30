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
