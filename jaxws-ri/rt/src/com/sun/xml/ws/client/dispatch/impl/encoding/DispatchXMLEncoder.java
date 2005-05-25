/*
 * $Id: DispatchXMLEncoder.java,v 1.3 2005-05-25 20:44:12 kohlert Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.transport.Connection;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.streaming.XMLWriter;
import com.sun.xml.ws.streaming.XMLWriterFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;
import static java.util.logging.Logger.getLogger;

/**
 * @author JAX-RPC Development Team
 */

public class DispatchXMLEncoder extends com.sun.xml.ws.client.SOAPXMLEncoder {

    private static final Logger logger =
        getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch.util").toString());
    private static JAXBContext jc = null;

    public DispatchXMLEncoder() {
        super();
    }

    protected JAXBContext getJAXBContext() {
        return jc;
    }

    public void encodeAndSend(MessageInfo messageInfo) {
        //processProperties(messageInfo);
        InternalMessage request = toInternalMessage(messageInfo);

        Connection connection = messageInfo.getConnection();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLWriterFactory factory = XMLWriterFactory.newInstance();
        XMLWriter writer = factory.createXMLWriter(baos);

        try {
            startEnvelope(writer);
            //headers will most likely be handled by "JAXWS handlers"
            //need to overide for now
            writeHeader(writer, request);
            writeBody(writer, request, messageInfo);
            endEnvelope(writer);

            writer.close();

            // sending the request over the wire
            if (connection != null)
                connection.write(ByteBuffer.wrap(baos.toByteArray()));
            else
                throw new WebServiceException("connection is null");

        } catch (Exception e) {
            if (e instanceof WebServiceException)
                throw (WebServiceException) e;
            else
                throw new WebServiceException(e.getMessage(), e);
        }
    }

    protected void writeHeaders(XMLWriter writer, InternalMessage response,
                                MessageInfo messageInfo) {

    }

    //dispatch will need to overide for now till handlers figured out
    protected void writeHeader(XMLWriter writer, InternalMessage request) {
        List<HeaderBlock> headerBlocks = request.getHeaders();

        if (headerBlocks == null) {
            return;
        }
    }
    //use super - leave for now
    /* protected void writeBody(XMLWriter writer, InternalMessage request) {
         BodyBlock bodyBlock = request.getBody();

         if (bodyBlock == null) {
             throw new SenderException("sender.request.missingBodyInfo");
         }

         writer.startElement(SOAPNamespaceConstants.TAG_BODY,
                 SOAPNamespaceConstants.ENVELOPE,
                 NamespaceConstants.NSPREFIX_SOAP_ENVELOPE);

         DispatchSerializer bodySerializer =
                 new DispatchSerializer(encoderDecoderUtil);

         bodySerializer.serialize(bodyBlock.getValue(),
                 writer,
                 getJAXBContext());


         writer.endElement(); // env:BODY
     }
     */


    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        processProperties(messageInfo);
        InternalMessage internalMessage = new InternalMessage();
        DispatchContext context = (DispatchContext) messageInfo.getMetaData(BindingProviderProperties.DISPATCH_CONTEXT);
        DispatchContext.MessageType type =
            (DispatchContext.MessageType) context.getProperty(DispatchContext.DISPATCH_MESSAGE);
        Object[] data = messageInfo.getData();
        BodyBlock bodyBlock = null;
        switch (type) {
            case JAXB_MESSAGE:
                break;
            case JAXB_PAYLOAD:
                JAXBBeanInfo jaxbInfo = new JAXBBeanInfo(data[0], getJAXBContext());
                bodyBlock = new BodyBlock(jaxbInfo);
                break;
            case SOURCE_PAYLOAD:
                data = messageInfo.getData();
                bodyBlock = new BodyBlock((Source) data[0]);
                break;
            default:

        }

        if (bodyBlock != null)
            internalMessage.setBody(bodyBlock);

