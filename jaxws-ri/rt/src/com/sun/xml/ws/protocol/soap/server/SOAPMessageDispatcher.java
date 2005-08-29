/*
 * $Id: SOAPMessageDispatcher.java,v 1.17 2005-08-29 19:37:31 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.sun.xml.ws.protocol.soap.server;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.presentation.Tie;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.client.BindingProviderProperties;
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
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import javax.xml.ws.Binding;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.SOAPMessage;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.xml.ws.server.*;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.util.SOAPConnectionUtil;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.server.AppMsgContextImpl;

import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY;

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

            // Content negotiation logic (TODO: remove dep with SAAJ RI)
            try {
                // If FI is accepted by client, set property to optimistic
                if (((com.sun.xml.messaging.saaj.soap.MessageImpl) soapMessage).acceptFastInfoset()) {
                    messageInfo.setMetaData(CONTENT_NEGOTIATION_PROPERTY, "optimistic");
                }                
            }
            catch (ClassCastException e) {
                // Content negotiation fails
            }
            
            HandlerContext context = new HandlerContext(messageInfo, null,
                soapMessage);
            updateContextPropertyBag(messageInfo, context);
                    
            boolean skipEndpoint = false;
            SystemHandlerDelegate shd = getSystemHandlerDelegate(messageInfo);
            if (shd != null) {
                context.getMessageContext().put(
                    MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.FALSE);
                skipEndpoint = !shd.processRequest(
                        context.getSOAPMessageContext());
                // TODO: need to act if processRequest() retuns false
            }
            boolean peekOneWay = false;
            try {
                LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                SOAPDecoder decoder = eptf.getSOAPDecoder();
                peekOneWay = decoder.doMustUnderstandProcessing(soapMessage,
                        messageInfo, context, true);
                //peekOneWay = checkHeadersPeekBody(messageInfo, context);
            } catch (SOAPFaultException e) {
                skipEndpoint = true;
                RuntimeEndpointInfo rei = MessageInfoUtil.getRuntimeContext(
                    messageInfo).getRuntimeEndpointInfo();
                String id = ((SOAPBindingImpl)
                    rei.getBinding()).getBindingId();
                InternalMessage internalMessage = null;
                if (id.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
                    internalMessage = SOAPRuntimeModel.createFaultInBody(
                        e, null, null, null);
                } else if (id.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
                    internalMessage = SOAPRuntimeModel.createSOAP12FaultInBody(
                        e, null, null, null, null);
                }
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
                    LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
                    SOAPEncoder encoder = eptf.getSOAPEncoder();
                    soapMessage = encoder.toSOAPMessage(internalMessage, messageInfo);
                }
                sendResponse(messageInfo, soapMessage);
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
                        shd.preInvokeEndpointHook(context.getMessageContext());
                    }
                    updateWebServiceContext(messageInfo, context);
                    invokeEndpoint(messageInfo, context);
                }

                if (isOneway(messageInfo)) {
                    if (isFailure(messageInfo)) {
                        // Just log the error. Not much to do
                    }
                } else {
                    //updateContextPropertyBag(messageInfo, context);
                    soapMessage = getResponse(messageInfo, context);
                    if (shd != null) {
                        context.getMessageContext().put(
                    MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
                        shd.processResponse(
                                context.getSOAPMessageContext());
                    }
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
            Binding binding = MessageInfoUtil.getRuntimeContext(messageInfo).getRuntimeEndpointInfo().getBinding();
            String bindingId = (binding != null)?((SOAPBindingImpl)binding).getBindingId():SOAPBinding.SOAP11HTTP_BINDING;

            if (messageInfo.getMethod() == null) {
                messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                SOAPFaultInfo faultInfo = new SOAPFaultInfo(
                            "Cannot find dispatch method",
                            SOAPConstants.FAULT_CODE_SERVER,
                            null, null, bindingId);
                messageInfo.setResponse(faultInfo);
            } else {
                /* TODO
                context.put(MessageContext.WSDL_OPERATION,
                    messageInfo.getMetaData("METHOD_QNAME"));
                 */
            }
            //updateMessageInfoPropertyBag(context, messageInfo);
        }
    }

    /*
     * Gets SOAPMessage from the connection
     */
    private SOAPMessage getSOAPMessage(MessageInfo messageInfo) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        return SOAPConnectionUtil.getSOAPMessage(con, messageInfo, null);
    }
    
    /*
     * Sets the WebServiceContext with correct MessageContext which contains
     * APPLICATION scope properties
     */
    protected void updateWebServiceContext(MessageInfo messageInfo, HandlerContext hc) {
        RuntimeEndpointInfo endpointInfo = 
            MessageInfoUtil.getRuntimeContext(messageInfo).getRuntimeEndpointInfo();
        WebServiceContext wsContext = endpointInfo.getWebServiceContext();
        if (wsContext != null) {
            AppMsgContextImpl appCtxt = new AppMsgContextImpl(hc.getMessageContext());
            wsContext.setMessageContext(appCtxt);
        }
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
        if (internalMessage != null) {
            LogicalEPTFactory eptf = (LogicalEPTFactory)messageInfo.getEPTFactory();
            SOAPEncoder encoder = eptf.getSOAPEncoder();
            SOAPMessage soapMesage = encoder.toSOAPMessage(internalMessage, messageInfo);
            context.setSOAPMessage(soapMesage);
            context.setInternalMessage(null);
        }
        return context.getSOAPMessage();
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
        WSConnection con = (WSConnection)messageInfo.getConnection();
        SOAPConnectionUtil.sendResponse(con, soapMessage);
    }

    protected void sendResponseOneway(MessageInfo messageInfo) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        SOAPConnectionUtil.sendResponseOneway(con);
    }

    private void sendResponseError(MessageInfo messageInfo, Exception e) {
        e.printStackTrace();
        WSConnection con = (WSConnection)messageInfo.getConnection();
        Binding binding = MessageInfoUtil.getRuntimeContext(messageInfo).getRuntimeEndpointInfo().getBinding();
        String bindingId = ((SOAPBindingImpl)binding).getBindingId();
        SOAPConnectionUtil.sendResponseError(con, bindingId);
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
        RuntimeContext context = (RuntimeContext)
            info.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        Binding binding = context.getRuntimeEndpointInfo().getBinding();
        HandlerChainCaller caller =
            new HandlerChainCaller(binding.getHandlerChain());
        if (binding instanceof SOAPBinding) {
            caller.setRoles(((SOAPBinding) binding).getRoles());
        }
        return caller;
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
        
        RuntimeEndpointInfo endpointInfo = 
            MessageInfoUtil.getRuntimeContext(messageInfo).getRuntimeEndpointInfo();
        WebServiceContext wsContext = endpointInfo.getWebServiceContext();
        if (wsContext != null) {
            context.setMessageContext(wsContext.getMessageContext());
        }
    }

    /*
     * Check the headers for MU fault and peek into the
     * body and make a best guess as to whether the request
     * is one-way or not. Assume request-response
     * if it cannot be determined.
     *
     *
    private boolean checkHeadersPeekBody(MessageInfo mi, HandlerContext context)
            throws SOAPException {

        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        SOAPMessage message = context.getSOAPMessage();
        SOAPHeader header = message.getSOAPHeader();
        if (header != null) {
            //TODO remove, we dont want enc/dec to have any state
            //rtCtxt.setMethodAndMEP(null, context.getMessageInfo());
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
     */

    /*
     * Try to create as few objects as possible, thus carry
     * around null sets when possible and check if MU headers
     * are found. Also assume handler chain caller is null
     * unless one is found.
     *
    private void checkMustUnderstandHeaders(MessageInfo mi, HandlerContext context,
        SOAPHeader header) throws SOAPException {

        // this is "finer" level
        logger.entering("com.sun.xml.ws.server.SOAPMessageDispatcher",
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
        SOAPRuntimeModel model = (SOAPRuntimeModel)rtCtxt.getModel();
        if (model != null && model.getKnownHeaders() != null) {
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
     */
    
    private SystemHandlerDelegate getSystemHandlerDelegate(MessageInfo mi) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        return endpointInfo.getBinding().getSystemHandlerDelegate();
    }

}

