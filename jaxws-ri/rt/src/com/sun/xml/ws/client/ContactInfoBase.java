/*
 * $Id: ContactInfoBase.java,v 1.3 2005-07-18 16:52:05 kohlert Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.ContactInfo;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.protocol.Interceptors;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.transport.Connection;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalDecoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
import com.sun.xml.ws.encoding.soap.ClientEncoderDecoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.server.LogicalEncoderImpl;

import javax.xml.ws.soap.SOAPBinding;

/**
 * @author WS Development Team
 */
public class ContactInfoBase implements ContactInfo, LogicalEPTFactory {

    protected Connection _connection;
    protected MessageDispatcher _messageDispatcher;
    protected Encoder _encoder;
    protected Decoder _decoder;
    private String bindingId;

    public ContactInfoBase(Connection connection,
                           MessageDispatcher messageDispatcher,
                           Encoder encoder,
                           Decoder decoder, String bindingId) {
        _connection = connection;
        _messageDispatcher = messageDispatcher;
        _encoder = encoder;
        _decoder = decoder;
        this.bindingId = bindingId;
    }

    public ContactInfoBase() {
        _connection = null;
        _messageDispatcher = null;
        _encoder = null;
        _decoder = null;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.ContactInfo#getConnection(com.sun.pept.ept.MessageInfo)
     */
    public Connection getConnection(MessageInfo arg0) {
        return _connection;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.EPTFactory#getMessageDispatcher(com.sun.pept.ept.MessageInfo)
     */
    public MessageDispatcher getMessageDispatcher(MessageInfo arg0) {
        return _messageDispatcher;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.EPTFactory#getEncoder(com.sun.pept.ept.MessageInfo)
     */
    public Encoder getEncoder(MessageInfo arg0) {
        return _encoder;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.EPTFactory#getDecoder(com.sun.pept.ept.MessageInfo)
     */
    public Decoder getDecoder(MessageInfo arg0) {
        return _decoder;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.EPTFactory#getInterceptors(com.sun.pept.ept.MessageInfo)
     */
    public Interceptors getInterceptors(MessageInfo arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.ept.EPTFactory#getTargetFinder(com.sun.pept.ept.MessageInfo)
     */
    public TargetFinder getTargetFinder(MessageInfo arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public LogicalEncoder getLogicalEncoder() {
        //TODO use only one instance of LogicalEncoderImpl
        return new LogicalEncoderImpl();
    }

    public LogicalDecoder getLogicalDecoder() {
        //TODO Auto-generated method stub
        return null;
    }

    public SOAPEncoder getSOAPEncoder() {
        return (SOAPEncoder) _encoder;
    }

    public SOAPDecoder getSOAPDecoder() {
        return (SOAPDecoder) _decoder;
    }

    public InternalEncoder getInternalEncoder() {
        return new ClientEncoderDecoder();
    }

    public String getBindingId(){
        if(bindingId == null)
            return SOAPBinding.SOAP11HTTP_BINDING;
        return bindingId;
    }
}
