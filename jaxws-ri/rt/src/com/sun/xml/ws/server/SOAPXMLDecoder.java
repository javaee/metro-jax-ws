/*
 * $Id: SOAPXMLDecoder.java,v 1.1 2005-05-23 22:50:25 bbissett Exp $
 */

/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.server;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderFactory;
import com.sun.xml.ws.util.MessageInfoUtil;


/**
 * @author JAX-RPC RI Development Team
 */
public class SOAPXMLDecoder extends SOAPDecoder {
    private static final XMLReaderFactory factory = XMLReaderFactory.newInstance();

    public SOAPXMLDecoder() {
        
    }
    
    /*
     *
     * @throws ServerRtException
     * @see com.sun.xml.rpc.encoding.soap.SOAPDecoder#toInternalMessage(javax.xml.soap.SOAPMessage)
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLReader reader = null;
        try {
            InternalMessage request = new InternalMessage();
            Source source = soapMessage.getSOAPPart().getContent();
            reader = factory.createXMLReader(source, true);
            reader.nextElementContent();
            decodeEnvelope(reader, request, false, messageInfo);
            return request;
        } catch(Exception e) {
            throw new ServerRtException("soapdecoder.err", new Object[]{e});
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /*
     * Headers from SOAPMesssage are mapped to HeaderBlocks in InternalMessage
     * Body from SOAPMessage is skipped
     * BodyBlock in InternalMessage is converted to JAXBTypeInfo or RpcLitPayload
     *
     * @throws ServerRtException
     * @see com.sun.xml.rpc.encoding.soap.SOAPDecoder#toInternalMessage(javax.xml.soap.SOAPMessage, InternalMessage)
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
            InternalMessage request, MessageInfo messageInfo) {
        
        // TODO handle exceptions, attachments
        XMLReader reader = null;
        try {
            Source source = soapMessage.getSOAPPart().getContent();
            reader = factory.createXMLReader(source, true);
            reader.nextElementContent();
            decodeEnvelope(reader, request, true, messageInfo);
            convertBodyBlock(request, messageInfo);
        } catch(Exception e) {
            throw new ServerRtException("soapdecoder.err", new Object[]{e});
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return request;
    }

    /*
     * 
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public void decode(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    /*
     * 
     * @see com.sun.pept.encoding.Decoder#receiveAndDecode(com.sun.pept.ept.MessageInfo)
     */
    public void receiveAndDecode(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }
    
    public void decodeDispatchMethod(XMLReader reader, InternalMessage request, MessageInfo messageInfo) {
        // Operation's QName. takes care of <body/>
        QName name = (reader.getState() == XMLReader.START) ? reader.getName() : null;
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        Method method = rtCtxt.getDispatchMethod(name, messageInfo);
        if (method == null) {
            raiseFault(SOAPConstants.FAULT_CODE_CLIENT, "Cannot find the dispatch method");
        }
        messageInfo.setMethod(method);
    }
    
    protected SOAPFaultInfo decodeFault(XMLReader reader, InternalMessage internalMessage,
        MessageInfo messageInfo) {
        raiseFault(SOAPConstants.FAULT_CODE_CLIENT, "Server cannot handle fault message");
        return null;
    }
    
    /*
     * @throws ServerRtException using this any known error is thrown
     */
    private void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(faultCode, faultString, null, null);
    }
    
}
