/**
 * $Id: XMLEPTFactoryImpl.java,v 1.2 2005-08-29 19:37:31 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.protocol.Interceptors;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalDecoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
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
    private LogicalEncoder logicalEncoder;
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
        LogicalEncoder logicalEncoder, InternalEncoder internalEncoder,
        TargetFinder targetFinder, MessageDispatcher messageDispatcher) {
        this.xmlEncoder = xmlEncoder;
        this.xmlDecoder = xmlDecoder;
        this.encoder = null;
        this.decoder = null;
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
     * @see com.sun.istack.pept.ept.EPTFactory#getInterceptors(com.sun.istack.pept.ept.MessageInfo)
     */
    public Interceptors getInterceptors(MessageInfo x) {
        return null;
    }

    /*
     * @see com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory#getLogicalEncoder()
     */
    public LogicalEncoder getLogicalEncoder() {
        return logicalEncoder;
    }

    /*
     * @see com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory#getLogicalDecoder()
     */
    public LogicalDecoder getLogicalDecoder() {
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
