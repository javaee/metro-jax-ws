/**
 * $Id: SOAPTestHandler.java,v 1.1 2007-09-21 22:43:57 ramapulavarthi Exp $
 *
 * Copyright 2005 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package handler.single_handlertube.common;
import static handler.single_handlertube.common.TestConstants.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Rama Pulavarthi
 */
public class SOAPTestHandler implements SOAPHandler<SOAPMessageContext> {
    HandlerTracker tracker;
    String name;
    HandlerUtil util;

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
        tracker.registerCalledHandler(name);
        switch (tracker.getHandlerAction(name)) {
            case HA_RETURN_TRUE :
                return true;
            case HA_RETURN_FALSE :
                return false;
            case HA_RETURN_FALSE_OUTBOUND :
                return util.returnFalseOutbound(messageContext, name);
            case HA_CHECK_LMC :
                throw new UnsupportedOperationException(name +
                    " can't check logical message context");
            case HA_ADD_ONE :
                return util.addIntToSOAPMessage(messageContext, 1);
            case HA_THROW_RUNTIME_EXCEPTION_OUTBOUND:
                return util.throwRuntimeException(
                    messageContext, name, OUTBOUND);
            case HA_THROW_RUNTIME_EXCEPTION_INBOUND:
                return util.throwRuntimeException(
                    messageContext, name, INBOUND);
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
        util = new HandlerUtil();
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
            case HF_GET_FAULT_IN_MESSAGE :
                return util.getFaultInMessage(messageContext);

        }
        throw new RuntimeException(name + " didn't understand action: " +
            tracker.getHandleFaultAction(name));
    }
}
