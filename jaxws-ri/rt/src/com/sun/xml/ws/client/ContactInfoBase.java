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
import com.sun.xml.ws.encoding.soap.ClientEncoderDecoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

import javax.xml.ws.soap.SOAPBinding;


/**
 * @author WS Development Team
 */
public class ContactInfoBase implements ContactInfo, SOAPEPTFactory {
    protected Connection _connection;
    protected MessageDispatcher _messageDispatcher;
    protected Encoder _encoder;
    protected Decoder _decoder;
    private String bindingId;

    public ContactInfoBase(Connection connection,
                           MessageDispatcher messageDispatcher, Encoder encoder, Decoder decoder,
                           String bindingId) {
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

    public SOAPEncoder getSOAPEncoder() {
        return (SOAPEncoder) _encoder;
    }

    public SOAPDecoder getSOAPDecoder() {
        return (SOAPDecoder) _decoder;
    }

    public InternalEncoder getInternalEncoder() {
        return new ClientEncoderDecoder();
    }

    public String getBindingId() {
        if (bindingId == null) {
            return SOAPBinding.SOAP11HTTP_BINDING;
        }

        return bindingId;
    }
}
