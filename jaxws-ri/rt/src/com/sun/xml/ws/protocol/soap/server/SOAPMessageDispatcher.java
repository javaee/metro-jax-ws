/*
 * $Id: SOAPMessageDispatcher.java,v 1.39 2005-10-18 20:39:47 vivekp Exp $
 */
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
package com.sun.xml.ws.protocol.soap.server;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.presentation.Tie;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerChainCaller.Direction;
import com.sun.xml.ws.handler.HandlerChainCaller.RequestOrResponse;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.handler.SOAPHandlerContext;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.AppMsgContextImpl;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.spi.runtime.Invoker;
import com.sun.xml.ws.spi.runtime.SystemHandlerDelegate;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.FastInfosetUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPConnectionUtil;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY;

public class SOAPMessageDispatcher implements MessageDispatcher {
    
    private static final String[] contentTypes = {
            "text/xml", "application/soap+xml", "application/xop+xml",
            "application/fastinfoset", "application/soap+fastinfoset" };

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".server.soapmd");
    /*
    private Localizer localizer = new Localizer();
    private LocalizableMessageFactory messageFactory =
        new LocalizableMessageFactory("com.sun.xml.ws.resources.soapmd");
     */

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
            try {
                checkContentType(messageInfo);
            } catch(ServerRtException e) {
                SOAPConnectionUtil.sendKnownError(messageInfo,
                        WSConnection.UNSUPPORTED_MEDIA);
                return;
            }
            
            SOAPMessage soapMessage = null;
            try {
                soapMessage = getSOAPMessage(messageInfo);
            } catch(Exception e) {
                sendResponseError(messageInfo, e);
                return;
            }

            // Content negotiation logic
            try {
                // If FI is accepted by client, set property to optimistic
                if (((com.sun.xml.messaging.saaj.soap.MessageImpl) soapMessage).acceptFastInfoset()) {
                    messageInfo.setMetaData(CONTENT_NEGOTIATION_PROPERTY, "optimistic");
                }                
            }
            catch (ClassCastException e) {
                // Content negotiation fails
            }
            
            SOAPHandlerContext context = new SOAPHandlerContext(messageInfo, null,
                soapMessage);
            updateHandlerContext(messageInfo, context);
                    
            SystemHandlerDelegate shd = getSystemHandlerDelegate(messageInfo);
            SoapInvoker implementor = new SoapInvoker(messageInfo, soapMessage,
                context, shd);
            if (shd == null) {
                implementor.invoke();
            } else {                
                context.getMessageContext().put(
                    MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.FALSE);
                context.setInvoker(implementor);
                if (shd.processRequest(context.getSHDSOAPMessageContext())) {
                    implementor.invoke();
                    context.getMessageContext().put(
                        MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);
                    shd.processResponse(context.getSHDSOAPMessageContext());
                }
            }
            if (!isOneway(messageInfo)) {
                makeSOAPMessage(messageInfo, context);
                sendResponse(messageInfo, context);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            sendResponseError(messageInfo, e);
        }
    }

    protected void toMessageInfo(MessageInfo messageInfo, SOAPHandlerContext context) {
        InternalMessage internalMessage = context.getInternalMessage();
        try {
            SOAPMessage soapMessage = context.getSOAPMessage();
            if (internalMessage == null) {
                // Bind headers, body from SOAPMessage
                SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
                SOAPDecoder decoder = eptf.getSOAPDecoder();
                internalMessage = decoder.toInternalMessage(soapMessage, messageInfo);
            } else {
                // Bind headers from SOAPMessage
                SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
                SOAPDecoder decoder = eptf.getSOAPDecoder();
                internalMessage = decoder.toInternalMessage(soapMessage, internalMessage, messageInfo);
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(e);
        }
        // InternalMessage to MessageInfo
        if (!isFailure(messageInfo)) {
            SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
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
            }
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
     * Checks against know Content-Type headers
     */
    private void checkContentType(MessageInfo mi) {
        WSConnection con = (WSConnection)mi.getConnection();
        Map<String, List<String>> headers = con.getHeaders();
        List<String> cts = headers.get("Content-Type");
        if (cts != null && cts.size() > 0) {
            String ct = cts.get(0);
            for(String contentType : contentTypes) {
                if (ct.indexOf(contentType) != -1) {
                    return;
                }
            }
        }
        throw new ServerRtException("Incorrect Content-Type="+cts);
    }
    
    /*
     * Sets the WebServiceContext with correct MessageContext which contains
     * APPLICATION scope properties
     */
    protected void updateWebServiceContext(MessageInfo messageInfo, SOAPHandlerContext hc) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        //rtCtxt.setHandlerContext(hc);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        WebServiceContext wsContext = endpointInfo.getWebServiceContext();
        if (wsContext != null) {
            AppMsgContextImpl appCtxt = new AppMsgContextImpl(hc.getMessageContext());
            wsContext.setMessageContext(appCtxt);
        }
    }

    /*
     * Invokes the endpoint.
     */
    protected void invokeEndpoint(MessageInfo messageInfo, SOAPHandlerContext hc) {
        TargetFinder targetFinder =
            messageInfo.getEPTFactory().getTargetFinder(messageInfo);
        Tie tie = targetFinder.findTarget(messageInfo);
        tie._invoke(messageInfo);
    }

    protected void getResponse(MessageInfo messageInfo, SOAPHandlerContext context) {
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
                    //there are handlers so disable Xop encoding if enabled, so that they dont
                    // see xop:Include reference
                    JAXWSAttachmentMarshaller am = MessageInfoUtil.getAttachmentMarshaller(messageInfo);
                    boolean isXopped = false;
                    if((am != null) && am.isXOPPackage()){
                        isXopped = am.isXOPPackage();
                        am.setXOPPackage(false);
                    }
                    callHandlersOnResponse(handlerCaller, context);
                    // now put back the old value
                    if((am != null)){
                        am.setXOPPackage(isXopped);
                    }
                }
            }
        } catch(Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            InternalMessage internalMessage = SOAPRuntimeModel.createFaultInBody(
                    e, null, null, null);
            context.setInternalMessage(internalMessage);
            context.setSOAPMessage(null);
        }
    }
    
    private void makeSOAPMessage(MessageInfo messageInfo, SOAPHandlerContext context) {
        InternalMessage internalMessage = context.getInternalMessage();
        if (internalMessage != null) {
            SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
            SOAPEncoder encoder = eptf.getSOAPEncoder();
            SOAPMessage soapMesage = encoder.toSOAPMessage(internalMessage, messageInfo);
            context.setSOAPMessage(soapMesage);
            context.setInternalMessage(null);
        }
    }

    /*
     * MessageInfo contains the endpoint invocation results. The information
     * is converted to InternalMessage or SOAPMessage and set in HandlerContext
     */
    protected void setResponseInContext(MessageInfo messageInfo,
            SOAPHandlerContext context) {
        // MessageInfo to InternalMessage
        SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
        InternalMessage internalMessage = (InternalMessage)eptf.getInternalEncoder().toInternalMessage(
                messageInfo);
        // set handler context
        context.setInternalMessage(internalMessage);
        context.setSOAPMessage(null);
    }

    /*
     * Sends SOAPMessage response on the connection
     */
    private void sendResponse(MessageInfo messageInfo, SOAPHandlerContext ctxt) {
        SOAPMessage soapMessage = ctxt.getSOAPMessage();
        WSConnection con = (WSConnection)messageInfo.getConnection();
        Integer status = MessageContextUtil.getHttpStatusCode(ctxt.getMessageContext());
        int statusCode = (status == null) ? WSConnection.OK : status;
        SOAPConnectionUtil.setStatus(con, statusCode);
        SOAPConnectionUtil.sendResponse(con, soapMessage);
    }
    
    protected void sendResponseOneway(MessageInfo messageInfo) {
        SOAPConnectionUtil.sendResponseOneway(messageInfo);
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
        SOAPHandlerContext context, boolean responseExpected) {

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
        HandlerChainCaller caller = MessageInfoUtil.getHandlerChainCaller(info);
        if (caller == null) {
            RuntimeContext context = (RuntimeContext) info.getMetaData(
                BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
            Binding binding = context.getRuntimeEndpointInfo().getBinding();
            caller = new HandlerChainCaller(binding.getHandlerChain());
            if (binding instanceof SOAPBinding) {
                caller.setRoles(((SOAPBinding) binding).getRoles());
            }
            MessageInfoUtil.setHandlerChainCaller(info, caller);
        }
        return caller;
    }

    protected boolean callHandlersOnResponse(HandlerChainCaller caller,
        SOAPHandlerContext context) {

        return caller.callHandlers(Direction.OUTBOUND,
            RequestOrResponse.RESPONSE, context, false);
    }

    /*
     * Used when the endpoint throws an exception. HandleFault is called
     * on the server handlers rather than handleMessage.
     */
    protected boolean  callHandleFault(HandlerChainCaller caller, SOAPHandlerContext context) {
        return caller.callHandleFault(context);
    }

    /*
     * Server does not know if a message is one-way until after
     * the handler chain has finished processing the request. If
     * it is a one-way message, have the handler chain caller
     * call close on the handlers.
     */
    private void closeHandlers(MessageInfo info, SOAPHandlerContext context) {
        HandlerChainCaller handlerCaller = getCallerFromMessageInfo(info);
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


    /*
     * Sets MessageContext into HandlerContext and sets HandlerContext in
     * RuntimeContext
     */
    private void updateHandlerContext(MessageInfo messageInfo,
            SOAPHandlerContext context) {
        MessageInfoUtil.getRuntimeContext(messageInfo).setHandlerContext(context);
        RuntimeEndpointInfo endpointInfo = 
            MessageInfoUtil.getRuntimeContext(messageInfo).getRuntimeEndpointInfo();
        context.setBindingId(((BindingImpl)endpointInfo.getBinding()).getActualBindingId());
        WebServiceContext wsContext = endpointInfo.getWebServiceContext();
        if (wsContext != null) {
            context.setMessageContext(wsContext.getMessageContext());
        }
    }
    
    private SystemHandlerDelegate getSystemHandlerDelegate(MessageInfo mi) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        return endpointInfo.getBinding().getSystemHandlerDelegate();
    }
    
    private class SoapInvoker implements Invoker {
    
        MessageInfo messageInfo;
        SOAPMessage soapMessage;
        SOAPHandlerContext context;
        boolean skipEndpoint;
        SystemHandlerDelegate shd;
        
        SoapInvoker(MessageInfo messageInfo, SOAPMessage soapMessage,
                SOAPHandlerContext context, SystemHandlerDelegate shd) {
            this.messageInfo = messageInfo;
            this.soapMessage = soapMessage;
            this.context = context;
            this.shd = shd;
        }
        
        public void invoke() throws Exception {
            boolean peekOneWay = false;
            if (!skipEndpoint) {
                try {
                    SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
                    SOAPDecoder decoder = eptf.getSOAPDecoder();
                    
                    // add handler chain caller to message info
                    getCallerFromMessageInfo(messageInfo);
                    peekOneWay = decoder.doMustUnderstandProcessing(soapMessage,
                            messageInfo, context, true);                
                    context.setMethod(messageInfo.getMethod());
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
                    SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
                    SOAPEncoder encoder = eptf.getSOAPEncoder();
                    soapMessage = encoder.toSOAPMessage(internalMessage, messageInfo);
                }
                
                // Ensure message is encoded according to conneg                
                FastInfosetUtil.ensureCorrectEncoding(messageInfo, soapMessage);
                
                //sendResponse(messageInfo, soapMessage);
                context.setSOAPMessage(soapMessage);
                context.setInternalMessage(null);
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
                        shd.preInvokeEndpointHook(context.getSHDSOAPMessageContext());
                    }
                    updateWebServiceContext(messageInfo, context);
                    invokeEndpoint(messageInfo, context);
                }

                if (isOneway(messageInfo)) {
                    if (isFailure(messageInfo)) {
                        // Just log the error. Not much to do
                    }
                } else {
                    getResponse(messageInfo, context);
                }
            }
        }
        
        public Method getMethod(QName name) {
            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
            return rtCtxt.getDispatchMethod(name, messageInfo);
        }
    }

}

