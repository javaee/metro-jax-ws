/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.handler;

import javax.xml.namespace.QName;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.*;
import javax.xml.ws.handler.soap.*;
import javax.xml.ws.soap.SOAPFaultException;

import javax.xml.soap.Detail;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.util.HandlerAnnotationInfo;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * The class stores the actual "chain" of handlers that is called
 * during a request or response. On the client side, it is created
 * by a {@link com.sun.xml.ws.binding.BindingImpl} class when a
 * binding provider is created. On the server side, where a Binding
 * object may be passed from an outside source, the handler chain
 * caller is created by the
 * {@link com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher}
 * or {@link com.sun.xml.ws.protocol.xml.server.XMLMessageDispatcher}.
 *
 * <p>When created, a java.util.List of Handlers is passed in. This list
 * is sorted into logical and protocol handlers, so the handler order
 * that is returned from getHandlerChain() may be different from the
 * original that was passed in.
 *
 * <p>At runtime, one of the callHandlers() methods is invoked by the
 * soap or xml message dispatchers, passing in a {@link HandlerContext}
 * or {@link XMLHandlerContext} object along with other information
 * about the current message that is required for proper handler flow.
 *
 * <p>Exceptions are logged in many cases here before being rethrown. This
 * is to help primarily with server side handlers.
 *
 * <p>Currently, the handler chain caller checks for a null soap
 * message context to see if the binding in use is XML/HTTP.
 *
 * @see com.sun.xml.ws.binding.BindingImpl
 * @see com.sun.xml.ws.protocol.soap.client.SOAPMessageDispatcher
 * @see com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher
 * @see com.sun.xml.ws.protocol.xml.server.XMLMessageDispatcher
 *
 * @author WS Development Team
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
    
    private Set<URI> roles;

    /**
     * The handlers that are passed in will be sorted into
     * logical and soap handlers.
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

    public void setRoles(Set<URI> roles) {
        this.roles = roles;
    }

    public Set<URI> getRoles() {
        return roles;
    }
    
    // returns a string version of the roles
    public Set<String> getRoleStrings() {
        if (roles == null) {
            return null;
        }
        Set<String> rStrings = new HashSet<String>(roles.size());
        for (URI role : roles) {
            rStrings.add(role.toString());
        }
        return rStrings;
    }

    public Set<QName> getUnderstoodHeaders() {
        return understoodHeaders;
    }

    /**
     * This method separates the logical and protocol handlers. When
     * this method returns, the original "handlers" List has been
     * resorted.
     */
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

    /**
     * Replace the message in the given message context with a
     * fault message. If the context already contains a fault
     * message, then return without changing it.
     */
    private void insertFaultMessage(ContextHolder holder,
        ProtocolException exception) {

        try {
            SOAPMessageContext context = holder.getSMC();
            if (context == null) { // non-soap case
                LogicalMessage msg = holder.getLMC().getMessage();
                if (msg != null) {
                    msg.setPayload(null);
                }
                return;
            }
            
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

//            // try to handle any nulls gracefully
//            if (exception instanceof SOAPFaultException) {
//                SOAPFaultException sfe = (SOAPFaultException) exception;
//                if (sfe.getFaultCode() != null) {
//                    fault.setFaultCode(envelope.createName(
//                        sfe.getFaultCode().getLocalPart(),
//                        sfe.getFaultCode().getPrefix(),
//                        sfe.getFaultCode().getNamespaceURI()));
//                } else {
//                    fault.setFaultCode(envelope.createName("Server",
//                        "env", "http://schemas.xmlsoap.org/soap/envelope/"));
//                }
//                if (sfe.getFaultString() != null) {
//                    fault.setFaultString(sfe.getFaultString());
//                } else {
//                    if (sfe.getMessage() != null) {
//                        fault.setFaultString(sfe.getMessage());
//                    } else {
//                        fault.setFaultString(sfe.toString());
//                    }
//                }
//                if (sfe.getFaultActor() != null) {
//                    fault.setFaultActor(sfe.getFaultActor());
//                } else {
//                    fault.setFaultActor("");
//                }
//                if (sfe.getDetail() != null) {
//                    fault.addChildElement(sfe.getDetail());
//                }
//            } else if (exception.getMessage() != null) {
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
            // severe since this is from runtime and not handler
            logger.log(Level.SEVERE,
                "exception while creating fault message in handler chain", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to set a property so that the runtime
     * knows to ignore a fault in a message. It is used when there
     * is a new exception thrown during handle fault processing.
     */
    private void setIgnoreFaultProperty(ContextHolder holder) {
       holder.getLMC().put(IGNORE_FAULT_PROPERTY, Boolean.TRUE);
       holder.getLMC().setScope(IGNORE_FAULT_PROPERTY,
           MessageContext.Scope.APPLICATION);
    }

    /**
     * Method used to call handlers with a HandlerContext that
     * may contain logical and protocol handlers.
     */
    public boolean callHandlers(Direction direction,
        RequestOrResponse messageType,
        SOAPHandlerContext context,
        boolean responseExpected) {
        
        return internalCallHandlers(direction, messageType,
            new ContextHolder(context), responseExpected);
    }
    
    /**
     * Method used to call handlers with a HandlerContext that
     * may contain logical and protocol handlers.
     */
    public boolean callHandlers(Direction direction,
        RequestOrResponse messageType,
        XMLHandlerContext context,
        boolean responseExpected) {
        
        return internalCallHandlers(direction, messageType,
            new ContextHolder(context), responseExpected);
    }
    
    /**
     * Main runtime method, called internally by the callHandlers()
     * methods that may be called with HandlerContext or 
     * XMLHandlerContext objects.
     *
     * The boolean passed in is whether or not a response is required
     * for the current message. See section 5.3.2. (todo: this section
     * is going to change). 
     *
     * The callLogicalHandlers and callProtocolHandlers methods will
     * take care of execution once called and return true or false or
     * throw an exception.
     */
    private boolean internalCallHandlers(Direction direction,
        RequestOrResponse messageType,
        ContextHolder ch,
        boolean responseExpected) {

        // set outbound property
        ch.getLMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY,
            (direction == Direction.OUTBOUND));
        
        // if there is as soap message context, set roles
        if (ch.getSMC() != null) {
            ((SOAPMessageContextImpl) ch.getSMC()).setRoles(getRoles());
        }

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

    /**
     * This method called by the server when an endpoint has thrown
     * an exception. This method calls handleFault on the handlers
     * and closes them. Because this method is called only during
     * a response after the endpoint has been reached, all of the
     * handlers have been called during the request and so all are
     * closed.
     */
    public boolean callHandleFault(SOAPHandlerContext context) {
        ContextHolder ch = new ContextHolder(context);
        ch.getSMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, true);
        ((SOAPMessageContextImpl) ch.getSMC()).setRoles(getRoles());

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
                if (soapHandlers.get(j).handleFault(ch.getSMC()) == false) {
                    return false;
                }
                j++;
            }
        } catch (RuntimeException re) {
            logger.log(Level.FINER, "exception in handler chain", re);
            throw re;
        } finally {
            closeHandlers(ch);
        }
        return true;
    }

    /**
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
                logger.log(Level.FINER, "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    insertFaultMessage(holder, (ProtocolException) re);

                    // reverse direction and handle fault
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
                logger.log(Level.FINER, "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    insertFaultMessage(holder, (ProtocolException) re);
                    
                    // reverse direction and handle fault
                    try {
                        // if i==size-1, no more logical handlers to call
                        if (i == logicalHandlers.size()-1 ||
                            callLogicalHandleFault(holder, i+1,
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
                    closeProtocolHandlers(holder, soapHandlers.size()-1, 0);
                    closeLogicalHandlers(holder, logicalHandlers.size()-1, i);
                }
                throw re;
            }
        }

        return true;
    }

    /**
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
                            if (i>0) {
                                callProtocolHandleMessage(holder, i-1, 0);
                            }
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
                logger.log(Level.FINER, "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    insertFaultMessage(holder, (ProtocolException) re);

                    // reverse direction and handle fault
                    try {
                        if (i == 0 || // still on first handler
                            callProtocolHandleFault(holder, i-1, 0)) {
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

                        // reverse and call handle message/response
                        if (responseExpected && i != soapHandlers.size()-1) {
                            callProtocolHandleMessage(holder, i+1,
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
                logger.log(Level.FINER, "exception in handler chain", re);
                if (responseExpected && re instanceof ProtocolException) {
                    insertFaultMessage(holder, (ProtocolException) re);

                    // reverse direction and handle fault
                    try {
                        if (i < soapHandlers.size()-1) {
                            callProtocolHandleFault(holder, i+1,
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
                    closeProtocolHandlers(holder, soapHandlers.size()-1, i);
                }
                throw re;
            }
        }
        return true;
    }

    /**
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

    /**
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

    /**
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

    /**
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

    /**
     * Utility method that closes protocol handlers and then
     * logical handlers.
     */
    private void closeHandlers(ContextHolder holder) {
        closeProtocolHandlers(holder, soapHandlers.size()-1, 0);
        closeLogicalHandlers(holder, logicalHandlers.size()-1, 0);
    }

    /**
     * This version is called by the server code once it determines
     * that an incoming message is a one-way request. Or it is called
     * by the client when an MU fault occurs since the handler chain
     * never gets invoked. Either way, the direction is an inbound message.
     */
    public void forceCloseHandlers(SOAPHandlerContext context) {
        ContextHolder ch = new ContextHolder(context);

        // only called after an inbound request
        ch.getSMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
        ((SOAPMessageContextImpl) ch.getSMC()).setRoles(getRoles());
        closeHandlers(ch);
    }

    /**
     * Version of forceCloseHandlers(HandlerContext) that is used
     * by XML binding.
     */
    public void forceCloseHandlers(XMLHandlerContext context) {
        ContextHolder ch = new ContextHolder(context);

        // only called after an inbound request
        ch.getLMC().put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, false);
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
    
    /**
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
     * Used to hold the context objects that are used to get
     * and set the current message.
     *
     * If a HandlerContext is passed in, both logical and soap
     * handlers are used. If XMLHandlerContext is passed in,
     * only logical handlers are assumed to be present.
     */
    static class ContextHolder {

        boolean logicalOnly;
        SOAPHandlerContext context;
        XMLHandlerContext xmlContext;

        ContextHolder(SOAPHandlerContext context) {
            this.context = context;
            logicalOnly = false;
        }
        
        ContextHolder(XMLHandlerContext xmlContext) {
            this.xmlContext = xmlContext;
            logicalOnly = true;
        }

        LogicalMessageContext getLMC() {
            return (logicalOnly ? xmlContext.getLogicalMessageContext() :
                context.getLogicalMessageContext());
        }

        SOAPMessageContext getSMC() {
            return (logicalOnly ? null : context.getSOAPMessageContext());
        }
    }
    
}
