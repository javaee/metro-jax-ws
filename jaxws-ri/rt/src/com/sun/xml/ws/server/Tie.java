/**
 * $Id: Tie.java,v 1.7 2005-08-05 01:03:31 jitu Exp $
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

/**
 * Entry point for all server requests.
 *
 * @author WS Development Team
 */
public class Tie implements com.sun.xml.ws.spi.runtime.Tie {
    
    /**
     * Common entry point for server runtime. <br>
     * Creates a MessageInfo for every Request/Response.<br>
     * Creates a RuntimeContext for every Request/Response and sets that as a metadata in 
     * MessageInfo. Doesn't create any other metadata on MessageInfo. If anything is needed, 
     * that can be created on RuntimeContext<br>
     * EPTFactoryFactoryBase is used to select a correct EPTFactory<br>
     * Calls MessageDispatcher.receive(MessageInfo). <br>
     * MessageDispatcher orchestrates all the flow: reading from WSConnection, 
     * decodes message to parameters, invoking implementor, encodes parameters to message, 
     * and writing to WSConnection
     *
     * @param connection encapsulates multiple transports
     * @param endpoint has all the information about target endpoint
     * @throws Exception throws Exception if any error occurs
     */
    public void handle(WSConnection connection,
        com.sun.xml.ws.spi.runtime.RuntimeEndpointInfo endpoint)
    throws Exception {
        
        // Create MessageInfo. MessageInfo holds all the info for this request
        Delegate delegate = new DelegateBase();
        MessageInfo messageInfo = (MessageInfo)delegate.getMessageStruct();
        
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
        
        // MessageDispatcher archestrates the flow
        MessageDispatcher messageDispatcher =
            messageInfo.getEPTFactory().getMessageDispatcher(messageInfo);
        messageDispatcher.receive(messageInfo);
    }
    
}
