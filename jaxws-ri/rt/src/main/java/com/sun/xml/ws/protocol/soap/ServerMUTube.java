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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.protocol.soap;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.binding.BindingImpl;
import javax.xml.namespace.QName;
import java.util.Set;

/**
 * @author Rama Pulavarthi
 */

public class ServerMUTube extends MUTube {
    
    private ServerTubeAssemblerContext tubeContext;
    private final Set<String> roles;
    private final Set<QName> handlerKnownHeaders;

    public ServerMUTube(ServerTubeAssemblerContext tubeContext, Tube next) {
        super(tubeContext.getEndpoint().getBinding(), next);

        this.tubeContext = tubeContext;

        //On Server, HandlerConfiguration does n't change after publish, so store locally
        HandlerConfiguration handlerConfig = binding.getHandlerConfig();
        roles = handlerConfig.getRoles();
        handlerKnownHeaders = handlerConfig.getHandlerKnownHeaders();
    }

    protected ServerMUTube(ServerMUTube that, TubeCloner cloner) {
        super(that,cloner);
        tubeContext = that.tubeContext;
        roles = that.roles;
        handlerKnownHeaders = that.handlerKnownHeaders;
    }

    /**
     * Do MU Header Processing on incoming message (request)
     * @return
     *      if all the headers in the packet are understood, returns action such that
     *      next pipe will be inovked.
     *      if all the headers in the packet are not understood, returns action such that
     *      SOAPFault Message is sent to previous pipes.
     */
    @Override
    public NextAction processRequest(Packet request) {
        Set<QName> misUnderstoodHeaders = getMisUnderstoodHeaders(request.getMessage().getMessageHeaders(),roles, handlerKnownHeaders);
        if((misUnderstoodHeaders == null)  || misUnderstoodHeaders.isEmpty()) {
            return doInvoke(super.next, request);
        }
        return doReturnWith(request.createServerResponse(createMUSOAPFaultMessage(misUnderstoodHeaders),
                tubeContext.getWsdlModel(), tubeContext.getSEIModel(), tubeContext.getEndpoint().getBinding()));
    }

    public ServerMUTube copy(TubeCloner cloner) {
        return new ServerMUTube(this,cloner);
    }

}
