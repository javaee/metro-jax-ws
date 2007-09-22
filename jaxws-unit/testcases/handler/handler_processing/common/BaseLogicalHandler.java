/**
 * $Id: BaseLogicalHandler.java,v 1.1 2007-09-22 00:39:24 ramapulavarthi Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.common;

import java.io.StringWriter;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;

public class BaseLogicalHandler implements
    LogicalHandler<LogicalMessageContext>, TestConstants, HasName {
    
    HandlerTracker tracker;
    String name;
    HandlerUtil util;
    
    public boolean handleMessage(LogicalMessageContext messageContext) {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("handler " + name + " (action: " +
                tracker.getHandlerAction(name) + ")");
        }
        switch (tracker.getHandlerAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_RETURN_FALSE_INBOUND :
                return util.returnFalseInbound(messageContext, name);
            case HA_RETURN_FALSE_OUTBOUND :
                return util.returnFalseOutbound(messageContext, name);
            case HA_RETURN_FALSE_CHANGE_MESSAGE :
                util.changeRequestToResponse(messageContext, name);
                return false;
            case HA_CHECK_MC_PROPS :
                return util.checkMessageContextProps(messageContext);
            case HA_CHECK_SMC :
                throw new UnsupportedOperationException(name +
                    " can't check soap message context");
            case HA_CHECK_LMC :
                return util.checkLogicalMessageContext(messageContext);
            case HA_ADD_ONE :
                return util.addIntToLogicalMessage(messageContext, 1);
            case HA_REGISTER_HANDLE_XYZ :
                return util.registerHandlerCalled(messageContext, name);
            case HA_THROW_RUNTIME_EXCEPTION_OUTBOUND :
                return util.throwRuntimeException(
                    messageContext, name, OUTBOUND);
            case HA_THROW_RUNTIME_EXCEPTION_INBOUND :
                return util.throwRuntimeException(
                    messageContext, name, INBOUND);
            case HA_ADD_HEADER_OUTBOUND :
                throw new UnsupportedOperationException(name +
                    " can't check soap message context");
            case HA_CHECK_FOR_ADDED_HEADER_OUTBOUND :
                throw new UnsupportedOperationException(name +
                    " can't check soap message context");
            case HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, OUTBOUND);
            case HA_THROW_PROTOCOL_EXCEPTION_INBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, INBOUND);
            case HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND :
                throw new UnsupportedOperationException(name +
                    " shouldn't throw SOAP exceptions");
            case HA_ADD_AND_CHECK_PROPS_INBOUND :
                return util.checkAndAddUserProps((MessageContext) messageContext,
                    name, INBOUND, USER_CLIENT_PROPERTY_NAME + "last",
                    USER_PROPERTY_HANDLER_SET + "last");
	    case HA_CHECK_MC_BAD_PROPS :
		return util.addBadPropertyTypes(messageContext);
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandlerAction(name));
    }
    
    @PostConstruct
    public void init() {
        if (name == null || name.startsWith(CLIENT_PREFIX)) {
            tracker = HandlerTracker.getClientInstance();
        } else if (name.startsWith(SERVER_PREFIX)) {
            tracker = HandlerTracker.getServerInstance();
        } else {
            throw new RuntimeException("unrecognized prefix in name: " + name);
        }
        
        tracker.registerHandler(name);
        util = new HandlerUtil(name);
    }
    
    public String getName() {
        return name;
    }
    
    public void close(MessageContext messageContext) {
        tracker.registerClose(name);
    }
    
    @PreDestroy
    public void destroy() {
        tracker.registerDestroy(name);
    }
    
    // always register that handleFault was called and then check action
    public boolean handleFault(LogicalMessageContext messageContext) {
        tracker.registerCalledHandler(name + "_FAULT");
        switch (tracker.getHandleFaultAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_REGISTER_HANDLE_XYZ : // already registered
                return true;
            case HF_RETURN_FALSE :
                return false;
            case HF_GET_FAULT_IN_MESSAGE:
                return util.getFaultInMessage(messageContext);
            case HF_THROW_PROTOCOL_EXCEPTION :
                throw new ProtocolException(name +
                    " throwing ProtocolException from handleFault");
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandleFaultAction(name));
    }
    
}
