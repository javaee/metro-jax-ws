/*
 * $Id: DelegateBase.java,v 1.2 2005-05-25 18:22:12 kohlert Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap.internal;

import com.sun.pept.Delegate;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.ContactInfo;
import com.sun.pept.ept.ContactInfoList;
import com.sun.pept.ept.ContactInfoListIterator;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.ContextMap;
import com.sun.xml.ws.client.BindingProviderProperties;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.util.Iterator;

/**
 * @author JAX-RPC RI Development Team
 */
public class DelegateBase implements Delegate {
    protected ContactInfoList contactInfoList;

    public DelegateBase() {
    }

    public DelegateBase(ContactInfoList contactInfoList) {
        this.contactInfoList = contactInfoList;
    }

    public MessageStruct getMessageStruct() {
        return new MessageInfoBase();
    }

    public void send(MessageStruct messageStruct) {
        MessageInfo messageInfo = (MessageInfo) messageStruct;

//        ContactInfoListIterator iterator = contactInfoList.iterator();
        if (!contactInfoList.iterator().hasNext())
            throw new RuntimeException("no next");
   //TODO: use new prop MAP-kw

        ContextMap properties = (ContextMap)
                messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        BindingProvider stub = (BindingProvider)properties.get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);
        //get handle property(the stub
        ContactInfo contactInfo = null;
        Encoder encoder = null;
        Decoder decoder = null;

        if (properties != null) {
            for (Iterator names = properties.getPropertyNames(); names.hasNext();) {
                String propName = (String)names.next();
                if (propName.equals(BindingProviderProperties.XMLFAST_ENCODING_PROPERTY)) {
                    String encoding = (String) properties.get(BindingProviderProperties.XMLFAST_ENCODING_PROPERTY);
//                    checkEncoderDecoderPair(encoding, iterator, messageInfo, contactInfo);
                    encoder = getEncoder(encoding, contactInfoList.iterator(), messageInfo, contactInfo);
                } else if (propName.equals(BindingProviderProperties.ACCEPT_ENCODING_PROPERTY)) {
                    String encoding = (String) properties.get(BindingProviderProperties.ACCEPT_ENCODING_PROPERTY);
                    decoder = getDecoder(encoding, contactInfoList.iterator(), messageInfo, contactInfo);
                }
            }
        }

        if (contactInfo == null)
            contactInfo = (ContactInfo) contactInfoList.iterator().next();

        // If XMLFAST_ENCODING_PROPERTY is not set in the client
        // pick the Encoder from the first ContactIffo
        if (encoder == null)
            encoder = contactInfo.getEncoder(messageInfo);

        // If ACCEPT_ENCODING_PROPERTY is not set in the client
        // pick the Decoder from the first ContactIffo
        if (decoder == null)
            decoder = contactInfo.getDecoder(messageInfo);

        // TODO: choose the decoder based upon Content-Type
        // TODO: Update the logic after discussion on content negotiation
        messageInfo.setEPTFactory(contactInfo);
//        messageInfo.setEncoder(encoder);
//        messageInfo.setDecoder(decoder);
//        messageInfo.setMessageDispatcher(contactInfo.getMessageDispatcher(messageInfo));

        MessageDispatcher messageDispatcher =
                contactInfo.getMessageDispatcher(messageInfo);
        messageDispatcher.send(messageInfo);
        //stub.updateResponseContext(messageInfo);

    }

//    boolean checkEncoderDecoderPair(String encoding, ContactInfoListIterator iterator, MessageInfo messageInfo, ContactInfo contactInfo) {
//        boolean codersFound = false;
//        while (iterator.hasNext()) {
//            contactInfo = (ContactInfo) iterator.next();
//            if (encoding.equals(FAST_ENCODING_VALUE) &&
//                    (contactInfo.getEncoder(messageInfo) instanceof com.sun.xml.rpc.fast.client.SOAPFastEncoder) &&
//                    (contactInfo.getDecoder(messageInfo) instanceof com.sun.xml.rpc.fast.client.SOAPFastDecoder)) {
//                codersFound = true;
//                break;
//            }
//            if (encoding.equals(XML_ENCODING_VALUE) &&
//                    (contactInfo.getEncoder(messageInfo) instanceof com.sun.xml.rpc.client.SOAPXMLEncoder) &&
//                    (contactInfo.getDecoder(messageInfo) instanceof com.sun.xml.rpc.client.SOAPXMLDecoder)) {
//                codersFound = true;
//                break;
//            }
//        }
//        if (!codersFound)
//            if (encoding.equals(FAST_ENCODING_VALUE))
//                throw new JAXRPCException("Invalid Fast encoder/decoder");
//            else
//                throw new JAXRPCException("Invalid XML encoder/decoder");
//
//        return false;
//    }

    Encoder getEncoder(String encoding, ContactInfoListIterator iterator, MessageInfo messageInfo, ContactInfo contactInfo) {
        Encoder encoder = null;

        boolean encoderFound = false;
        while (iterator.hasNext()) {
            contactInfo = (ContactInfo) iterator.next();
//            if (encoding.equals(FAST_ENCODING_VALUE) &&
//                    ((encoder = contactInfo.getEncoder(messageInfo)) instanceof com.sun.xml.rpc.fast.client.SOAPFastEncoder)) {
//                encoderFound = true;
//                break;
//            }
            if (encoding.equals(BindingProviderProperties.XML_ENCODING_VALUE) &&
                    ((encoder = contactInfo.getEncoder(messageInfo)) instanceof com.sun.xml.ws.client.SOAPXMLEncoder)) {
                encoderFound = true;
                break;
            }
        }
        if (!encoderFound)
            if (encoding.equals(BindingProviderProperties.FAST_ENCODING_VALUE))
                throw new WebServiceException("Invalid Fast encoder: " + encoder.getClass().getName());
            else
                throw new WebServiceException("Invalid XML encoder: " + encoder.getClass().getName());

        return encoder;
    }

    Decoder getDecoder(String encoding, ContactInfoListIterator iterator, MessageInfo messageInfo, ContactInfo contactInfo) {
        Decoder decoder = null;

        boolean decoderFound = false;
        while (iterator.hasNext()) {
            contactInfo = (ContactInfo) iterator.next();
//            if ((encoding.indexOf(FAST_ENCODING_VALUE) != -1) &&
//                    ((decoder = contactInfo.getDecoder(messageInfo)) instanceof com.sun.xml.rpc.fast.client.SOAPFastDecoder)) {
//                decoderFound = true;
//                break;
//            }
            if ((encoding.indexOf(BindingProviderProperties.XML_ENCODING_VALUE) != -1) &&
                    ((decoder = contactInfo.getDecoder(messageInfo)) instanceof com.sun.xml.ws.client.SOAPXMLDecoder)) {
                decoderFound = true;
                break;
            }
        }
        if (!decoderFound)
            if (encoding.equals(BindingProviderProperties.FAST_ENCODING_VALUE))
                throw new WebServiceException("Invalid Fast decoder: " + decoder.getClass().getName());
            else
                throw new WebServiceException("Invalid XML decoder: " + decoder.getClass().getName());

        return decoder;
    }

}