/*
 * $Id: XMLMessageDispatcher.java,v 1.4 2005-08-01 19:40:03 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.sun.xml.ws.protocol.xml.server;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.presentation.Tie;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.xml.XMLDecoder;
import com.sun.xml.ws.encoding.xml.XMLEPTFactory;
import com.sun.xml.ws.encoding.xml.XMLEncoder;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.XMLHandlerContext;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import javax.xml.ws.Binding;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.server.*;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.util.XMLConnectionUtil;

/**
 * @author WS Development Team
 *
 */
public class XMLMessageDispatcher implements MessageDispatcher {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.xmlmd");
    private Localizer localizer = new Localizer();
    private LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.xmlmd");

    public XMLMessageDispatcher() {
    }

    public void send(MessageInfo messageInfo) {
        // Not required for server
        throw new UnsupportedOperationException();
    }

    // TODO: need to work the exception logic
    public void receive(MessageInfo messageInfo) {
        try {
            XMLMessage xmlMessage = null;
            try {
                xmlMessage = getXMLMessage(messageInfo);
            } catch(Exception e) {
                sendResponseError(messageInfo, e);
                return;
            }

            XMLHandlerContext context = new XMLHandlerContext(messageInfo, null,
                xmlMessage);
            updateContextPropertyBag(messageInfo, context);
                    
            boolean skipEndpoint = false;
            SystemHandlerDelegate shd = getSystemHandlerDelegate(messageInfo);
            if (shd != null) {
                /*
                skipEndpoint = !shd.processRequest(
                        context.getLogicalMessageContext());
                 */
                // TODO: need to act if processRequest() retuns false
            }
            boolean peekOneWay = false;

            // Call inbound handlers. It also calls outbound handlers incase of
            // reversal of flow.
            if (!skipEndpoint) {
                skipEndpoint = callHandlersOnRequest(
                    messageInfo, context, !peekOneWay);
            }

            if (skipEndpoint) {
                xmlMessage = context.getXMLMessage();
                if (xmlMessage == null) {
                    InternalMessage internalMessage = context.getInternalMessage();
                    XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
                    XMLEncoder encoder = eptf.getXMLEncoder();
                    xmlMessage = encoder.toXMLMessage(internalMessage, messageInfo);
                }
                sendResponse(messageInfo, xmlMessage);
            } else {
                toMessageInfo(messageInfo, context);

                if (isOneway(messageInfo)) {
                    sendResponseOneway(messageInfo);
                    if (!peekOneWay) { // handler chain didn't already clos
                        closeHandlers(messageInfo, context);
                    }
                }

                if (!isFailure(messageInfo)) {
                    if (shd != null) {
                        shd.preInvokeEndpointHook(context);
                    }
                    invokeEndpoint(messageInfo, context);
                }

                if (isOneway(messageInfo)) {
                    if (isFailure(messageInfo)) {
                        // Just log the error. Not much to do
                    }
                } else {
                    updateContextPropertyBag(messageInfo, context);
                    xmlMessage = getResponse(messageInfo, context);
                    if (shd != null) {
                        /*
                        shd.processResponse(
                                context.getXMLMessageContext());
                         */
                    }
                    sendResponse(messageInfo, xmlMessage);
                }
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            sendResponseError(messageInfo, e);
        }
    }

    protected void toMessageInfo(MessageInfo messageInfo, XMLHandlerContext context) {
        InternalMessage internalMessage = context.getInternalMessage();
        try {
            XMLMessage xmlMessage = context.getXMLMessage();
            if (internalMessage == null) {
                // Bind headers, body from SOAPMessage
                XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
                XMLDecoder decoder = eptf.getXMLDecoder();
                internalMessage = decoder.toInternalMessage(xmlMessage, messageInfo);
            } else {
                // Bind headers from SOAPMessage
                XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
                XMLDecoder decoder = eptf.getXMLDecoder();
                internalMessage = decoder.toInternalMessage(xmlMessage, internalMessage, messageInfo);
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
            XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
            eptf.getInternalEncoder().toMessageInfo(internalMessage, messageInfo);
            if (messageInfo.getMethod() == null) {
                messageInfo.setResponseType(
                    MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
//                SOAPFaultInfo faultInfo = new SOAPFaultInfo(
//                    "Cannot find dispatch method",
//                    SOAPConstants.FAULT_CODE_SERVER,
//                    null, null);
//                messageInfo.setResponse(faultInfo);
            } else {
                context.put(MessageContext.WSDL_OPERATION,
                    messageInfo.getMetaData("METHOD_QNAME"));
            }
            updateMessageInfoPropertyBag(context, messageInfo);
        }
    }

    /*
     * Gets XMLMessage from the connection
     */
    private XMLMessage getXMLMessage(MessageInfo messageInfo) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        return XMLConnectionUtil.getXMLMessage(con, messageInfo);
    }

    /*
     * Invokes the endpoint.
     */
    protected void invokeEndpoint(MessageInfo messageInfo, XMLHandlerContext hc) {
        TargetFinder targetFinder =
            messageInfo.getEPTFactory().getTargetFinder(messageInfo);
        Tie tie = targetFinder.findTarget(messageInfo);
        tie._invoke(messageInfo);
    }

    protected XMLMessage getResponse(MessageInfo messageInfo, XMLHandlerContext context) {
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
            context.setXMLMessage(null);
        }
        InternalMessage internalMessage = context.getInternalMessage();
        if (internalMessage != null) {
            XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
            XMLEncoder encoder = eptf.getXMLEncoder();
            XMLMessage xmlMessage = encoder.toXMLMessage(internalMessage, messageInfo);
            context.setXMLMessage(xmlMessage);
        }
        return context.getXMLMessage();
    }

    /*
     * MessageInfo contains the endpoint invocation results. The information
     * is converted to InternalMessage or SOAPMessage and set in HandlerContext
     */
    protected void setResponseInContext(MessageInfo messageInfo,
            XMLHandlerContext context) {
        // MessageInfo to InternalMessage
        XMLEPTFactory eptf = (XMLEPTFactory)messageInfo.getEPTFactory();
        InternalMessage internalMessage = (InternalMessage)eptf.getInternalEncoder().toInternalMessage(
                messageInfo);
        // set handler context
        context.setInternalMessage(internalMessage);
        context.setXMLMessage(null);
    }

    /*
     * Sends SOAPMessage response on the connection
     */
    // TODO: HTTP response code
    private void sendResponse(MessageInfo messageInfo, XMLMessage xmlMessage) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        XMLConnectionUtil.sendResponse(con, xmlMessage);
    }

