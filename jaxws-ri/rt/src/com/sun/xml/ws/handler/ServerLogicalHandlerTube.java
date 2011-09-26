/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.message.DataHandlerAttachment;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.spi.db.BindingContext;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author WS Development Team
 */
public class ServerLogicalHandlerTube extends HandlerTube {

    private SEIModel seiModel;

    /**
     * Creates a new instance of LogicalHandlerTube
     */
    public ServerLogicalHandlerTube(WSBinding binding, SEIModel seiModel, WSDLPort port, Tube next) {
        super(next, port, binding);
        this.seiModel = seiModel;
        setUpHandlersOnce();
    }

    /**
     * This constructor is used on client-side where, SOAPHandlerTube is created
     * first and then a LogicalHandlerTube is created with a handler to that
     * SOAPHandlerTube.
     * With this handle, LogicalHandlerTube can call
     * SOAPHandlerTube.closeHandlers()
     */
    public ServerLogicalHandlerTube(WSBinding binding, SEIModel seiModel, Tube next, HandlerTube cousinTube) {
        super(next, cousinTube, binding);
        this.seiModel = seiModel;
        setUpHandlersOnce();
    }

    /**
     * Copy constructor for {@link com.sun.xml.ws.api.pipe.Tube#copy(com.sun.xml.ws.api.pipe.TubeCloner)}.
     */

    private ServerLogicalHandlerTube(ServerLogicalHandlerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.seiModel = that.seiModel;
        this.handlers = that.handlers;
    }

    //should be overridden by DriverHandlerTubes
    @Override
    protected void initiateClosing(MessageContext mc) {
         if (getBinding().getSOAPVersion() != null) {
            super.initiateClosing(mc);
        } else {
            close(mc);
            super.initiateClosing(mc); 
        }
    }

   public AbstractFilterTubeImpl copy(TubeCloner cloner) {
        return new ServerLogicalHandlerTube(this, cloner);
    }

    private void setUpHandlersOnce() {
        handlers = new ArrayList<Handler>();
        List<LogicalHandler> logicalSnapShot= ((BindingImpl) getBinding()).getHandlerConfig().getLogicalHandlers();
        if (!logicalSnapShot.isEmpty()) {
            handlers.addAll(logicalSnapShot);
        }
    }

    protected void resetProcessor() {
    	processor = null;
    }
    
    void setUpProcessor() {
        if (!handlers.isEmpty() && processor == null) {
            if (getBinding().getSOAPVersion() == null) {
                processor = new XMLHandlerProcessor(this, getBinding(),
                        handlers);
            } else {
                processor = new SOAPHandlerProcessor(false, this, getBinding(), handlers);
            }
        }
    }

    MessageUpdatableContext getContext(Packet packet) {
        return new LogicalMessageContextImpl(getBinding(), getBindingContext(), packet);
    }   

    private BindingContext getBindingContext() {
        return (seiModel!= null && seiModel instanceof AbstractSEIModelImpl) ?
        	((AbstractSEIModelImpl)seiModel).getBindingContext() : null;
	}

    boolean callHandlersOnRequest(MessageUpdatableContext context, boolean isOneWay) {

        boolean handlerResult;
        try {
            //SERVER-SIDE
            handlerResult = processor.callHandlersRequest(HandlerProcessor.Direction.INBOUND, context, !isOneWay);

        } catch (RuntimeException re) {
            remedyActionTaken = true;
            throw re;
        }
        if (!handlerResult) {
            remedyActionTaken = true;
        }
        return handlerResult;
    }

    void callHandlersOnResponse(MessageUpdatableContext context, boolean handleFault) {
        //Lets copy all the MessageContext.OUTBOUND_ATTACHMENT_PROPERTY to the message
        Map<String, DataHandler> atts = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        AttachmentSet attSet = context.packet.getMessage().getAttachments();
        for(String cid : atts.keySet()){
            Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
            attSet.add(att);
        }

        try {
            //SERVER-SIDE
            processor.callHandlersResponse(HandlerProcessor.Direction.OUTBOUND, context, handleFault);

        } catch (WebServiceException wse) {
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            throw re;
        }
    }

    void closeHandlers(MessageContext mc) {
        closeServersideHandlers(mc);

    }
}
