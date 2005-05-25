/**
 * $Id: SOAP12XMLDecoder.java,v 1.2 2005-05-25 19:05:47 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import javax.xml.stream.XMLStreamReader;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import java.lang.reflect.Method;
import java.util.Set;
import org.xml.sax.XMLReader;

public class SOAP12XMLDecoder extends SOAPXMLDecoder {

    /*
     * TODO need to add more logic and processing
     * @see com.sun.xml.rpc.rt.client.SOAPXMLDecoder#decodeFault(com.sun.xml.rpc.streaming.XMLStreamReader, com.sun.xml.rpc.soap.internal.InternalMessage, com.sun.pept.ept.MessageInfo)
     */
    @Override
    protected SOAPFaultInfo decodeFault(XMLStreamReader reader, InternalMessage internalMessage, MessageInfo messageInfo) {
        RuntimeContext rtContext = MessageInfoUtil.getRuntimeContext(messageInfo);

        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAP12Constants.QNAME_SOAP_FAULT);
        Method methodName = messageInfo.getMethod();

        // env:Code
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAP12Constants.QNAME_FAULT_CODE);
        
        //env:Value
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAP12Constants.QNAME_FAULT_VALUE);

        XMLStreamReaderUtil.nextContent(reader);
        
        QName faultcode = null;
        String tokens = reader.getText();
        String uri = "";
        tokens = EncoderUtils.collapseWhitespace(tokens);
        String prefix = XmlUtil.getPrefix(tokens);
        if (prefix != null) {
            uri = reader.getNamespaceURI(prefix);
            if (uri == null) {
                throw new DeserializationException("xsd.unknownPrefix", prefix);
            }
        }
        String localPart = XmlUtil.getLocalPart(tokens);
        faultcode = new QName(uri, localPart);
        XMLStreamReaderUtil.next(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        
        //TODO: process env:Subcode
        XMLStreamReaderUtil.verifyTag(reader, SOAP12Constants.QNAME_FAULT_CODE);

        //TODO: env:Reason
        //TODO: one or more env:Text
        //TODO: optional env:Node, env:Role and env:Detail
        String faultactor = null;
        Object faultdetail = null;
        QName faultName = null;
        String faultstring = null;
        //SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);
        SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);

        // reader could be left on CHARS token rather than </fault>
        if (reader.getEventType() == CHARACTERS && reader.isWhiteSpace()) {
            XMLStreamReaderUtil.nextContent(reader);
        }

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent(reader);

        return soapFaultInfo;
    }


    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#decodeHeader(com.sun.xml.rpc.streaming.XMLStreamReader, com.sun.pept.ept.MessageInfo, com.sun.xml.rpc.soap.internal.InternalMessage)
     */
    @Override
    protected void decodeHeader(XMLStreamReader reader, MessageInfo messageInfo, InternalMessage request) {
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
        Set<QName> knownHeaders = ((SOAPRuntimeModel) rtCtxt.getModel()).getKnownHeaders();
        QName name = reader.getName();
        if (knownHeaders != null && knownHeaders.contains(name)) {
            QName headerName = reader.getName();
            if (msg.isHeaderPresent(name)) {
                // More than one instance of header whose QName is mapped to a
                // method parameter. Generates a runtime error.
                raiseFault(SOAP12Constants.FAULT_CODE_CLIENT, "Duplicate Header" + headerName);
            }
            Object decoderInfo = rtCtxt.getDecoderInfo(name);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                // JAXB leaves on </env:Header> or <nextHeaderElement>
                JAXBTypeSerializer.getInstance().deserialize(reader, bridgeInfo, bridgeContext);
                HeaderBlock headerBlock = new HeaderBlock(bridgeInfo);

                //TODO remove after JAXB provides QName access thru Bridge
                headerBlock.setName(name);

                msg.addHeader(headerBlock);
            }
        } else {
            XMLStreamReaderUtil.skipElement(reader);                 // Moves to END state
            XMLStreamReaderUtil.nextElementContent(reader);
        }
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getBodyTag()
     */
    @Override
    protected QName getBodyTag() {
        return SOAP12Constants.QNAME_SOAP_BODY;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getEnvelopeTag()
     */
    @Override
    protected QName getEnvelopeTag() {
        return SOAP12Constants.QNAME_SOAP_ENVELOPE;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.client.SOAPXMLDecoder#toSOAPMessage(com.sun.pept.ept.MessageInfo)
     */
    @Override
    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        SOAPConnection connection = (SOAPConnection) messageInfo.getConnection();
        SOAPMessage sm = connection.getSOAPMessage(messageInfo);

        return sm;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getHeaderTag()
     */
    @Override
    protected QName getHeaderTag() {
        return SOAP12Constants.QNAME_SOAP_HEADER;
    }

}
