/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.message.DataHandlerAttachment;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author WS Development Team
 */
public class ClientSOAPHandlerTube extends HandlerTube {

    private Set<String> roles;

    /**
     * Creates a new instance of SOAPHandlerTube
     */
    public ClientSOAPHandlerTube(WSBinding binding, WSDLPort port, Tube next) {
        super(next, port, binding);
        if (binding.getSOAPVersion() != null) {
            // SOAPHandlerTube should n't be used for bindings other than SOAP.
            // TODO: throw Exception
        }
    }

    // Handle to LogicalHandlerTube means its used on SERVER-SIDE

    /**
     * This constructor is used on client-side where, LogicalHandlerTube is created
     * first and then a SOAPHandlerTube is created with a handler to that
     * LogicalHandlerTube.
     * With this handle, SOAPHandlerTube can call LogicalHandlerTube.closeHandlers()
     */
    public ClientSOAPHandlerTube(WSBinding binding, Tube next, HandlerTube cousinTube) {
        super(next, cousinTube, binding);
    }

    /**
     * Copy constructor for {@link com.sun.xml.ws.api.pipe.Tube#copy(com.sun.xml.ws.api.pipe.TubeCloner)}.
     */
    private ClientSOAPHandlerTube(ClientSOAPHandlerTube that, TubeCloner cloner) {
        super(that, cloner);
    }

   public AbstractFilterTubeImpl copy(TubeCloner cloner) {
        return new ClientSOAPHandlerTube(this, cloner);
    }

    void setUpProcessor() {
    	if (handlers == null) {
	        // Take a snapshot, User may change chain after invocation, Same chain
	        // should be used for the entire MEP
	        handlers = new ArrayList<Handler>();
	        HandlerConfiguration handlerConfig = ((BindingImpl) getBinding()).getHandlerConfig();
	        List<SOAPHandler> soapSnapShot= handlerConfig.getSoapHandlers();
	        if (!soapSnapShot.isEmpty()) {
	            handlers.addAll(soapSnapShot);
	            roles = new HashSet<String>();
	            roles.addAll(handlerConfig.getRoles());
	            processor = new SOAPHandlerProcessor(true, this, getBinding(), handlers);
	        }
    	}
    }

    MessageUpdatableContext getContext(Packet packet) {
        SOAPMessageContextImpl context = new SOAPMessageContextImpl(getBinding(), packet,roles);
        return context;
    }

    boolean callHandlersOnRequest(MessageUpdatableContext context, boolean isOneWay) {

        boolean handlerResult;
        //Lets copy all the MessageContext.OUTBOUND_ATTACHMENT_PROPERTY to the message
        Map<String, DataHandler> atts = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        AttachmentSet attSet = context.packet.getMessage().getAttachments();
        for (Entry<String, DataHandler> entry : atts.entrySet()) {
            String cid = entry.getKey();
            if (attSet.get(cid) == null) {  // Otherwise we would be adding attachments twice
                Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
                attSet.add(att);
            }
        }

        try {
            //CLIENT-SIDE
            handlerResult = processor.callHandlersRequest(HandlerProcessor.Direction.OUTBOUND, context, !isOneWay);
        } catch (WebServiceException wse) {
            remedyActionTaken = true;
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            remedyActionTaken = true;

            throw new WebServiceException(re);

        }
        if (!handlerResult) {
            remedyActionTaken = true;
        }
        return handlerResult;
    }

    void callHandlersOnResponse(MessageUpdatableContext context, boolean handleFault) {
        try {

            //CLIENT-SIDE
            processor.callHandlersResponse(HandlerProcessor.Direction.INBOUND, context, handleFault);

        } catch (WebServiceException wse) {
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            throw new WebServiceException(re);
        }
    }

    void closeHandlers(MessageContext mc) {
        closeClientsideHandlers(mc);

    }
}
