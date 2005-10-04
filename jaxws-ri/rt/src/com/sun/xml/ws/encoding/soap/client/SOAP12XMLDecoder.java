/**
 * $Id: SOAP12XMLDecoder.java,v 1.12 2005-10-04 00:44:01 kohsuke Exp $
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
package com.sun.xml.ws.encoding.soap.client;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.client.dispatch.impl.encoding.Dispatch12Serializer;
import com.sun.xml.ws.client.dispatch.impl.encoding.SerializerIF;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.FaultCode;
import com.sun.xml.ws.encoding.soap.message.FaultCodeEnum;
import com.sun.xml.ws.encoding.soap.message.FaultReason;
import com.sun.xml.ws.encoding.soap.message.FaultReasonText;
import com.sun.xml.ws.encoding.soap.message.FaultSubcode;
import com.sun.xml.ws.encoding.soap.message.SOAP12FaultInfo;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author WS Development Team
 */
public class SOAP12XMLDecoder extends SOAPXMLDecoder {

    //needs further cleanup
    private static final Logger logger =
        Logger.getLogger (new StringBuffer ().append (com.sun.xml.ws.util.Constants.LoggingDomain).append (".client.dispatch").toString ());

    public SOAP12XMLDecoder () {
    }

    protected SerializerIF getSerializerInstance (){
        return Dispatch12Serializer.getInstance ();
    }

    /*
     *
     * @see SOAPXMLDecoder#decodeFault(XMLStreamReader, InternalMessage, MessageInfo)
     */
    @Override
    protected SOAPFaultInfo decodeFault (XMLStreamReader reader, InternalMessage internalMessage, MessageInfo messageInfo) {
        RuntimeContext rtContext = MessageInfoUtil.getRuntimeContext (messageInfo);

        XMLStreamReaderUtil.verifyReaderState (reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_SOAP_FAULT);

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
        Object detail = null;

        QName name = reader.getName ();
        if(name.equals (SOAP12Constants.QNAME_FAULT_NODE)){
            node = reader.getText ();
        }

        if(name.equals (SOAP12Constants.QNAME_FAULT_ROLE)){
            XMLStreamReaderUtil.nextContent (reader);
            role = reader.getText ();
            XMLStreamReaderUtil.nextElementContent (reader);
            XMLStreamReaderUtil.nextElementContent (reader);
        }

        if(name.equals (SOAP12Constants.QNAME_FAULT_DETAIL)){
            //TODO: process encodingStyle attribute information item
            
            XMLStreamReaderUtil.nextElementContent (reader);
            detail = readFaultDetail (reader, messageInfo);

            XMLStreamReaderUtil.nextElementContent (reader);
        }

        XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent (reader);

        return new SOAP12FaultInfo (code, reason, node, role, detail);
    }

    protected QName readFaultValue (XMLStreamReader reader){
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

    protected FaultSubcode readFaultSubcode (XMLStreamReader reader){
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

    protected FaultReason readFaultReason (XMLStreamReader reader){
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

    protected void readFaultReasonTexts (XMLStreamReader reader, List<FaultReasonText> texts) {
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
        Locale loc = Locale.getDefault();
        if(lang != null)
            loc = new Locale(lang);

        texts.add (new FaultReasonText (text, loc));
        
        //call again to see if there are more soapenv:Text elements
        readFaultReasonTexts (reader, texts);
    }

    protected Object readFaultDetail (XMLStreamReader reader, MessageInfo mi){
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext (mi);
        QName faultName = reader.getName ();
        if (rtCtxt.getModel().isKnownFault (faultName, mi.getMethod ())) {
            Object decoderInfo = rtCtxt.getDecoderInfo (faultName);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                // JAXB leaves on </env:Header> or <nextHeaderElement>
                bridgeInfo.deserialize (reader, rtCtxt.getBridgeContext());
                XMLStreamReaderUtil.verifyReaderState (reader, END_ELEMENT);
                XMLStreamReaderUtil.verifyTag (reader, SOAP12Constants.QNAME_FAULT_DETAIL);
                return bridgeInfo;
            }
        }

        return decodeFaultDetail(reader);
    }         

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.soap.SOAPDecoder#decodeHeader(com.sun.xml.ws.streaming.XMLStreamReader, com.sun.pept.ept.MessageInfo, com.sun.xml.ws.soap.internal.InternalMessage)
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
                bridgeInfo.deserialize(reader,bridgeContext);
                HeaderBlock headerBlock = new HeaderBlock (bridgeInfo);
                msg.addHeader (headerBlock);
            }
        } else {
            XMLStreamReaderUtil.skipElement (reader);                 // Moves to END state
            XMLStreamReaderUtil.nextElementContent (reader);
        }
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.soap.SOAPDecoder#getFaultTag()
     */
    @Override
    protected QName getFaultTag (){
        return SOAP12Constants.QNAME_SOAP_FAULT;
    }
    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.soap.SOAPDecoder#getBodyTag()
     */
    @Override
    protected QName getBodyTag () {
        return SOAP12Constants.QNAME_SOAP_BODY;
    }

    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.soap.SOAPDecoder#getEnvelopeTag()
     */
    @Override
    protected QName getEnvelopeTag () {
        return SOAP12Constants.QNAME_SOAP_ENVELOPE;
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.ws.rt.encoding.soap.SOAPDecoder#getHeaderTag()
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

    @Override
    protected QName getFaultDetailTag() {
        return SOAP12Constants.QNAME_FAULT_DETAIL;
    }

    @Override
    public String getBindingId() {
        return SOAPBinding.SOAP12HTTP_BINDING;
    }

}
