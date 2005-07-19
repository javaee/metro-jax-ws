/**
 * $Id: SOAP12XMLDecoder.java,v 1.1 2005-07-19 20:41:23 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap.client;

import static com.sun.pept.presentation.MessageStruct.UNCHECKED_EXCEPTION_RESPONSE;

import javax.xml.stream.XMLStreamReader;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.message.*;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.client.dispatch.impl.encoding.Dispatch12Serializer;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchSerializer;
import com.sun.xml.ws.client.dispatch.impl.encoding.SerializerIF;
import com.sun.xml.ws.client.dispatch.DispatchContext;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.Service;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.xml.sax.XMLReader;
import com.sun.xml.ws.encoding.soap.*;

public class SOAP12XMLDecoder extends SOAPXMLDecoder {
    
    //needs further cleanup
    private static final Logger logger =
        Logger.getLogger (new StringBuffer ().append (com.sun.xml.ws.util.Constants.LoggingDomain).append (".client.dispatch").toString ());
    
    //jaxbcontext can not be static
    private JAXBContext jc;
    
    public SOAP12XMLDecoder () {
    }
    
    protected SerializerIF getSerializerInstance (){
        return Dispatch12Serializer.getInstance ();
    }
    
    /*
     * TODO need to add more logic and processing
     * @see com.sun.xml.rpc.rt.client.SOAPXMLDecoder#decodeFault(com.sun.xml.rpc.streaming.XMLStreamReader, com.sun.xml.rpc.soap.internal.InternalMessage, com.sun.pept.ept.MessageInfo)
     */
    @Override
        protected SOAPFaultInfo decodeFault (XMLStreamReader reader, InternalMessage internalMessage, MessageInfo messageInfo) {
        RuntimeContext rtContext = MessageInfoUtil.getRuntimeContext (messageInfo);
        
        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_SOAP_FAULT);
        Method methodName = messageInfo.getMethod ();
        
        // env:Code
        XMLStreamReaderUtil.nextElementContent (reader);
        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_CODE);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        //env:Value
        QName faultcode = readFaultValue (reader);
        FaultCodeEnum codeValue = FaultCodeEnum.get (faultcode);
        if(codeValue == null)
            throw new DeserializationException ("unknown fault code:", faultcode.toString ());
        
        
        //Subcode
        FaultSubcode subcode = null;
        if(reader.getEventType () == START_ELEMENT)
            subcode = readFaultSubcode (reader);
        FaultCode code = new FaultCode (codeValue, subcode);
        
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_CODE);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        FaultReason reason = readFaultReason (reader);
        String node = null;
        String role = null;
        List details = new ArrayList ();
        
        QName name = reader.getName ();
        if(name.equals (SOAP12Constants.QNAME_FAULT_NODE)){
            
        }
        
        if(name.equals (SOAP12Constants.QNAME_FAULT_ROLE)){
            
        }
        
        if(name.equals (SOAP12Constants.QNAME_FAULT_DETAIL)){
            //TODO: process encodingStyle attribute information item
            
            XMLStreamReaderUtil.nextElementContent (reader);
            readFaultDetail (reader, messageInfo, details);
            XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
            XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_DETAIL);
            XMLStreamReaderUtil.nextElementContent (reader);
        }
        
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        BodyBlock responseBody = new BodyBlock (new SOAP12FaultInfo (code, reason, node, role, details));
        internalMessage.setBody (responseBody);
        
        //return null for now as we have already set the fault in the body
        return null;
    }
    
    private QName readFaultValue (XMLStreamReader reader){
        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_VALUE);
        
        XMLStreamReaderUtil.nextContent (reader);
        
        String tokens = reader.getText ();
        
        XMLStreamReaderUtil.next (reader);
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_VALUE);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        String uri = "";
        tokens = EncoderUtils.collapseWhitespace (tokens);
        String prefix = XmlUtil.getPrefix (tokens);
        if (prefix != null) {
            uri = reader.getNamespaceURI (prefix);
            if (uri == null) {
                throw new DeserializationException ("xsd.unknownPrefix", prefix);
            }
        }
        String localPart = XmlUtil.getLocalPart (tokens);
        return new QName (uri, localPart);
    }
    
    private FaultSubcode readFaultSubcode (XMLStreamReader reader){
        FaultSubcode code = null;
        QName name = reader.getName ();
        if(name.equals (SOAP12Constants.QNAME_FAULT_SUBCODE)){
            XMLStreamReaderUtil.nextElementContent (reader);
            QName faultcode = readFaultValue (reader);
            FaultSubcode subcode = null;
            if(reader.getEventType () == START_ELEMENT)
                subcode = readFaultSubcode (reader);
            code = new FaultSubcode (faultcode, subcode);
            XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
            XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_SUBCODE);
            XMLStreamReaderUtil.nextElementContent (reader);
        }
        return code;
    }
    
    private FaultReason readFaultReason (XMLStreamReader reader){
        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_REASON);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        //soapenv:Text
        List<FaultReasonText> texts = new ArrayList<FaultReasonText>();
        readFaultReasonTexts (reader, texts);
        
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_REASON);
        XMLStreamReaderUtil.nextElementContent (reader);
        
        FaultReasonText[] frt = texts.toArray (new FaultReasonText[0]);
        return new FaultReason (frt);
    }
    
    private void readFaultReasonTexts (XMLStreamReader reader, List<FaultReasonText> texts) {
        QName name = reader.getName ();
        if (!name.equals (SOAP12Constants.QNAME_FAULT_REASON_TEXT)) {
            return;
        }
        String lang = reader.getAttributeValue (SOAP12NamespaceConstants.XML_NS, "lang");
        //lets be more forgiving, if its null lets assume its 'en'
        if(lang == null)
            lang = "en";
        
        //TODO: what to do when the lang is other than 'en', for example clingon?
        
        //get the text value
        XMLStreamReaderUtil.nextContent (reader);
        String text = null;
        if (reader.getEventType () == CHARACTERS) {
            text = reader.getText ();
            XMLStreamReaderUtil.next (reader);
        }
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_REASON_TEXT);
        XMLStreamReaderUtil.nextElementContent (reader);
        texts.add (new FaultReasonText (lang, text));
        
        //call again to see if there are more soapenv:Text elements
        readFaultReasonTexts (reader, texts);
    }
    
    private void readFaultDetail (XMLStreamReader reader, MessageInfo mi, List details){
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext (mi);
        QName faultName = reader.getName ();
        if (((SOAPRuntimeModel) rtCtxt.getModel ()).isKnownFault (faultName, mi.getMethod ())) {
            Object decoderInfo = rtCtxt.getDecoderInfo (faultName);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                // JAXB leaves on </env:Header> or <nextHeaderElement>
                JAXBTypeSerializer.getInstance ().deserialize (reader, bridgeInfo,
                    rtCtxt.getBridgeContext ());
                details.add (bridgeInfo);
            }
        }
        //TODO: process rest of the Detail entries
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#decodeHeader(com.sun.xml.rpc.streaming.XMLStreamReader, com.sun.pept.ept.MessageInfo, com.sun.xml.rpc.soap.internal.InternalMessage)
     */
    @Override
        protected void decodeHeader (XMLStreamReader reader, MessageInfo messageInfo, InternalMessage request) {
        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        if (!SOAPNamespaceConstants.TAG_HEADER.equals (reader.getLocalName ())) {
            return;
        }
        XMLStreamReaderUtil.verifyTag (reader, getHeaderTag ());
        XMLStreamReaderUtil.nextElementContent (reader);
        while (true) {
            if (reader.getEventType () == START_ELEMENT) {
                decodeHeaderElement (reader, messageInfo, request);
            } else {
                break;
            }
        }
        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, getHeaderTag ());
        XMLStreamReaderUtil.nextElementContent (reader);
    }
    
    /*
     * If JAXB can deserialize a header, deserialize it.
     * Otherwise, just ignore the header
     */
    protected void decodeHeaderElement (XMLStreamReader reader, MessageInfo messageInfo,
        InternalMessage msg) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext (messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext ();
        Set<QName> knownHeaders = ((SOAPRuntimeModel) rtCtxt.getModel ()).getKnownHeaders ();
        QName name = reader.getName ();
        if (knownHeaders != null && knownHeaders.contains (name)) {
            QName headerName = reader.getName ();
            if (msg.isHeaderPresent (name)) {
                // More than one instance of header whose QName is mapped to a
                // method parameter. Generates a runtime error.
                raiseFault (SOAP12Constants.FAULT_CODE_CLIENT, "Duplicate Header" + headerName);
            }
            Object decoderInfo = rtCtxt.getDecoderInfo (name);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                // JAXB leaves on </env:Header> or <nextHeaderElement>
                JAXBTypeSerializer.getInstance ().deserialize (reader, bridgeInfo, bridgeContext);
                HeaderBlock headerBlock = new HeaderBlock (bridgeInfo);
                msg.addHeader (headerBlock);
            }
        } else {
            XMLStreamReaderUtil.skipElement (reader);                 // Moves to END state
            XMLStreamReaderUtil.nextElementContent (reader);
        }
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getFaultTag()
     */
    @Override
        protected QName getFaultTag (){
        return SOAP12Constants.QNAME_SOAP_FAULT;
    }
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getBodyTag()
     */
    @Override
        protected QName getBodyTag () {
        return SOAP12Constants.QNAME_SOAP_BODY;
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getEnvelopeTag()
     */
    @Override
        protected QName getEnvelopeTag () {
        return SOAP12Constants.QNAME_SOAP_ENVELOPE;
    }
    
//    /* (non-Javadoc)
//     * @see com.sun.xml.rpc.rt.client.SOAPXMLDecoder#toSOAPMessage(com.sun.pept.ept.MessageInfo)
//     */
//    @Override
//    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
//        SOAPConnection connection = (SOAPConnection) messageInfo.getConnection();
//        SOAPMessage sm = connection.getSOAPMessage(messageInfo);
//
//        return sm;
//    }
//
    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPDecoder#getHeaderTag()
     */
    @Override
        protected QName getHeaderTag () {
        return SOAP12Constants.QNAME_SOAP_HEADER;
    }
    
    @Override
        protected QName getMUAttrQName (){
        return SOAP12Constants.QNAME_MUSTUNDERSTAND;
    }
    
    @Override
        protected QName getRoleAttrQName (){
        return SOAP12Constants.QNAME_ROLE;
    }
    
}
