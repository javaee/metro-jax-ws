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
package com.sun.xml.ws.server;

import com.sun.xml.ws.pept.encoding.Decoder;
import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.TargetFinder;
import com.sun.xml.ws.pept.protocol.Interceptors;
import com.sun.xml.ws.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.xml.XMLDecoder;
import com.sun.xml.ws.encoding.xml.XMLEPTFactory;
import com.sun.xml.ws.encoding.xml.XMLEncoder;

/**
 * @author WS Development Team
 */
public class XMLEPTFactoryImpl implements XMLEPTFactory {
    private Encoder encoder;
    private Decoder decoder;
    private XMLEncoder xmlEncoder;
    private XMLDecoder xmlDecoder;
    private InternalEncoder internalEncoder;
    private TargetFinder targetFinder;
    private MessageDispatcher messageDispatcher;

    public XMLEPTFactoryImpl(Encoder encoder, Decoder decoder,
            TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.targetFinder = targetFinder;
        this.messageDispatcher = messageDispatcher;
    }
    
    public XMLEPTFactoryImpl(XMLEncoder xmlEncoder, XMLDecoder xmlDecoder,
                             InternalEncoder internalEncoder,
                             TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.xmlEncoder = xmlEncoder;
        this.xmlDecoder = xmlDecoder;
        this.encoder = null;
        this.decoder = null;
        this.internalEncoder = internalEncoder;
        this.targetFinder = targetFinder;
        this.messageDispatcher = messageDispatcher;
    }

    public Encoder getEncoder(MessageInfo messageInfo) {
        messageInfo.setEncoder(encoder);
        return messageInfo.getEncoder();
    }

    public Decoder getDecoder(MessageInfo messageInfo) {
        messageInfo.setDecoder(decoder);
        return messageInfo.getDecoder();
    }

    public TargetFinder getTargetFinder(MessageInfo messageInfo) {
        return targetFinder;
    }

    public MessageDispatcher getMessageDispatcher(MessageInfo messageInfo) {
        messageInfo.setMessageDispatcher(messageDispatcher);
        return messageDispatcher;
    }

    /*
     * @see com.sun.istack.pept.ept.EPTFactory#getInterceptors(com.sun.istack.pept.ept.MessageInfo)
     */
    public Interceptors getInterceptors(MessageInfo x) {
        return null;
    }

    /*
     * @see com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory#getSoapEncoder()
     */
    public XMLEncoder getXMLEncoder() {
        return xmlEncoder;
    }

    /*
     * @see com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory#getSoapDecoder()
     */
    public XMLDecoder getXMLDecoder() {
        return xmlDecoder;
    }

    public InternalEncoder getInternalEncoder() {
        return internalEncoder;
    }


}
