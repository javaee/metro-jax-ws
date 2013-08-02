/*
 * $Id: BaseSOAPHandler.java,v 1.2 2008-03-06 02:38:15 ramapulavarthi Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package handler.handler_processing.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.*;
import javax.xml.ws.handler.soap.*;
import javax.xml.ws.ProtocolException;
import javax.xml.soap.SOAPMessage;

public class BaseSOAPHandler implements SOAPHandler<SOAPMessageContext>,
    TestConstants, HasName {
    
    HandlerTracker tracker;
    String name;
    HandlerUtil util;
    boolean available;
    
    public BaseSOAPHandler() {
        available = false;
    }
    
    // used when adding a handler programatically
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean handleMessage(SOAPMessageContext messageContext) {
        if (HandlerTracker.VERBOSE_HANDLERS) {
            System.out.println("handler " + name + " (action: " +
                tracker.getHandlerAction(name) + ")");
        }
        switch (tracker.getHandlerAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_RETURN_FALSE_OUTBOUND :
                return util.returnFalseOutbound(messageContext, name);
            case HA_RETURN_FALSE_CHANGE_MESSAGE :
                util.changeRequestToResponse(messageContext, name);
                return false;
            case HA_CHECK_MC_PROPS :
                return util.checkMessageContextProps(messageContext);
            case HA_CHECK_SMC :
                return util.checkSOAPMessageContext(messageContext, name);
            case HA_CHECK_SMC_ALL_ROLES :
                return util.checkSOAPMessageContextAllRoles(
                    messageContext, name);
            case HA_CHECK_LMC :
                throw new UnsupportedOperationException(name +
                    " can't check logical message context");
            case HA_ADD_ONE :
                return util.addIntToSOAPMessage(messageContext, 1);
            case HA_REGISTER_HANDLE_XYZ :
                return util.registerHandlerCalled(messageContext, name);
            case HA_THROW_RUNTIME_EXCEPTION_OUTBOUND:
                return util.throwRuntimeException(
                    messageContext, name, OUTBOUND);
            case HA_THROW_RUNTIME_EXCEPTION_INBOUND:
                return util.throwRuntimeException(
                    messageContext, name, INBOUND);
            case HA_ADD_HEADER_OUTBOUND :
                return util.addHeaderToMessage(messageContext, OUTBOUND, null);
            case HA_ADD_MIMEHEADER_OUTBOUND:
                return util.addMimeHeaderToMessage(messageContext, OUTBOUND);
            case HA_CHECK_MIMEHEADER_INBOUND:
                return util.checkMimeHeaderInMessage(messageContext, INBOUND);
            case HA_CHECK_FOR_ADDED_HEADER_OUTBOUND :
                return util.checkForAddedHeader(messageContext, OUTBOUND);
            case HA_CHECK_FOR_ADDED_HEADER_INBOUND :
                return util.checkForAddedHeader(messageContext, INBOUND);
            case HA_THROW_PROTOCOL_EXCEPTION_OUTBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, OUTBOUND);
            case HA_THROW_SOAP_FAULT_EXCEPTION_OUTBOUND :
                return util.throwSOAPFaultException(messageContext,
                    name, OUTBOUND);
            case HA_THROW_PROTOCOL_EXCEPTION_INBOUND :
                return util.throwSimpleProtocolException(messageContext,
                    name, INBOUND);
            case HA_THROW_SOAP_FAULT_EXCEPTION_INBOUND :
                return util.throwSOAPFaultException(messageContext,
                    name, INBOUND);
            case HA_CHECK_FOR_USER_PROPERTY_OUTBOUND :
                return util.checkUserProperty(messageContext, name, OUTBOUND);
            case HA_ADD_USER_PROPERTY_INBOUND:
                return util.addUserProperty(messageContext, name, INBOUND);
            case HA_ADD_GOOD_MU_HEADER_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, true, null);
            case HA_ADD_BAD_MU_HEADER_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, false, null);
            case HA_ADD_BAD_MU_HEADER_CLIENT2_OUTBOUND :
                return util.addMustUndertandHeader(messageContext,
                    name, OUTBOUND, false, "http://sun.com/client/role2");
            case HA_ADD_HEADER_OUTBOUND_CLIENT_ROLE1 :
                // add one header with actor and one without
                return ( util.addHeaderToMessage(messageContext,
                    OUTBOUND, "http://sun.com/client/role1") &&
                    util.addHeaderToMessage(messageContext,
                    OUTBOUND, null) );
            case HA_INSERT_FAULT_AND_THROW_PE_INBOUND :
                return util.insertFaultMessageAndThrowPE(
                    messageContext, name, INBOUND);
            case HA_INSERT_FAULT_AND_THROW_PE_OUTBOUND :
                return util.insertFaultMessageAndThrowPE(
                    messageContext, name, OUTBOUND);
            case HA_ADD_USER_PROPERTY_OUTBOUND :
                return util.addUserProperty(messageContext, name, OUTBOUND);
	    case HA_CHECK_MC_BAD_PROPS :
		return util.addBadPropertyTypes(messageContext);
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandlerAction(name));
    }
    
    @PostConstruct
    public void initTheHandler() {
        if (name.startsWith(CLIENT_PREFIX)) {
            tracker = HandlerTracker.getClientInstance();
        } else if (name.startsWith(SERVER_PREFIX)) {
            tracker = HandlerTracker.getServerInstance();
        } else {
            throw new RuntimeException("unrecognized prefix in name: " + name);
        }
        tracker.registerHandler(name);
        util = new HandlerUtil(name);
        available = true;
    }
    
    public Set<QName> getHeaders() {
        Set<QName> headers = new HashSet<QName>();
        headers.add(new QName("http://example.com/someheader", "testheader1"));
        return headers;
    }
    
    public void close(MessageContext messageContext) {
        tracker.registerClose(name);
    }
    
    @PreDestroy
    public void destroyHandler() {
        if (tracker != null) {
            tracker.registerDestroy(name);
        }
        available = false;
    }
    
    // always register that handleFault was called and then check action
    public boolean handleFault(SOAPMessageContext messageContext) {
        tracker.registerCalledHandler(name + "_FAULT");
        switch (tracker.getHandleFaultAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_REGISTER_HANDLE_XYZ : // already registered
                return true;
            case HF_THROW_RUNTIME_EXCEPTION :
                throw new RuntimeException(name +
                    " throwing RuntimeException from handleFault");
            case HF_THROW_PROTOCOL_EXCEPTION :
                throw new ProtocolException(name +
                    " throwing ProtocolException from handleFault");
            case HF_RETURN_FALSE:
                return false;
            case HF_CHECK_FAULT_MESSAGE_STRING :
                return util.checkFaultMessageString(messageContext);
            case HF_GET_FAULT_IN_MESSAGE :
                return util.getFaultInMessage(messageContext);
            case HF_THROW_TEST_PROTOCOL_EXCEPTION :
                throw new TestProtocolException(name +
                    " throwing TestProtocolException from handleFault");
        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandleFaultAction(name));
    }
    
    public boolean isAvailable() {
        return available;
    }
    
}
