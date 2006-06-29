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
package com.sun.xml.ws.server;

import com.sun.xml.ws.pept.encoding.Decoder;
import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.ept.EPTFactory;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.TargetFinder;
import com.sun.xml.ws.pept.protocol.Interceptors;
import com.sun.xml.ws.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

/**
 * @author WS Development Team
 */
public class EPTFactoryBase implements EPTFactory, SOAPEPTFactory {
    private Encoder encoder;
    private Decoder decoder;
    private SOAPEncoder soapEncoder;
    private SOAPDecoder soapDecoder;
    private InternalEncoder internalEncoder;
    private TargetFinder targetFinder;
    private MessageDispatcher messageDispatcher;

    public EPTFactoryBase(Encoder encoder, Decoder decoder,
            TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.targetFinder = targetFinder;
        this.messageDispatcher = messageDispatcher;
    }
    
    public EPTFactoryBase(SOAPEncoder soapEncoder, SOAPDecoder soapDecoder,
                          InternalEncoder internalEncoder,
                          TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.encoder = null;
        this.decoder = null;
        this.soapEncoder = soapEncoder;
        this.soapDecoder = soapDecoder;
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
     * @see EPTFactory#getInterceptors(com.sun.istack.pept.ept.MessageInfo)
     */
    public Interceptors getInterceptors(MessageInfo x) {
        return null;
    }

    /*
     * @see LogicalEPTFactory#getSoapEncoder()
     */
    public SOAPEncoder getSOAPEncoder() {
        return soapEncoder;
    }

    /*
     * @see LogicalEPTFactory#getSoapDecoder()
     */
    public SOAPDecoder getSOAPDecoder() {
        return soapDecoder;
    }

    public InternalEncoder getInternalEncoder() {
        return internalEncoder;
    }


}