    protected void sendResponseOneway(MessageInfo messageInfo) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        //SOAPConnectionUtil.sendResponseOneway(con);
    }

    private void sendResponseError(MessageInfo messageInfo, Exception e) {
        e.printStackTrace();
        WSConnection con = (WSConnection)messageInfo.getConnection();
        //SOAPConnectionUtil.sendResponseError(con);
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
        XMLHandlerContext context, boolean responseExpected) {

        boolean skipEndpoint = false;
        HandlerChainCaller handlerCaller =
            getCallerFromMessageInfo(messageInfo);

        if (handlerCaller != null && handlerCaller.hasHandlers()) {
            skipEndpoint = !handlerCaller.callHandlers(Direction.INBOUND,
                RequestOrResponse.REQUEST, context, responseExpected);
        }
        return skipEndpoint;
    }

    private HandlerChainCaller getCallerFromMessageInfo(MessageInfo info) {
        RuntimeContext context = (RuntimeContext)
            info.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        Binding binding = context.getRuntimeEndpointInfo().getBinding();
        HandlerChainCaller caller =
            new HandlerChainCaller(binding.getHandlerChain());
        //if (binding instanceof SOAPBinding) {
        //    caller.setRoles(((SOAPBinding) binding).getRoles());
        //}
        return caller;
    }

    protected boolean callHandlersOnResponse(HandlerChainCaller caller,
        XMLHandlerContext context) {
        
        return caller.callHandlers(Direction.OUTBOUND,
            RequestOrResponse.RESPONSE, context, false);
    }

    /*
     * Used when the endpoint throws an exception. HandleFault is called
     * on the server handlers rather than handleMessage.
     */
    protected boolean  callHandleFault(HandlerChainCaller caller, XMLHandlerContext context) {
        /*
        return caller.callHandleFault(context);
         */
        return false;
    }

    /*
     * Server does not know if a message is one-way until after
     * the handler chain has finished processing the request. If
     * it is a one-way message, have the handler chain caller
     * call close on the handlers.
     */
    private void closeHandlers(MessageInfo info, XMLHandlerContext context) {
        HandlerChainCaller handlerCaller =
            (HandlerChainCaller) info.getMetaData(
                HandlerChainCaller.HANDLER_CHAIN_CALLER);
        if (handlerCaller != null && handlerCaller.hasHandlers()) {
            /*
            handlerCaller.forceCloseHandlers(context);
             */
        }
    }

    private static boolean isFailure(MessageInfo messageInfo) {
        return (messageInfo.getResponseType() == MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
    }

    public static boolean isOneway(MessageInfo messageInfo) {
        return (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP);
    }


    // copy from message info to handler context
    private void updateContextPropertyBag(MessageInfo messageInfo,
            XMLHandlerContext context) {
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
    private void updateMessageInfoPropertyBag(XMLHandlerContext context,
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
    
    private SystemHandlerDelegate getSystemHandlerDelegate(MessageInfo mi) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        return endpointInfo.getBinding().getSystemHandlerDelegate();
    }

}

