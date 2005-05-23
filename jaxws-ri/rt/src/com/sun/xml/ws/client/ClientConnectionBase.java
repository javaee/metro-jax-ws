/*
 * $Id: ClientConnectionBase.java,v 1.1 2005-05-23 22:26:34 bbissett Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.transport.Connection;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.encoding.soap.message.SOAPMsgCreateException;
import com.sun.xml.ws.server.SOAPConnectionBase;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_TYPE_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.ONE_WAY_OPERATION;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;


/**
 * @author JAX-RPC RI Development Team
 */
public class ClientConnectionBase extends SOAPConnectionBase implements Connection {
    ClientTransport transport;
    String endpoint;
    SOAPMessageContext messageContext = null;
    SOAPMessage soapMessage = null;

    public ClientConnectionBase(String endpoint, ClientTransport transport, SOAPMessageContext messageContext) {
        this.endpoint = endpoint;
        this.transport = transport;
        this.messageContext = messageContext;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.transport.Connection#write(java.nio.ByteBuffer)
     */
    public void write(ByteBuffer buffer) {
        try {
            byte[] data = buffer.array();
            int offset = buffer.arrayOffset();
            int length = buffer.limit() - buffer.position();
            messageContext.getMessage().getSOAPPart().setContent(new StreamSource(new ByteInputStream(data, offset, length)));
            String mep = (String) messageContext.get(ONE_WAY_OPERATION);
            if ((mep != null) && mep.equalsIgnoreCase("true"))
                transport.invokeOneWay(endpoint, messageContext);
            else
                transport.invoke(endpoint, messageContext);
        } catch (SOAPException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        }
    }

    /* (non-Javadoc)
     * @see com.sun.pept.transport.Connection#read(ByteBuffer)
     */
    public int read(ByteBuffer buffer) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SOAPMessage sm = messageContext.getMessage();
            sm.writeTo(baos);
            if (baos != null) {
                buffer.put(baos.toByteArray());
                buffer.flip();
            }
        } catch (SOAPException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        } catch (IOException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        }

        return buffer.array().length;
    }

    /* (non-Javadoc)
     * @see com.sun.pept.transport.Connection#readUntilEnd()
     */
    public ByteBuffer readUntilEnd() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            SOAPMessage sm = messageContext.getMessage();
            sm.writeTo(baos);
        } catch (SOAPException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        } catch (IOException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        }

        return ByteBuffer.wrap(baos.toByteArray());
    }

    /* (non-Javadoc)
     * @see com.sun.pept.transport.Connection#getEPTFactory()
     */
    public EPTFactory getEPTFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    public void sendResponse(SOAPMessage soapMessage) {
        this.soapMessage = soapMessage;

        // copy all the MIME headers from MessageContext to SOAPMessage
        MimeHeaders copyFrom = messageContext.getMessage().getMimeHeaders();
        MimeHeaders copyInto = soapMessage.getMimeHeaders();
        Iterator iter = copyFrom.getAllHeaders();
        while (iter.hasNext()) {
            MimeHeader mh = (MimeHeader) iter.next();
            if (copyInto.getHeader(mh.getName()) == null)
                copyInto.addHeader(mh.getName(), mh.getValue());
        }

        // now set the SOAP message in message context
        messageContext.setMessage(soapMessage);
        String mep = (String) messageContext.get(ONE_WAY_OPERATION);
        if ((mep != null) && mep.equalsIgnoreCase("true"))
            transport.invokeOneWay(endpoint, messageContext);
        else
            transport.invoke(endpoint, messageContext);
    }

    public SOAPMessage getSOAPMessage() {
        return soapMessage;
    }

    public SOAPMessage getSOAPMessage(MessageInfo messageInfo) {
        Connection connection = messageInfo.getConnection();
        ByteBuffer responseBuffer = connection.readUntilEnd();
        ByteInputStream inputStream = new ByteInputStream(responseBuffer.array(),
            responseBuffer.array().length);

        MimeHeaders mh = new MimeHeaders();
//        if (messageContext.getProperty("FAST_ENCODING") != null) {
//            mh.addHeader(CONTENT_TYPE_PROPERTY, FAST_CONTENT_TYPE_VALUE);
//        } else {
        mh.addHeader(CONTENT_TYPE_PROPERTY, XML_CONTENT_TYPE_VALUE);
//        }
        SOAPMessage message = null;
        try {
            message = messageContext.createMessage(mh, inputStream);
        } catch (IOException e) {
            throw new SOAPMsgCreateException("soap.msg.create.err",
                new Object[]{e});
        }

        return message;
    }

}
