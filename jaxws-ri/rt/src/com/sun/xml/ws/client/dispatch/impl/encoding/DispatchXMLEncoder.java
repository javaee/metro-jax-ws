/*
 * $Id: DispatchXMLEncoder.java,v 1.7 2005-06-14 15:35:15 kwalsh Exp $
 *
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.bind.api.BridgeContext;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.List;

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

    private boolean skipHeader(MessageInfo messageInfo) {
        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) ==
            Service.Mode.PAYLOAD) {
            return true;
        }
        return false;
    }

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

    public SOAPMessage toSOAPMessage(InternalMessage internalMessage,
                                     MessageInfo messageInfo) {
        setAttachmentsMap(messageInfo, internalMessage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

        SOAPMessage message = null;
        try {
            startEnvelope(writer);
            writeHeaders(writer, internalMessage, messageInfo);
            writeBody(writer, internalMessage, messageInfo);
            endEnvelope(writer);
            writer.writeEndDocument();
            writer.close();

            byte[] buf = baos.toByteArray();
            ByteInputStream bis = new ByteInputStream(buf, 0, buf.length);

            // TODO: Copy the mime headers from messageInfo.METADATA
            MimeHeaders mh = new MimeHeaders();
            mh.addHeader("Content-Type", getContentType(messageInfo));
            message = new SOAPMessageContext().createMessage(mh, bis, getBindingId());
            processAttachments(internalMessage, message);
        } catch (IOException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        } catch (SOAPException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }

        return message;
    }

    /*
     * writes multiple header elements in <env:Header> ... </env:Header>
     */
    protected void writeHeaders(XMLStreamWriter writer, InternalMessage response,
        MessageInfo messageInfo)
    {
        //just stub it out
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
