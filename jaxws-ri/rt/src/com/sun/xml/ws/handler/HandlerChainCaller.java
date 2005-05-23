/**
 * $Id: HandlerChainCaller.java,v 1.1 2005-05-23 22:37:25 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.handler;

import javax.xml.namespace.QName;

import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.*;
import javax.xml.ws.handler.soap.*;

import javax.xml.soap.Detail;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * Used in BindingImpl to handler calls to the hander chain. This is
 * a replacement in 2.0 for the HandlerChainImpl class.
 *
 * Exceptions are logged in many cases here before being rethrown. This
 * is to help primarily with server side handlers.
 *
 * @author JAX-WS RI Development Team
 */
public class HandlerChainCaller {

    public static final String HANDLER_CHAIN_CALLER = "handler_chain_caller";
    public static final String IGNORE_FAULT_PROPERTY =
        "ignore msg fault, use exception";

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".handler");

    // need request or response for Handle interface
    public enum RequestOrResponse { REQUEST, RESPONSE }
    public enum Direction { OUTBOUND, INBOUND }

    private Set<QName> understoodHeaders;
    private List<Handler> handlers; // may be logical/soap mixed
    
    private List<LogicalHandler> logicalHandlers;
    private List<SOAPHandler> soapHandlers;
    
    private Set<String> roles; // todo: change to Set<URI> ?

    /*
     * The handlers that are passed in will be sorted into
     * logical and soap handlers. The handler chain caller cannot
     * sort them into service, port, or protocol lists. The list
     * must already be ordered appropriately when passed in.
     */
    public HandlerChainCaller(List<Handler> chain) {
        if (chain == null) { // should only happen in testing
            chain = new ArrayList<Handler>();
        }
        handlers = chain;
        logicalHandlers = new ArrayList<LogicalHandler>();
        soapHandlers = new ArrayList<SOAPHandler>();
        understoodHeaders = new HashSet<QName>();
        sortHandlers();
    }

    public List<Handler> getHandlerChain() {
        return handlers;
    }

    public boolean hasHandlers() {
        return (handlers.size() != 0);
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<QName> getUnderstoodHeaders() {
        return understoodHeaders;
    }

    private void sortHandlers() {
        for (Handler handler : handlers) {
            if (LogicalHandler.class.isAssignableFrom(handler.getClass())) {
                logicalHandlers.add((LogicalHandler) handler);
            } else if (SOAPHandler.class.isAssignableFrom(handler.getClass())) {
                soapHandlers.add((SOAPHandler) handler);
            } else if (Handler.class.isAssignableFrom(handler.getClass())) {
                throw new HandlerException(
                    "cannot.extend.handler.directly",
                    handler.getClass().toString());
            } else {
                throw new HandlerException("handler.not.valid.type",
                    handler.getClass().toString());
            }
        }
        handlers.clear();
        handlers.addAll(logicalHandlers);
        handlers.addAll(soapHandlers);
    }

    /*
     * Called before losing instance of this handler chain caller.
     */
    public void cleanup() {
        for (Handler handler : handlers) {
            handler.destroy();
        }
    }

    /*
     * Replace the message in the given message context with a
     * fault message. If the context already contains a fault
     * message, then return without changing it.
     */
    private void insertFaultMessage(ContextHolder holder,
        ProtocolException exception) {

        try {
            SOAPMessageContext context = holder.getSMC();
            SOAPMessage message = context.getMessage();
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            SOAPBody body = envelope.getBody();
            if (body.hasFault()) {
                return;
            }
            if (envelope.getHeader() != null) {
                envelope.getHeader().detachNode();
            }

            body.removeContents();
            SOAPFault fault = body.addFault();
/* no more sfe
 *
 * todo: remove after checking new spec

            // try to handle any nulls gracefully
            if (exception instanceof SOAPFaultException) {
                SOAPFaultException sfe = (SOAPFaultException) exception;
                if (sfe.getFaultCode() != null) {
                    fault.setFaultCode(envelope.createName(
                        sfe.getFaultCode().getLocalPart(),
                        sfe.getFaultCode().getPrefix(),
                        sfe.getFaultCode().getNamespaceURI()));
                } else {
                    fault.setFaultCode(envelope.createName("Server",
                        "env", "http://schemas.xmlsoap.org/soap/envelope/"));
                }
                if (sfe.getFaultString() != null) {
                    fault.setFaultString(sfe.getFaultString());
                } else {
                    if (sfe.getMessage() != null) {
                        fault.setFaultString(sfe.getMessage());
                    } else {
                        fault.setFaultString(sfe.toString());
                    }
                }
                if (sfe.getFaultActor() != null) {
                    fault.setFaultActor(sfe.getFaultActor());
                } else {
                    fault.setFaultActor("");
                }
                if (sfe.getDetail() != null) {
                    fault.addChildElement(sfe.getDetail());
                }
            } else if (exception.getMessage() != null) {
 */
            if (exception.getMessage() != null) {
                fault.setFaultCode(envelope.createName("Server",
                    "env", "http://schemas.xmlsoap.org/soap/envelope/"));
                if (exception.getMessage() != null) {
                    fault.setFaultString(exception.getMessage());
                } else {
                    fault.setFaultString(exception.toString());
                }
            }
        } catch (Exception e) {
            logger.log(Level.FINER,
                "exception while creating fault message", e);
            throw new RuntimeException(e);
        }
    }

    /*
     * This method is used to set a property so that the runtime
     * knows to ignore a fault in a message. It is used when there
     * is a new exception thrown during handle fault processing.
     */
    private void setIgnoreFaultProperty(ContextHolder holder) {
       holder.getSMC().put(IGNORE_FAULT_PROPERTY, Boolean.TRUE);
       holder.getSMC().setScope(IGNORE_FAULT_PROPERTY,
           MessageContext.Scope.APPLICATION);
    }

    /*
     * The boolean passed in is whether or not a response is required
     * for the current message. See section 5.3.2. (todo: this section
     * is going to change). The RequestOrResponse
     * value is so handleRequest or handleResponse can be called on
     * handlers that implement Handler.
     *
     * The callLogicalHandlers and callProtocolHandlers methods will
     * take care of execution once called and return true or false or
     * throw an exception.
     */
    public boolean callHandlers(Direction direction,
        RequestOrResponse messageType,
        HandlerContext context,
        boolean responseExpected) {

        ContextHolder ch = new ContextHolder(context);
        ch.getSMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY,
            (direction == Direction.OUTBOUND));
        ((SOAPMessageContextImpl) ch.getSMC()).setRoles(roles);

        // call handlers
        if (direction == Direction.OUTBOUND) {
            if (callLogicalHandlers(ch, direction, messageType,
                    responseExpected) == false) {
                return false;
            }
            if (callProtocolHandlers(ch, direction, messageType,
                    responseExpected) == false) {
                return false;
            }
        } else {
            if (callProtocolHandlers(ch, direction, messageType,
                    responseExpected) == false) {
                return false;
            }
            if (callLogicalHandlers(ch, direction, messageType,
                    responseExpected) == false) {
                return false;
            }
        }

        /*
         * Close if MEP finished. Server code responsible for closing
         * handlers if it determines that an incoming request is a
         * one way message.
         */
        if (!responseExpected) {
            closeHandlers(ch);
        }
        return true;
    }

    /*
     * This method called by the server when an endpoint has thrown
     * an exception. This method calls handleFault on the handlers
     * and closes them. Because this method is called only during
     * a response after the endpoint has been reached, all of the
     * handlers have been called during the request and so all are
     * closed.
     */
    public boolean callHandleFault(HandlerContext context) {
        ContextHolder ch = new ContextHolder(context);
        ch.getSMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
        ((SOAPMessageContextImpl) ch.getSMC()).setRoles(roles);

        int i = 0; // counter for logical handlers
        int j = 0; // counter for protocol handlers
        try {
            while (i < logicalHandlers.size()) {
                if (logicalHandlers.get(i).handleFault(ch.getLMC()) == false) {
                    return false;
                }
                i++;
            }
            while (j < soapHandlers.size()) {
                if (soapHandlers.get(j).handleFault(
                    ch.getSMC()) == false) {
                    return false;
                }
                j++;
            }
        } catch (RuntimeException re) {
            logger.log(Level.FINER,
                "exception in handler chain", re);
            throw re;
        } finally {
            closeHandlers(ch);
        }
        return true;
    }

    /*
     * Called from the main callHandlers() method.
     * Logical message context updated before this method is called.
     */
    private boolean callLogicalHandlers(ContextHolder holder,
        Direction direction, RequestOrResponse type, boolean responseExpected) {

        if (direction == Direction.OUTBOUND) {
            int i = 0;
            try {
                while (i < logicalHandlers.size()) {
                    if (logicalHandlers.get(i).
                        handleMessage(holder.getLMC()) == false) {

                        if (responseExpected) {
                            // reverse and call handle message
                            callLogicalHandleMessage(holder, i-1, 0);
                        }
                        if (type == RequestOrResponse.RESPONSE) {
                            closeHandlers(holder);
                        } else {
                            closeLogicalHandlers(holder, i, 0);
                        }
                        return false;
                    }
                    i++;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    // reverse direction and handle fault
                    insertFaultMessage(holder, (ProtocolException) re);
                    if (i>0) {
                        try {
                            callLogicalHandleFault(holder, i-1, 0);
                        } catch (RuntimeException re2) {
                            re = re2;
                            setIgnoreFaultProperty(holder);
                        }
                    }
                }
                if (type == RequestOrResponse.RESPONSE) {
                    closeHandlers(holder);
                } else {
                    closeLogicalHandlers(holder, i, 0);
                }
                throw re;
            }
        } else { // inbound case, H(x) -> H(x-1) -> ... H(1) -> H(0)
            int i = logicalHandlers.size()-1;
            try {
                while (i >= 0) {
                    if (logicalHandlers.get(i).
                        handleMessage(holder.getLMC()) == false) {

                        if (responseExpected) {
                            // reverse and call handle message/response
                            callLogicalHandleMessage(holder, i+1,
                                logicalHandlers.size()-1);
                            callProtocolHandleMessage(holder, 0,
                                soapHandlers.size()-1);
                        }
                        if (type == RequestOrResponse.RESPONSE) {
                            closeHandlers(holder);
                        } else {
                            closeProtocolHandlers(holder,
                                soapHandlers.size()-1, 0);
                            closeLogicalHandlers(holder,
                                logicalHandlers.size()-1, i);
                        }
                        return false;
                    }
                    i--;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    // reverse direction and handle fault
                    insertFaultMessage(holder,
                        (ProtocolException) re);
                    try {
                        if (callLogicalHandleFault(holder, i+1,
                                logicalHandlers.size()-1)) {
                            callProtocolHandleFault(holder, 0,
                                soapHandlers.size()-1);
                        }
                    } catch (RuntimeException re2) {
                        re = re2;
                        setIgnoreFaultProperty(holder);
                    }
                }
                if (type == RequestOrResponse.RESPONSE) {
                    closeHandlers(holder);
                } else {
                    closeProtocolHandlers(holder,
                        soapHandlers.size()-1, 0);
                    closeLogicalHandlers(holder, logicalHandlers.size()-1, i);
                }
                throw re;
            }
        }

        return true;
    }

    /*
     * Called from the main callHandlers() method.
     * SOAP message context updated before this method is called.
     */
    private boolean callProtocolHandlers(ContextHolder holder,
        Direction direction, RequestOrResponse type, boolean responseExpected) {

        if (direction == Direction.OUTBOUND) {
            int i = 0;
            try {
                while (i<soapHandlers.size()) {
                    if (soapHandlers.get(i).
                        handleMessage(holder.getSMC()) == false) {

                        if (responseExpected) {
                            // reverse and call handle message/response
                            callProtocolHandleMessage(holder,
                                i, 0);
                            callLogicalHandleMessage(holder,
                                logicalHandlers.size()-1, 0);
                        }
                        if (type == RequestOrResponse.RESPONSE) {
                            closeHandlers(holder);
                        } else {
                            closeProtocolHandlers(holder, i, 0);
                            closeLogicalHandlers(holder,
                                logicalHandlers.size()-1 , 0);
                        }
                        return false;
                    }
                    i++;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    // reverse direction and handle fault
                    insertFaultMessage(holder, (ProtocolException) re);
                    try {
                        if (callProtocolHandleFault(holder, i, 0)) {
                            callLogicalHandleFault(holder,
                                logicalHandlers.size()-1, 0);
                        }
                    } catch (RuntimeException re2) {
                        re = re2;
                        setIgnoreFaultProperty(holder);
                    }
                }
                if (type == RequestOrResponse.RESPONSE) {
                    closeHandlers(holder);
                } else {
                    closeProtocolHandlers(holder, i, 0);
                    closeLogicalHandlers(holder, logicalHandlers.size()-1, 0);
                }
                throw re;
            }
        } else { // inbound case, H(x) -> H(x-1) -> ... H(1) -> H(0)
            int i = soapHandlers.size()-1;
            try {
                while (i >= 0) {
                    if (soapHandlers.get(i).
                        handleMessage(holder.getSMC()) == false) {

                        if (responseExpected) {
                            // reverse and call handle message/response
                            callProtocolHandleMessage(holder, i,
                                soapHandlers.size()-1);
                        }
                        if (type == RequestOrResponse.RESPONSE) {
                            closeHandlers(holder);
                        } else {
                            closeProtocolHandlers(holder,
                                soapHandlers.size()-1, i);
                        }
                        return false;
                    }
                    i--;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    // reverse direction and handle fault
                    insertFaultMessage(holder, (ProtocolException) re);
                    try {
                        callProtocolHandleFault(holder, i,
                            soapHandlers.size()-1);
                    } catch (RuntimeException re2) {
                        re = re2;
                        setIgnoreFaultProperty(holder);
                    }
                }
                if (type == RequestOrResponse.RESPONSE) {
                    closeHandlers(holder);
                } else {
                    closeProtocolHandlers(holder,
                        soapHandlers.size()-1, i);
                }
                throw re;
            }
        }
        return true;
    }

    /*
     * Method called for abnormal processing (for instance, as the
     * result of a handler returning false during normal processing).
     * Start and end indices are inclusive.
     */
    private void callLogicalHandleMessage(ContextHolder holder,
            int start, int end) {

        if (logicalHandlers.isEmpty() ||
            start == -1 ||
            start == logicalHandlers.size()) {
            return;
        }
        if (start > end) {
            for (int i=start; i>=end; i--) {
                abstractHandle(logicalHandlers.get(i), holder.getLMC());
            }
        } else {
            for (int i=start; i<=end; i++) {
                abstractHandle(logicalHandlers.get(i), holder.getLMC());
            }
        }
    }

    /*
     * Method called for abnormal processing (for instance, as the
     * result of a handler returning false during normal processing).
     * Start and end indices are inclusive.
     */
    private void callProtocolHandleMessage(ContextHolder holder,
        int start, int end) {

        if (soapHandlers.isEmpty()) {
            return;
        }

        if (start > end) {
            for (int i=start; i>=end; i--) {
                abstractHandle(soapHandlers.get(i), holder.getSMC());
            }
        } else {
            for (int i=start; i<=end; i++) {
                abstractHandle(soapHandlers.get(i), holder.getSMC());
            }
        }
    }

    /*
     * Utility method for calling handleMessage and ignoring
     * the result.
     */
    private void abstractHandle(Handler h, MessageContext c) {
        try {
            h.handleMessage(c);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception ignored during handleMessage", e);
        }
    }

    /*
     * Calls handleFault on the logical handlers. Indices are
     * inclusive. Exceptions get passed up the chain, and an
     * exception or return of 'false' ends processing.
     */
    private boolean callLogicalHandleFault(ContextHolder holder,
            int start, int end) {
        
        return callGenericHandleFault(logicalHandlers,
            holder.getLMC(), start, end);
    }

    /*
     * Calls handleFault on the protocol handlers. Indices are
     * inclusive. Exceptions get passed up the chain, and an
     * exception or return of 'false' ends processing.
     */
    private boolean callProtocolHandleFault(ContextHolder holder,
        int start, int end) {

        return callGenericHandleFault(soapHandlers,
            holder.getSMC(), start, end);
    }

    /*
     * Used by callLogicalHandleFault and callProtocolHandleFault.
     */
    private boolean callGenericHandleFault(List<? extends Handler> handlerList,
        MessageContext context, int start, int end) {

        if (handlerList.isEmpty()) {
            return true;
        }
        int i = start;
        if (start > end) {
            try {
                while (i >= end) {
                    if (handlerList.get(i).
                            handleFault(context) == false) {

                        return false;
                    }
                    i--;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                throw re;
            }
        } else {
            try {
                while (i <= end) {
                    if (handlerList.get(i).
                        handleFault(context) == false) {

                        return false;
                    }
                    i++;
                }
            } catch (RuntimeException re) {
                logger.log(Level.FINER,
                    "exception in handler chain", re);
                throw re;
            }
        }
        return true;
    }

    // util method. change order if spec changes
    private void closeHandlers(ContextHolder holder) {
        closeProtocolHandlers(holder, soapHandlers.size()-1, 0);
        closeLogicalHandlers(holder, logicalHandlers.size()-1, 0);
    }

    /*
     * This version is called by the server code once it determines
     * that an incoming message is a one-way request. Or it is called
     * by the client when an MU fault occurs since the handler chain
     * never gets invoked. Either way, the direction is an inbound message.
     */
    public void forceCloseHandlers(HandlerContext context) {
        ContextHolder ch = new ContextHolder(context);

        // only called after an inbound request
        ch.getSMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
        ((SOAPMessageContextImpl) ch.getSMC()).setRoles(roles);
        closeHandlers(ch);
    }

    private void closeProtocolHandlers(ContextHolder holder,
        int start, int end) {
        
        closeGenericHandlers(soapHandlers, holder.getSMC(), start, end);
    }

    private void closeLogicalHandlers(ContextHolder holder,
        int start, int end) {
        
        closeGenericHandlers(logicalHandlers, holder.getLMC(), start, end);
    }
    
    /*
     * Calls close on the handlers from the starting
     * index through the ending index (inclusive). Made indices
     * inclusive to allow both directions more easily.
     */
    private void closeGenericHandlers(List<? extends Handler> handlerList,
        MessageContext context, int start, int end) {
        
        if (handlerList.isEmpty()) {
            return;
        }
        if (start > end) {
            for (int i=start; i>=end; i--) {
                try {
                    handlerList.get(i).close(context);
                } catch (RuntimeException re) {
                    logger.log(Level.INFO,
                        "Exception ignored during close", re);
                }
            }
        } else {
            for (int i=start; i<=end; i++) {
                try {
                    handlerList.get(i).close(context);
                } catch (RuntimeException re) {
                    logger.log(Level.INFO,
                        "Exception ignored during close", re);
                }
            }
        }
    }

    /**
     * Used to hold the context objects that get passed around
     * so that the caller class doesn't have instance variables
     * (which will get clobbered by different threads).
     */
    static class ContextHolder {

        HandlerContext context;

        ContextHolder(HandlerContext context) {
            this.context = context;
        }

        LogicalMessageContext getLMC() {
            return context.createLogicalMessageContext();
        }

        SOAPMessageContext getSMC() {
            return context.createSOAPMessageContext();
        }
    }
}
