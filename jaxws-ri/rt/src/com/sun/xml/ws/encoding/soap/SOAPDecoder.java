/*
 * $Id: SOAPDecoder.java,v 1.3 2005-05-25 19:05:50 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */

package com.sun.xml.ws.encoding.soap;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.stream.XMLStreamReader;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import com.sun.xml.ws.encoding.JAXRPCAttachmentUnmarshaller;
import com.sun.xml.ws.encoding.jaxb.*;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.MessageInfoUtil;

import static javax.xml.stream.XMLStreamReader.*;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.Source;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

/**
 * @author JAX-RPC RI Development Team
 */
public abstract class SOAPDecoder implements Decoder {

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public abstract void decode(MessageInfo arg0);

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#receieveAndDecode(com.sun.pept.ept.MessageInfo)
     */
    public abstract void receiveAndDecode(MessageInfo arg0);

    /**
     * parses and binds headers, body from SOAPMessage.
     * @param soapMessage
     * @return
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                    MessageInfo messageInfo) {
            return null;
    }
    
    /**
     * Parses and binds headers from SOAPMessage.
     * @param soapMessage
     * @param internalMesage
     *
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
            InternalMessage internalMessage, MessageInfo messageInfo) {
        return null;
    }

    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        return null;
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) { }

    protected QName getEnvelopeTag(){
        return SOAPConstants.QNAME_SOAP_ENVELOPE;
    }


    protected QName getBodyTag(){
        return SOAPConstants.QNAME_SOAP_BODY;
    }

    protected QName getHeaderTag(){
        return SOAPConstants.QNAME_SOAP_HEADER;
    }

    protected void skipBody(XMLStreamReader reader) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        XMLStreamReaderUtil.skipElement(reader);    // Moves to </Body>
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    /*
     * skipBody is true, the body is skipped during parsing.
     */
    protected void decodeEnvelope(XMLStreamReader reader, InternalMessage request,
            boolean skipBody, MessageInfo messageInfo) 
    {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        decodeHeader(reader, messageInfo, request);
        if (skipBody) {
            skipBody(reader);
        } else {
            decodeBody(reader, request, messageInfo);
        }
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_DOCUMENT);
    }
    
    protected void decodeHeader(XMLStreamReader reader, MessageInfo messageInfo,
        InternalMessage request) 
    {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;
        }
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        while (true) {
            if (reader.getEventType() == START_ELEMENT) {
                decodeHeaderElement(reader, messageInfo, request);
            } else {
                break;
            }
        }
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    /*
     * If JAXB can deserialize a header, deserialize it.
     * Otherwise, just ignore the header
     */
    private void decodeHeaderElement(XMLStreamReader reader, MessageInfo messageInfo,
        InternalMessage msg) 
    {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        Set<QName> knownHeaders = ((SOAPRuntimeModel)rtCtxt.getModel()).getKnownHeaders();
        QName name = reader.getName();
        if (knownHeaders != null && knownHeaders.contains(name)) {
            QName headerName = reader.getName();
            if (msg.isHeaderPresent(name)) {
                // More than one instance of header whose QName is mapped to a
                // method parameter. Generates a runtime error.
                raiseFault(SOAPConstants.FAULT_CODE_CLIENT, DUPLICATE_HEADER+headerName);
            }
            Object decoderInfo = rtCtxt.getDecoderInfo(name);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo)decoderInfo;
                // JAXB leaves on </env:Header> or <nextHeaderElement>
                JAXBTypeSerializer.getInstance().deserialize(reader, bridgeInfo, bridgeContext);
                HeaderBlock headerBlock = new HeaderBlock(bridgeInfo);

                //TODO remove after JAXB provides QName access thru Bridge
                headerBlock.setName(name);

                msg.addHeader(headerBlock);
            }
        } else {
            XMLStreamReaderUtil.skipElement(reader);           // Moves to END state
            XMLStreamReaderUtil.nextElementContent(reader);
        }
    }
    
    protected void decodeBody(XMLStreamReader reader, InternalMessage response, 
            MessageInfo messageInfo) 
    {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        int state = XMLStreamReaderUtil.nextElementContent(reader);
        decodeBodyContent(reader, response, messageInfo);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        XMLStreamReaderUtil.nextElementContent(reader);
    }
    
    protected void decodeBodyContent(XMLStreamReader reader, InternalMessage response, 
            MessageInfo messageInfo) 
    {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        decodeDispatchMethod(reader, response, messageInfo);
        if (reader.getEventType() == START_ELEMENT) {
            QName name = reader.getName(); // Operation name
            if (name.getNamespaceURI().equals(getEnvelopeTag().getNamespaceURI()) &&
                name.getLocalPart().equals(SOAPNamespaceConstants.TAG_FAULT)) {
                SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);
                BodyBlock responseBody = new BodyBlock(soapFaultInfo);
                response.setBody(responseBody);
            } else {
                Object decoderInfo = rtCtxt.getDecoderInfo(name);
                if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                    JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo)decoderInfo;
                    JAXBTypeSerializer.getInstance().deserialize(reader, bridgeInfo, bridgeContext);
                    BodyBlock responseBody = new BodyBlock(bridgeInfo);
                    response.setBody(responseBody);
                } else if (decoderInfo != null && decoderInfo instanceof RpcLitPayload) {
                    RpcLitPayload rpcLitPayload = (RpcLitPayload)decoderInfo;
                    RpcLitPayloadSerializer.deserialize(reader, rpcLitPayload, bridgeContext);
                    BodyBlock responseBody = new BodyBlock(rpcLitPayload);
                    response.setBody(responseBody);
                }
            }
        }
    }
    
    public void decodeDispatchMethod(XMLStreamReader reader, InternalMessage request, MessageInfo messageInfo) {
    }
    
    protected SOAPFaultInfo decodeFault(XMLStreamReader reader, InternalMessage internalMessage,
        MessageInfo messageInfo) {
        return null;
    }


    /*
    *
    */
   protected void convertBodyBlock(InternalMessage request, MessageInfo messageInfo) {
       BodyBlock bodyBlock = request.getBody();
       if (bodyBlock != null) {
           Object value = bodyBlock.getValue();
           if (value instanceof JAXBBeanInfo) {
               System.out.println("******* NOT HANDLED JAXBBeanInfo ***********");
           } else if (value instanceof JAXBBridgeInfo || value instanceof RpcLitPayload) {
               // Nothing to do
           } else if (value instanceof SOAPFaultInfo) {
               System.out.println("******* NOT HANDLED SOAPFaultInfo **********");
           } else if (value instanceof Source) {
               Source source = (Source)value;
               XMLStreamReader reader = SourceReaderFactory.createSourceReader(source, true);
               XMLStreamReaderUtil.nextElementContent(reader);
               decodeBodyContent(reader, request, messageInfo);
           } else {
               System.out.println("****** Unknown type in BodyBlock ***** "+value.getClass());
           }
       }
   }

    /**
     *
     * @param mi
     * @param im
     * @param message
     * @throws SOAPException
     * @throws ParseException
     */
    protected void processAttachments(MessageInfo mi, InternalMessage im, SOAPMessage message) throws SOAPException, ParseException, IOException {
        Iterator iter = message.getAttachments();
        if(iter.hasNext()){
            JAXRPCAttachmentUnmarshaller au = (JAXRPCAttachmentUnmarshaller) MessageInfoUtil.getRuntimeContext(mi).getBridgeContext().getAttachmentUnmarshaller();
            au.setXOPPackage(isXOPPackage(message));
            au.setXOPPackage(true);
            au.setAttachments(im.getAttachments());
        }

        while(iter.hasNext()){
            AttachmentPart ap = (AttachmentPart) iter.next();
            InputStream content = ap.getRawContent();
            String id = ap.getContentId();
            im.addAttachment(id, new AttachmentBlock(id, content, ap.getContentType()));
        }
    }

    /**
     * From the SOAP message header find out if its a XOP package.
     * @param sm
     * @return
     * @throws ParseException
     */
    private boolean isXOPPackage(SOAPMessage sm) throws ParseException {
        String ct = getContentType(sm.getMimeHeaders());
        ContentType contentType = new ContentType(ct);
        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();
        if(primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("xop+xml")){
            String type = contentType.getParameter("type");
            if(type.toLowerCase().startsWith("text/xml") || type.toLowerCase().startsWith("application/soap+xml"))
                return true;
        }
        return false;
    }

    private String getContentType(MimeHeaders headers) {
        String[] values = headers.getHeader("Content-Type");
        if (values == null)
            return null;
        else
            return values[0];
    }

    /*
     * @throws ServerRtException using this any known error is thrown
     */
    private void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(faultCode, faultString, null, null);
    }
    
    private final static String DUPLICATE_HEADER =
        "Duplicate Header in the message:";

}
