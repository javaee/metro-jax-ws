/*
 * $Id: SOAPMessageDispatcher.java,v 1.1 2005-05-23 22:50:25 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.sun.xml.ws.server;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.presentation.Tie;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.spi.runtime.JaxrpcConnection;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SOAPMessageDispatcher implements MessageDispatcher {
    
    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.soapmd");
    private Localizer localizer = new Localizer();
    private LocalizableMessageFactory messageFactory = 
        new LocalizableMessageFactory("com.sun.xml.ws.resources.soapmd");
    
    private final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING =
        "SOAP must understand error";

    public SOAPMessageDispatcher() {
    }
    
    public void send(MessageInfo messageInfo) {
        // Not required for server
        throw new UnsupportedOperationException();
    }
   
    // TODO: need to work the exception logic
    public void receive(MessageInfo messageInfo) {
        try {
            SOAPMessage soapMessage = null;
            try {
                soapMessage = getSOAPMessage(messageInfo);
            } catch(Exception e) {
                sendResponseError(messageInfo, e);
                return;
            }
            
            HandlerContext context = new HandlerContext(messageInfo, null,
                soapMessage);
            updateContextPropertyBag(messageInfo, context);

            boolean skipEndpoint = false;
            boolean peekOneWay = false;
            try {
                peekOneWay = checkHeadersPeekBody(messageInfo, context);
            } catch (Exception e) {
                skipEndpoint = true;
                InternalMessage internalMessage =
                    SOAPRuntimeModel.createFaultInBody(e, null, null, null);
                context.setInternalMessage(internalMessage);
                context.setSOAPMessage(null);
            }

            // Call inbound handlers. It also calls outbound handlers incase of
            // reversal of flow.
            if (!skipEndpoint) {
                skipEndpoint = callHandlersOnRequest(
                    messageInfo, context, !peekOneWay);
            }

            if (skipEndpoint) {
                soapMessage = context.getSOAPMessage();
                if (soapMessage == null) {
                    InternalMessage internalMessage = context.getInternalMessage();
                    //LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                    //SOAPEncoder encoder = eptf.getSOAPEncoder();
                    EPTFactory eptf = messageInfo.getEPTFactory();
                    SOAPEncoder encoder = (SOAPEncoder)eptf.getEncoder(messageInfo);
                    soapMessage = encoder.toSOAPMessage(internalMessage, messageInfo);
                }
                sendResponse(messageInfo, soapMessage);
            } else {
                toMessageInfo(messageInfo, context);
       
                if (isOneway(messageInfo)) {
                    sendResponseOneway(messageInfo);
                    if (!peekOneWay) { // handler chain didn't already close
                        closeHandlers(messageInfo, context);
                    }
                }
                
                if (!isFailure(messageInfo)) {
                    invokeEndpoint(messageInfo, context);
                }
                
                if (isOneway(messageInfo)) {
                    if (isFailure(messageInfo)) {
                        // Just log the error. Not much to do
                    }
                } else {
                    updateContextPropertyBag(messageInfo, context);
                    soapMessage = getResponse(messageInfo, context);
                    sendResponse(messageInfo, soapMessage);
                }
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            sendResponseError(messageInfo, e);
        }
    }
    
    protected void toMessageInfo(MessageInfo messageInfo, HandlerContext context) {
        InternalMessage internalMessage = context.getInternalMessage();
        try {
            SOAPMessage soapMessage = context.getSOAPMessage();
            if (internalMessage == null) {
                // Bind headers, body from SOAPMessage
                LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                SOAPDecoder decoder = eptf.getSOAPDecoder();
                internalMessage = decoder.toInternalMessage(soapMessage, messageInfo);
            } else {
                // Bind headers from SOAPMessage
                LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                SOAPDecoder decoder = eptf.getSOAPDecoder();
                internalMessage = decoder.toInternalMessage(soapMessage, internalMessage, messageInfo);
                // Convert to JAXB bean if body contains Source, or bean in other
                // JAXBContext
                //context.toJAXBBean(decoderUtil.getJAXBContext());
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(e);
        }
        // InternalMessage to MessageInfo
        if (!isFailure(messageInfo)) {
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            eptf.getInternalEncoder().toMessageInfo(internalMessage, messageInfo);
            if (messageInfo.getMethod() == null) {
                messageInfo.setResponseType(
                    MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                SOAPFaultInfo faultInfo = new SOAPFaultInfo(
                    SOAPConstants.FAULT_CODE_SERVER,
                    "Cannot find dispatch method", null, null);
                messageInfo.setResponse(faultInfo);
            } else {
                context.put(MessageContext.WSDL_OPERATION,
                    messageInfo.getMetaData("METHOD_QNAME"));
            }
            updateMessageInfoPropertyBag(context, messageInfo);
        }
    }
    
    /*
     * Gets SOAPMessage from the connection
     */
    private SOAPMessage getSOAPMessage(MessageInfo messageInfo) {
        JaxrpcConnection con = (JaxrpcConnection)messageInfo.getConnection();
        return SOAPConnectionUtil.getSOAPMessage(con);
    }
    
    /*
     * Invokes the endpoint.
     */
    protected void invokeEndpoint(MessageInfo messageInfo, HandlerContext hc) {
        TargetFinder targetFinder =
            messageInfo.getEPTFactory().getTargetFinder(messageInfo);
        Tie tie = targetFinder.findTarget(messageInfo);
        tie._invoke(messageInfo);
    }
    
    protected SOAPMessage getResponse(MessageInfo messageInfo, HandlerContext context) {
    	setResponseInContext(messageInfo, context);
        try {
            HandlerChainCaller handlerCaller =
                getCallerFromMessageInfo(messageInfo);
            if (handlerCaller != null && handlerCaller.hasHandlers()) {
                int messageType = messageInfo.getResponseType();
                if (messageType == MessageInfo.CHECKED_EXCEPTION_RESPONSE ||
                    messageType == MessageInfo.UNCHECKED_EXCEPTION_RESPONSE) {
                    
                    callHandleFault(handlerCaller, context);
                } else {
                    callHandlersOnResponse(handlerCaller, context);
                }
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            InternalMessage internalMessage = SOAPRuntimeModel.createFaultInBody(
                    e, null, null, null);
            context.setInternalMessage(internalMessage);
            context.setSOAPMessage(null);
        }
        InternalMessage internalMessage = context.getInternalMessage();
        if (internalMessage == null) {
            return context.getSOAPMessage();
        } else {
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            SOAPEncoder encoder = eptf.getSOAPEncoder();
            return encoder.toSOAPMessage(internalMessage, messageInfo);
        }
    }
    
    /*
     * MessageInfo contains the endpoint invocation results. The information
     * is converted to InternalMessage or SOAPMessage and set in HandlerContext
     */
    protected void setResponseInContext(MessageInfo messageInfo,
    		HandlerContext context) {
	    // MessageInfo to InternalMessage
        LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
	    InternalMessage internalMessage = (InternalMessage)eptf.getInternalEncoder().toInternalMessage(
	    		messageInfo);
	    // set handler context
	    context.setInternalMessage(internalMessage);
	    context.setSOAPMessage(null);
    }
    
    /*
     * Sends SOAPMessage response on the connection
     */
    // TODO: HTTP response code
    private void sendResponse(MessageInfo messageInfo, SOAPMessage soapMessage) {
        JaxrpcConnection con = (JaxrpcConnection)messageInfo.getConnection();
        SOAPConnectionUtil.sendResponse(con, soapMessage);
    }
    
    protected void sendResponseOneway(MessageInfo messageInfo) {
        JaxrpcConnection con = (JaxrpcConnection)messageInfo.getConnection();
        SOAPConnectionUtil.sendResponseOneway(con);
    }
    
    private void sendResponseError(MessageInfo messageInfo, Exception e) {
        e.printStackTrace();
        JaxrpcConnection con = (JaxrpcConnection)messageInfo.getConnection();
        SOAPConnectionUtil.sendResponseError(con);
    }
    
    

    /*
     * Calls inbound handlers. It also calls outbound handlers incase flow is
     * reversed. If the handler throws a ProtocolException, SOAP message is
     * already set in the context. Otherwise, it creates InternalMessage,
     * and that is used to create SOAPMessage.
     * 
     * returns whether to invoke endpoint or not.
     */
    private boolean callHandlersOnRequest(MessageInfo messageInfo,
        HandlerContext context, boolean responseExpected) {
        
        boolean skipEndpoint = false;
        HandlerChainCaller handlerCaller =
            getCallerFromMessageInfo(messageInfo);
        
        if (handlerCaller != null && handlerCaller.hasHandlers()) {
            try {
                skipEndpoint = !handlerCaller.callHandlers(Direction.INBOUND,
                    RequestOrResponse.REQUEST, context, responseExpected);
            } catch(ProtocolException pe) {
                skipEndpoint = true;
            } catch(RuntimeException re) {
                skipEndpoint = true;
                InternalMessage internalMessage =
                    SOAPRuntimeModel.createFaultInBody(re, null, null, null);
                context.setInternalMessage(internalMessage);
                context.setSOAPMessage(null);
            }
        }
        return skipEndpoint;
    }
    
    private HandlerChainCaller getCallerFromMessageInfo(MessageInfo info) {
        return MessageInfoUtil.getRuntimeContext(info).
            getRuntimeEndpointInfo().getHandlerChainCaller();
    }
    
    protected boolean callHandlersOnResponse(HandlerChainCaller caller,
        HandlerContext context) {
            
        return caller.callHandlers(Direction.OUTBOUND,
            RequestOrResponse.RESPONSE, context, false);
    }
    
    /*
     * Used when the endpoint throws an exception. HandleFault is called
     * on the server handlers rather than handleMessage.
     */
    protected boolean  callHandleFault(HandlerChainCaller caller, HandlerContext context) {
        return caller.callHandleFault(context);
    }

    /*
     * Server does not know if a message is one-way until after
     * the handler chain has finished processing the request. If
     * it is a one-way message, have the handler chain caller
     * call close on the handlers.
     */
    private void closeHandlers(MessageInfo info, HandlerContext context) {
        HandlerChainCaller handlerCaller =
            (HandlerChainCaller) info.getMetaData(
                HandlerChainCaller.HANDLER_CHAIN_CALLER);
        if (handlerCaller != null && handlerCaller.hasHandlers()) {
            handlerCaller.forceCloseHandlers(context);
        }
    }
    
    private static boolean isFailure(MessageInfo messageInfo) {
        return (messageInfo.getResponseType() == MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
    }
    
    public static boolean isOneway(MessageInfo messageInfo) {
        return (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP);
    }
    
/*
    protected void fine(String key, Object obj) {
        logger.fine(localizer.localize(messageFactory.getMessage(key, new Object[] { ""+obj.hashCode() })));
    }
*/
    
    // copy from message info to handler context
    private void updateContextPropertyBag(MessageInfo messageInfo,
            HandlerContext context) {
        MessageContext msgCtxt =
            (MessageContext) messageInfo.getMetaData("MESSAGE_CONTEXT");
        Set<String> keys = msgCtxt.keySet();
        for (String name : keys) {
            Object value = msgCtxt.get(name);
            context.put(name, value);
            context.setScope(name, Scope.APPLICATION);
        }
    }
    
    // copy from handler context to message info
    private void updateMessageInfoPropertyBag(HandlerContext context,
            MessageInfo messageInfo) {
        MessageContext msgCtxt =
            (MessageContext) messageInfo.getMetaData("MESSAGE_CONTEXT");
        Set<String> keys = context.keySet();
        for (String name : keys) {
            if (context.getScope(name) == Scope.APPLICATION) {
                msgCtxt.put(name, context.get(name));
                msgCtxt.setScope(name, Scope.APPLICATION);
            }
        }
    }

    /*
     * Check the headers for MU fault and peek into the
     * body and make a best guess as to whether the request
     * is one-way or not. Assume request-response
     * if it cannot be determined.
     *
     */
    private boolean checkHeadersPeekBody(MessageInfo mi, HandlerContext context)
            throws SOAPException {
     
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        SOAPMessage message = context.getSOAPMessage();
        SOAPHeader header = message.getSOAPHeader();
        if (header != null) {
            //TODO remove, we dont want enc/dec to have any state
            rtCtxt.setMethodAndMEP(null, context.getMessageInfo());            
            checkMustUnderstandHeaders(mi, context, header);
        }

        // peek into body if MU fault was not thrown
        try {
            Node node = message.getSOAPBody().getFirstChild();

            // ignore whitespace
            while (node.getNodeType() == Node.TEXT_NODE) {
                node = node.getNextSibling();
            }

            QName operationName = null;
            if (node != null) {     // To take care of <Body/>
                operationName = new QName(node.getNamespaceURI(),
                        node.getLocalName());
            }
            MessageInfo info = context.getMessageInfo();
            rtCtxt.setMethodAndMEP(operationName, info);
            return isOneway(info);
        } catch (Throwable t) {
            // assume cannot be read if there is an error
            return false;
        }

    }
    
    /*
     * Try to create as few objects as possible, thus carry
     * around null sets when possible and check if MU headers
     * are found. Also assume handler chain caller is null
     * unless one is found.
     */
    private void checkMustUnderstandHeaders(MessageInfo mi, HandlerContext context,
        SOAPHeader header) throws SOAPException {
        
        // this is "finer" level
        logger.entering("com.sun.xml.rpc.server.SOAPMessageDispatcher",
            "checkMustUnderstandHeaders");
        
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        
        // start with just the endpoint roles
        Set<String> roles = new HashSet<String>();
        roles.add("http://schemas.xmlsoap.org/soap/actor/next");
        roles.add("");
        HandlerChainCaller hcCaller = (HandlerChainCaller)
            context.getMessageInfo().getMetaData(
                HandlerChainCaller.HANDLER_CHAIN_CALLER);
        if (hcCaller != null) {
            roles.addAll(hcCaller.getRoles());
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("roles:");
            for (String r : roles) {
                logger.finest("\t\"" + r + "\"");
            }
        }

        // keep set=null if there are no understood headers
        Set<QName> understoodHeaders = null;
        if (((SOAPRuntimeModel)rtCtxt.getModel()).getKnownHeaders() != null) {
            understoodHeaders =
                new HashSet<QName>(((SOAPRuntimeModel)rtCtxt.getModel()).getKnownHeaders());
        }
        if (understoodHeaders == null) {
            if (hcCaller != null) {
                understoodHeaders = hcCaller.getUnderstoodHeaders();
            }
        } else {
            if (hcCaller != null) {
                understoodHeaders.addAll(hcCaller.getUnderstoodHeaders());
            }
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("understood headers:");
            if (understoodHeaders == null || understoodHeaders.isEmpty()) {
                logger.finest("\tnone");
            } else {
                for (QName nameX : understoodHeaders) {
                    logger.finest("\t" + nameX.toString());
                }
            }
        }

        // check MU headers for each role
        for (String role: roles) {
            logger.finest("checking role: " + role);
            Iterator<SOAPHeaderElement> iter =
                header.examineMustUnderstandHeaderElements(role);
            logger.finest("checking element targeted at role:");
            while (iter.hasNext()) {
                SOAPHeaderElement element = iter.next();
                QName qName = new QName(element.getNamespaceURI(),
                    element.getLocalName());
                logger.finest("\t" + qName.toString());
                if (understoodHeaders == null ||
                    !understoodHeaders.contains(qName)) {
                    logger.finest("*element not understood*");
                    throw new SOAPFaultException(
                        SOAPConstants.FAULT_CODE_MUST_UNDERSTAND,
                        MUST_UNDERSTAND_FAULT_MESSAGE_STRING,
                        role,
                        null);
                }
            }
        }
    }

}