        return internalMessage;

    }

    public void processProperties(MessageInfo messageInfo) {

        SOAPMessageContext messageContext = new SOAPMessageContext();
        SOAPMessage soapMessage = messageContext.createMessage();
        MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();


        boolean acceptPropertySet = false;
        boolean encodingPropertySet = false;
        //this needs to be jaxwscontext
        ContextMap ocontext =
            (ContextMap) messageInfo.getMetaData(JAXWS_CONTEXT_PROPERTY);
        ContextMap context = ((RequestContext) ocontext).copy();

        jc = (JAXBContext)
            context.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);

        if (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP)
            messageContext.put(BindingProviderProperties.ONE_WAY_OPERATION, "true");

        /*
        // process the properties
        //todo: properties - ? BindingProvider ? need to set transport

        Object username = context.getProperty(BindingProvider.USERNAME_PROPERTY);
        if (username != null) {
            messageContext.setProperty(BindingProvider.USERNAME_PROPERTY, username);
        }
        Object password = context.getProperty(BindingProvider.PASSWORD_PROPERTY);
        if (password != null) {
            messageContext.setProperty(BindingProvider.PASSWORD_PROPERTY, password);
        }
        Object endpoint = context.getProperty(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        if (endpoint != null) {
            messageContext.setProperty(Call.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        }
        Object operation = context.getProperty(Call.OPERATION_STYLE_PROPERTY);
        if (operation != null) {
            messageContext.setProperty(Call.OPERATION_STYLE_PROPERTY, operation);
        }

        //this is required properties
        Boolean isSOAPActionUsed =
                (Boolean) context.getProperty(BindingProvider.SOAPACTION_USE_PROPERTY);
        if (isSOAPActionUsed != null) {
            if (isSOAPActionUsed.booleanValue()) {
                messageContext.setProperty(HTTP_SOAPACTION_PROPERTY,
                        context.getProperty(BindingProvider.SOAPACTION_URI_PROPERTY));
            }
        }

        Object encoding = context.getProperty(Call.ENCODINGSTYLE_URI_PROPERTY);
        if (encoding != null) {
            messageContext.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY, encoding);
        }

        Object verification = context.getProperty(HOSTNAME_VERIFICATION_PROPERTY);
        if (verification != null) {
            messageContext.setProperty(HOSTNAME_VERIFICATION_PROPERTY,
                    verification);
        }

        Object maintainSession =
                context.getProperty(BindingProvider.SESSION_MAINTAIN_PROPERTY);
        if (maintainSession != null) {
            messageContext.setProperty(BindingProvider.SESSION_MAINTAIN_PROPERTY,
                    maintainSession);
        }
        if (maintainSession != null && maintainSession.equals(Boolean.TRUE)) {
            Object cookieJar =
                    context.getProperty(BindingProviderProperties.HTTP_COOKIE_JAR);
            if (cookieJar != null)
                messageContext.setProperty(BindingProviderProperties.HTTP_COOKIE_JAR,
                        cookieJar);
        }

        String encodingProp = (String)
                context.getProperty(XMLFAST_ENCODING_PROPERTY);

        if (encodingProp != null) {
            encodingPropertySet = true;
            if (encodingProp.equals(FAST_ENCODING_VALUE)) {
                mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, FAST_CONTENT_TYPE_VALUE);
            } else {
                mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, XML_CONTENT_TYPE_VALUE);
            }
        } else {    // default is XML encoding
            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
        }

        String accept = (String) context.getProperty(ACCEPT_ENCODING_PROPERTY);
        if (accept != null) {
            acceptPropertySet = true;
            if (accept.equals(FAST_ENCODING_VALUE))
                mimeHeaders.addHeader(ACCEPT_PROPERTY, FAST_ACCEPT_VALUE);
            else
                mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
        } else {    // default is XML encoding
            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
        }

        // default Content-Type is XML encoding
        if (!encodingPropertySet) {
            mimeHeaders.addHeader(CONTENT_TYPE_PROPERTY, XML_CONTENT_TYPE_VALUE);
        }

        // default Accept is XML encoding
        if (!acceptPropertySet) {
            mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
        }

        //need to do these as well
        //(SECURITY_CONTEXT);
        //
        //temp.add(CallPropertyConstants.SET_ATTACHMENT_PROPERTY);
        //temp.add(CallPropertyConstants.GET_ATTACHMENT_PROPERTY);
        */
        messageContext.setMessage(soapMessage);
        ClientTransportFactory clientTransportFactory = null;
        if (clientTransportFactory == null)
            clientTransportFactory = DispatchBase.getDefaultTransportFactory();

        messageInfo.setConnection(new ClientConnectionBase((String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY),
            clientTransportFactory.create(),
            messageContext));
    }

    public InternalMessage createInternalMessage(MessageInfo messageInfo) {

        InternalMessage internalMessage = new InternalMessage();
        Method method = messageInfo.getMethod();

        Object response = messageInfo.getResponse();

        BodyBlock bodyBlock = null;
        if (getJAXBContext() != null) {
            JAXBBeanInfo jaxbBean = new JAXBBeanInfo(response, getJAXBContext());
            bodyBlock = new BodyBlock(jaxbBean);
        } else if (response instanceof Source) {
            bodyBlock = new BodyBlock((Source) response);
        }

        internalMessage.setBody(bodyBlock);
        return internalMessage;
    }


}
