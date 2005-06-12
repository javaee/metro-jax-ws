/*
 * $Id: DispatchXMLEncoder.java,v 1.6 2005-06-12 19:07:14 kwalsh Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.transport.Connection;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamWriter;
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
    //jaxbcontext can not be static
    private JAXBContext jc = null;

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
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

        try {
            startEnvelope(writer);
            //headers will most likely be handled by "JAXWS handlers"
            //need to overide for now
            writeHeader(writer, request);
            writeBody(writer, request, messageInfo);
            endEnvelope(writer);
            writer.writeEndDocument();
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

    protected void writeHeaders(XMLStreamWriter writer, InternalMessage response,
                                MessageInfo messageInfo) {

    }

    //dispatch will need to overide for now till handlers figured out
    protected void writeHeader(XMLStreamWriter writer, InternalMessage request) {
        List<HeaderBlock> headerBlocks = request.getHeaders();

        if (headerBlocks == null) {
            return;
        }
    }
    //use super - leave for now
    /* protected void writeBody(XMLStreamWriter writer, InternalMessage request) {
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

        ContextMap ocontext =
            (ContextMap) messageInfo.getMetaData(JAXWS_CONTEXT_PROPERTY);
        ContextMap context = ((RequestContext) ocontext).copy();

        jc = (JAXBContext)
            context.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);

        if (messageInfo.getMEP() == MessageStruct.ONE_WAY_MEP)
            messageContext.put(BindingProviderProperties.ONE_WAY_OPERATION, "true");
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
