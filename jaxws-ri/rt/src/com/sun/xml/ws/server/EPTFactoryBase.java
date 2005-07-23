/**
 * $Id: EPTFactoryBase.java,v 1.3 2005-07-23 04:10:11 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.protocol.Interceptors;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalDecoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.ServerEncoderDecoder;

/**
 * @author WS Development Team
 */
public class EPTFactoryBase implements EPTFactory, LogicalEPTFactory {
    private Encoder encoder;
    private Decoder decoder;
    private SOAPEncoder soapEncoder;
    private SOAPDecoder soapDecoder;
    private LogicalEncoder logicalEncoder;
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
        LogicalEncoder logicalEncoder, InternalEncoder internalEncoder,
        TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.encoder = null;
        this.decoder = null;
        this.soapEncoder = soapEncoder;
        this.soapDecoder = soapDecoder;
        this.logicalEncoder = logicalEncoder;
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
     * @see LogicalEPTFactory#getLogicalEncoder()
     */
    public LogicalEncoder getLogicalEncoder() {
        return logicalEncoder;
    }

    /*
     * @see LogicalEPTFactory#getLogicalDecoder()
     */
    public LogicalDecoder getLogicalDecoder() {
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
