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

package com.sun.xml.ws.encoding.soap.server;

import com.sun.xml.ws.handler.MessageContextUtil;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;
import com.sun.xml.ws.server.*;
import com.sun.xml.ws.streaming.XMLStreamReaderException;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.handler.MessageContext;


/**
 * @author WS Development Team
 */
public class SOAPXMLDecoder extends SOAPDecoder {
    
    private static final Set<String> requiredRoles = new HashSet<String>();
    
    public SOAPXMLDecoder() {
        requiredRoles.add("http://schemas.xmlsoap.org/soap/actor/next");
        requiredRoles.add("");
    }

    /*
     *
     * @throws ServerRtException
     * @see SOAPDecoder#toInternalMessage(SOAPMessage)
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            InternalMessage request = new InternalMessage();
            processAttachments(messageInfo, request, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = SourceReaderFactory.createSourceReader(source, true,getSOAPMessageCharsetEncoding(soapMessage));
            XMLStreamReaderUtil.nextElementContent(reader);
            decodeEnvelope(reader, request, false, messageInfo);
            return request;
        } catch(Exception e) {
            if (isBadXML(e)) {
                RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
                HandlerContext handlerCtxt = rtCtxt.getHandlerContext();
                raiseBadXMLFault(handlerCtxt);
            }
            throw new ServerRtException("soapdecoder.err", new Object[]{e});
        } finally {
            if (reader != null) {
                XMLStreamReaderUtil.close(reader);
            }
        }
    }
    
    protected boolean isBadXML(Exception e) {
        while (e != null) {
            if (e instanceof XMLStreamException) {
                return true;
            }
            e = (e.getCause() instanceof Exception) ? (Exception)e.getCause() : null;
        }
        return false;
    }


    /*
     * Headers from SOAPMesssage are mapped to HeaderBlocks in InternalMessage
     * Body from SOAPMessage is skipped
     * BodyBlock in InternalMessage is converted to JAXBTypeInfo or RpcLitPayload
     *
     * @throws ServerRtException
     * @see SOAPDecoder#toInternalMessage(SOAPMessage, InternalMessage)
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
            InternalMessage request, MessageInfo messageInfo) {

        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            processAttachments(messageInfo, request, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = SourceReaderFactory.createSourceReader(source, true,getSOAPMessageCharsetEncoding(soapMessage));
            XMLStreamReaderUtil.nextElementContent(reader);
            decodeEnvelope(reader, request, true, messageInfo);
            convertBodyBlock(request, messageInfo);
        } catch(Exception e) {
            if (isBadXML(e)) {
                RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
                HandlerContext handlerCtxt = rtCtxt.getHandlerContext();
                raiseBadXMLFault(handlerCtxt);
            }
            throw new ServerRtException("soapdecoder.err", new Object[]{e});
        } finally {
            if (reader != null) {
                XMLStreamReaderUtil.close(reader);
            }
        }
        return request;
    }

    @Override
    public void decodeDispatchMethod(XMLStreamReader reader, InternalMessage request, MessageInfo messageInfo) {
        // Operation's QName. takes care of <body/>
        QName name = (reader.getEventType() == XMLStreamConstants.START_ELEMENT) ? reader.getName() : null;
        MessageContext msgCtxt = MessageInfoUtil.getMessageContext(messageInfo);        
        if (msgCtxt != null) {
            MessageContextUtil.setWsdlOperation(msgCtxt, name);
        }
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        Method method = rtCtxt.getDispatchMethod(name, messageInfo);
        if (method == null) {
            raiseFault(getSenderFaultCode(), "Cannot find the dispatch method");
        }
        messageInfo.setMethod(method);
    }
    
    protected SOAPFaultInfo decodeFault(XMLStreamReader reader, InternalMessage internalMessage,
        MessageInfo messageInfo) {
        raiseFault(getSenderFaultCode(), "Server cannot handle fault message");
        return null;
    }
    
    @Override
    protected void raiseBadXMLFault(HandlerContext ctxt) {
        MessageContextUtil.setHttpStatusCode(ctxt.getMessageContext(), 400);
        raiseFault(getSenderFaultCode(), "Bad request");
    }

    public Set<String> getRequiredRoles() {
        return requiredRoles;
    }
    
    @Override
    public String getBindingId() {
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
    
    @Override
    protected QName getSenderFaultCode() {
        return SOAPConstants.FAULT_CODE_CLIENT;
    }
    
    @Override
    protected QName getReceiverFaultCode() {
        return SOAPConstants.FAULT_CODE_SERVER;
    }
    
    @Override
    protected QName getVersionMismatchFaultCode() {
        return SOAPConstants.FAULT_CODE_VERSION_MISMATCH;
    }
    
}
