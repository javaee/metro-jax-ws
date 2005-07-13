/**
 * $Id: Tie.java,v 1.3 2005-07-13 21:21:15 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import javax.xml.ws.handler.MessageContext;

import com.sun.pept.Delegate;
import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.soap.internal.DelegateBase;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.model.RuntimeModel;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.util.MessageInfoUtil;

public class Tie implements com.sun.xml.ws.spi.runtime.Tie {
    
    public void handle(WSConnection connection,
        com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo endpoint)
    throws Exception {
                
        Delegate delegate = new DelegateBase();
        MessageInfo messageInfo = (MessageInfo)delegate.getMessageStruct();
        
        // TODO remove the hack
        MessageContext context = new SOAPMessageContext();
        messageInfo.setMetaData("MESSAGE_CONTEXT", context);
        
        // Create runtime context, runtime model for dynamic runtime
        RuntimeEndpointInfo endpointInfo = (RuntimeEndpointInfo)endpoint;
        RuntimeModel runtimeModel = endpointInfo.getRuntimeModel();
        RuntimeContext runtimeContext = new RuntimeContext(runtimeModel);
        runtimeContext.setRuntimeEndpointInfo(endpointInfo);
        
        // Set runtime context on MessageInfo
        MessageInfoUtil.setRuntimeContext(messageInfo, runtimeContext);
        messageInfo.setConnection(connection);
        
        // Select EPTFactory based on binding, and transport
        EPTFactory eptFactory = EPTFactoryFactoryBase.getEPTFactory(messageInfo);
        messageInfo.setEPTFactory(eptFactory);
        
        SystemHandlerDelegate systemHandlerDelegate =
            ((com.sun.xml.ws.spi.runtime.Binding) endpointInfo.getBinding()).getSystemHandlerDelegate();
            
        if (systemHandlerDelegate == null) {
            dispatchMessage(messageInfo);
        } else if (systemHandlerDelegate.processRequest(
            (com.sun.xml.ws.spi.runtime.SOAPMessageContext) context)) {
            
            dispatchMessage(messageInfo);
            systemHandlerDelegate.processResponse(
                (com.sun.xml.ws.spi.runtime.SOAPMessageContext) context);
        }
    }

    private void dispatchMessage(MessageInfo messageInfo) throws Exception {
        MessageDispatcher messageDispatcher =
            messageInfo.getEPTFactory().getMessageDispatcher(messageInfo);
        messageDispatcher.receive(messageInfo);
    }
    
}
