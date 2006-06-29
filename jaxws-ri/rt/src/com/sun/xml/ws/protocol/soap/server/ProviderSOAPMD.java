/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.protocol.soap.server;

import javax.xml.ws.Service;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.handler.SOAPHandlerContext;
import com.sun.xml.ws.handler.LogicalMessageImpl;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;
import com.sun.xml.ws.util.FastInfosetUtil;

import static com.sun.xml.ws.developer.JAXWSProperties.*;
import com.sun.xml.ws.server.provider.ProviderModel;
import com.sun.xml.ws.server.provider.ProviderPeptTie;

public class ProviderSOAPMD extends SOAPMessageDispatcher {

    /*
     * Fill the parameters, method in MessageInfo for Provider interface.
     * invoke(Source, HandlerContext) to Object[]
     * invoke(SOAPMessage, HandlerContext) to Object[]
     */
    @Override
    protected void toMessageInfo(MessageInfo messageInfo, SOAPHandlerContext context) {
        Object[] data = new Object[1];
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Class providerClass = endpointInfo.getImplementorClass();
        ProviderModel model = endpointInfo.getProviderModel();
        boolean isSource = model.isSource();
        Service.Mode mode = model.getServiceMode();

        if (mode == Service.Mode.PAYLOAD) {
            if (isSource) {
                data[0] = new LogicalMessageImpl(context).getPayload();
            }
            // else doesn't happen and it is checked while creating the model
        } else {
            InternalMessage internalMessage = context.getInternalMessage();
            SOAPMessage soapMessage = context.getSOAPMessage();
            try {
                if (internalMessage != null) {
                    // SOAPMessage's body is replaced by InternalMessage's BodyBlock
                    SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
                    SOAPEncoder encoder = eptf.getSOAPEncoder();
                    soapMessage = encoder.toSOAPMessage(internalMessage, soapMessage);
                }
                if (isSource) {
                    // Get SOAPMessage's SOAPPart as Source
                    data[0]= soapMessage.getSOAPPart().getContent();
                } else {
                    data[0] = soapMessage;
                }
            } catch(Exception e) {
                messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                messageInfo.setResponse(e);
            }
        }
        messageInfo.setData(data);
        messageInfo.setMethod(ProviderPeptTie.invoke_Method);
    }

    /*
     * MessageInfo contains the endpoint invocation results. If the endpoint
     * returns a SOAPMessage, just set the object in HandlerContext. If the
     * endpoint returns a Source in Mode.MESSAGE, it is converted to SOAPMessage
     * and set in HandlerContext. If the endpoint returns a Source in
     * Mode.PAYLOAD, it is set in InternalMessage, and InternalMessage is set
     * in HandlerContext
     */
    @Override
    protected void setResponseInContext(MessageInfo messageInfo,
            SOAPHandlerContext context) 
    {
        Object obj = messageInfo.getResponse();
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Class providerClass = endpointInfo.getImplementorClass();
        ProviderModel model = endpointInfo.getProviderModel();
        Service.Mode mode = model.getServiceMode();
        
        if (messageInfo.getResponseType() == MessageInfo.NORMAL_RESPONSE &&
                mode == Service.Mode.MESSAGE) {
            SOAPMessage soapMessage = null;
            if (obj instanceof SOAPMessage) {
                soapMessage = (SOAPMessage)obj;
            } else {
                // put Source into SOAPPart of SOAPMessage
                try {
                    Source source = (Source)obj;
                    String bindingId = ((BindingImpl)endpointInfo.getBinding()).getBindingId();
                    soapMessage = SOAPUtil.createMessage(bindingId);
                    soapMessage.getSOAPPart().setContent(source);
                } catch(SOAPException e) {
                    throw new ServerRtException("soapencoder.err", new Object[]{e});
                }
            }
            
            // Ensure message is encoded according to conneg
            FastInfosetUtil.ensureCorrectEncoding(messageInfo, soapMessage);
            
            context.setSOAPMessage(soapMessage);
            context.setInternalMessage(null);
        } 
        else {
            // set Source or any Exception in InternalMessage's BodyBlock
            SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
            InternalEncoder ine = eptf.getInternalEncoder();
            InternalMessage internalMessage =
                (InternalMessage)ine.toInternalMessage(messageInfo);
            // set handler context
            context.setInternalMessage(internalMessage);
            context.setSOAPMessage(null);
        }
    }

}
