/*
 * $Id: SOAPXMLEncoder.java,v 1.2 2005-05-24 17:48:11 vivekp Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.client;


import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.transport.Connection;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.encoding.JAXRPCAttachmentMarshaller;
import com.sun.xml.ws.streaming.XMLWriter;
import com.sun.xml.ws.streaming.XMLWriterFactory;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import static com.sun.xml.ws.client.BindingProviderProperties.ACCEPT_ENCODING_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.ACCEPT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_TYPE_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_ACCEPT_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_CONTENT_TYPE_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.FAST_ENCODING_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_COOKIE_JAR;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXRPC_CONTEXT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.ONE_WAY_OPERATION;
import static com.sun.xml.ws.client.BindingProviderProperties.SOAP_ACTION_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.XMLFAST_ENCODING_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_ACCEPT_VALUE;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;

/**
 * @author JAX-RPC RI Development Team
 */
public class SOAPXMLEncoder extends SOAPEncoder {

    public SOAPXMLEncoder() {
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public ByteBuffer encode(MessageInfo messageInfo) {
        InternalMessage request = toInternalMessage(messageInfo);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance();
        XMLWriter writer = factory.createXMLWriter(baos);

        startEnvelope(writer);
        writeHeaders(writer, request, messageInfo);
        writeBody(writer, request, messageInfo);
        endEnvelope(writer);
        writer.close();

        return ByteBuffer.wrap(baos.toByteArray());
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
     */
    public void encodeAndSend(MessageInfo messageInfo) {
        ByteBuffer buffer = encode(messageInfo);

        // sending the request over the wire
        Connection connection = messageInfo.getConnection();
        if (connection == null)
            throw new SenderException("sender.request.connectionNotInitialized");

        connection.write(buffer);
    }

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
        InternalEncoder internalEncoder = eptf.getInternalEncoder();
        processProperties(messageInfo);
        InternalMessage im = (InternalMessage) internalEncoder.toInternalMessage(messageInfo);

        return im;
    }

    public SOAPMessage toSOAPMessage(InternalMessage internalMessage,
        MessageInfo messageInfo) {
        setAttachmentsMap(messageInfo, internalMessage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance();
        XMLWriter writer = factory.createXMLWriter(baos);

        SOAPMessage message = null;
        try {
            startEnvelope(writer);
            writeHeaders(writer, internalMessage, messageInfo);
            writeBody(writer, internalMessage, messageInfo);
            endEnvelope(writer);
            writer.close();

            byte[] buf = baos.toByteArray();
            ByteInputStream bis = new ByteInputStream(buf, 0, buf.length);

            // TODO: Copy the mime headers from messageInfo.METADATA
            MimeHeaders mh = new MimeHeaders();
            mh.addHeader("Content-Type", getContentType(messageInfo));
            message = new SOAPMessageContext().createMessage(mh, bis);
            processAttachments(internalMessage, message);
        } catch (IOException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        } catch (SOAPException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }

        return message;
    }

    /**
     * @param messageInfo
     */
    protected void processProperties(MessageInfo messageInfo) {
        SOAPMessageContext messageContext = new SOAPMessageContext();
        SOAPMessage soapMessage = messageContext.createMessage();
        MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();

        ContextMap properties = (ContextMap) messageInfo
            .getMetaData(JAXRPC_CONTEXT_PROPERTY);

        if (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP)
            messageContext.put(ONE_WAY_OPERATION, "true");

        ClientTransportFactory clientTransportFactory = null;
        boolean acceptPropertySet = false;
        boolean encodingPropertySet = false;
        // process the properties
        if (properties != null) {
            for (Iterator names = properties.getPropertyNames(); names.hasNext();) {
                String propName = (String) names.next();
                // consume PEPT-specific properties
                if (propName.equals(ClientTransportFactory.class.getName())) {
                    clientTransportFactory = (ClientTransportFactory) properties
                        .get(propName);
                } else if (propName.equals(BindingProvider.SESSION_MAINTAIN_PROPERTY)) {
                    Object maintainSession = properties.get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
                    if (maintainSession != null && maintainSession.equals(Boolean.TRUE)) {
                        Object cookieJar = properties.get(HTTP_COOKIE_JAR);
                        if (cookieJar != null)
                            messageContext.put(HTTP_COOKIE_JAR, cookieJar);
                    }
                } else if (propName.equals(XMLFAST_ENCODING_PROPERTY)) {
                    encodingPropertySet = true;
                    String encoding = (String) properties.get(XMLFAST_ENCODING_PROPERTY);
                    if (encoding != null) {
                        if (encoding.equals(FAST_ENCODING_VALUE)) {
                            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, FAST_CONTENT_TYPE_VALUE);
                        } else {
                            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, getContentType(messageInfo));
                        }
                    } else { // default is XML encoding
                        mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
                    }
                } else if (propName.equals(ACCEPT_ENCODING_PROPERTY)) {
                    acceptPropertySet = true;
                    String accept = (String) properties.get(ACCEPT_ENCODING_PROPERTY);
                    if (accept != null) {
                        if (accept.equals(FAST_ENCODING_VALUE))
                            mimeHeaders.addHeader(ACCEPT_PROPERTY, FAST_ACCEPT_VALUE);
                        else
                            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
                    } else { // default is XML encoding
                        mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
                    }
                } else if (propName.equals(SOAP_ACTION_PROPERTY)) {
                    String soapAction = (String) properties.get(SOAP_ACTION_PROPERTY);
                    if (soapAction != null)
                        mimeHeaders.addHeader(SOAP_ACTION_PROPERTY, soapAction);
                } else {
                    messageContext.put(propName, properties.get(propName));
                }
            }
        }

        // default Content-Type is XML encoding
        if (!encodingPropertySet) {
            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, XML_CONTENT_TYPE_VALUE);
        }

        // default Accept is XML encoding
        if (!acceptPropertySet) {
            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
        }

        messageContext.setMessage(soapMessage);
        if (clientTransportFactory == null)
            clientTransportFactory = new HttpClientTransportFactory();

        messageInfo.setConnection(new ClientConnectionBase((String) properties.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY), clientTransportFactory.create(),
            messageContext));
    }

    protected String getContentType(MessageInfo messageInfo){
        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXRPC_RUNTIME_CONTEXT);
        if(rtc != null){
            JAXRPCAttachmentMarshaller am = (JAXRPCAttachmentMarshaller)((com.sun.xml.ws.server.RuntimeContext)rtc).getBridgeContext().getAttachmentMarshaller();
            if(am.isXopped())
                return "application/xop+xml;type=\"text/xml\"";
        }
        return XML_CONTENT_TYPE_VALUE;
    }
}
