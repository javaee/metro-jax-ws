/*
 * $Id: MessageInfoBase.java,v 1.2 2005-06-16 13:43:53 vivekp Exp $
 */

/*
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.soap.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.sun.pept.encoding.Decoder;
import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.pept.transport.Connection;

/**
 * @author JAX-RPC RI Development Team
 */
public class MessageInfoBase implements MessageInfo {

    protected Object[] _data;
    protected Method _method;
    protected Map _metadata;
    protected int _messagePattern;
    protected Object _response;
    protected int _responseType;
    protected EPTFactory _eptFactory;
    protected MessageDispatcher _messageDispatcher;
    protected Encoder _encoder;
    protected Decoder _decoder;
    protected Connection _connection;

    public void setData(Object[] data) {
        _data = data;
    }

    public Object[] getData() {
        return _data;
    }

    public void setMethod(Method method) {
        _method = method;
    }

    public Method getMethod() {
        return _method;
    }

    public void setMetaData(Object name, Object value) {
        if (_metadata == null)
           _metadata = new HashMap();
        _metadata.put(name, value);
    }

    public Object getMetaData(Object name) {
        Object value = null;

        if ((name != null) && (_metadata != null)) {
             value = _metadata.get(name);
        }
        return value;
    }

    public int getMEP() {
        return _messagePattern;
    }

    public void setMEP(int messagePattern) {
        _messagePattern = messagePattern;
    }

    public int getResponseType() {
        return _responseType;
    }

    public void setResponseType(int responseType) {
        _responseType = responseType;
    }

    public Object getResponse() {
        return _response;
    }

    public void setResponse(Object response) {
        _response = response;
    }

    public EPTFactory getEPTFactory() {
        return _eptFactory;
    }

    public void setEPTFactory(EPTFactory eptFactory) {
        _eptFactory = eptFactory;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#getMessageDispatcher()
     */
    public MessageDispatcher getMessageDispatcher() {
        return _messageDispatcher;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#getEncoder()
     */
    public Encoder getEncoder() {
        return _encoder;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#getDecoder()
     */
    public Decoder getDecoder() {
        return _decoder;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#getConnection()
     */
    public Connection getConnection() {
        return _connection;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#setMessageDispatcher(com.sun.pept.protocol.MessageDispatcher)
     */
    public void setMessageDispatcher(MessageDispatcher arg0) {
        this._messageDispatcher = arg0;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#setEncoder(com.sun.pept.encoding.Encoder)
     */
    public void setEncoder(Encoder encoder) {
        this._encoder = encoder;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#setDecoder(com.sun.pept.encoding.Decoder)
     */
    public void setDecoder(Decoder decoder) {
        this._decoder = decoder;
    }

    /*
     * @see com.sun.pept.ept.MessageInfo#setConnection(com.sun.pept.transport.Connection)
     */
    public void setConnection(Connection connection) {
        this._connection = connection;
    }

    public static MessageInfo copy(MessageInfo mi){
        MessageInfoBase mib = (MessageInfoBase)mi;
        MessageInfoBase newMi = new MessageInfoBase();
        if(newMi._data != null){
            Object[] data = new Object[mib._data.length];
            int i = 0;
            for(Object o : mib._data){
                data[i++] = o;
            }
            newMi._data = data;
        }
        newMi.setConnection(mi.getConnection());
        newMi.setMethod(mi.getMethod());
        newMi.setDecoder(mi.getDecoder());
        newMi.setEncoder(mi.getEncoder());
        newMi.setEPTFactory(mi.getEPTFactory());
        newMi.setMEP(mi.getMEP());
        newMi._messageDispatcher = mib._messageDispatcher;
        newMi._metadata = new HashMap(mib._metadata);
        return (MessageInfo)newMi;
    }
}
