/*
 * $Id: SOAPXMLEncoder.java,v 1.5 2005-07-18 18:55:45 kwalsh Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.transport.Connection;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.*;
import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.sun.xml.ws.client.BindingProviderProperties.*;
import static java.util.logging.Logger.getLogger;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;

/**
 * @author WS RI Development Team
 */
public class SOAPXMLEncoder extends SOAPEncoder {


    private static final Logger logger =
        getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch.util").toString());
    //jaxbcontext can not be static
    private JAXBContext jc = null;

    protected JAXBContext getJAXBContext(MessageInfo messageInfo) {
        if (jc == null){
            RequestContext context = (RequestContext)messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
            if (context != null)
                jc = (JAXBContext)context.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        }
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
        if (context != null) {
            DispatchContext.MessageType type =
                (DispatchContext.MessageType) context.getProperty(DispatchContext.DISPATCH_MESSAGE);
            Object[] data = messageInfo.getData();
            BodyBlock bodyBlock = null;
            switch (type) {
                case JAXB_MESSAGE:
                    break;
                case JAXB_PAYLOAD:
                    JAXBBeanInfo jaxbInfo = new JAXBBeanInfo(data[0], getJAXBContext(messageInfo));
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

        } else {
            LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
            InternalEncoder internalEncoder = eptf.getInternalEncoder();
            //processProperties(messageInfo);
            return (InternalMessage) internalEncoder.toInternalMessage(messageInfo);
        }
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

    public InternalMessage createInternalMessage(MessageInfo messageInfo) {

        InternalMessage internalMessage = new InternalMessage();
        Method method = messageInfo.getMethod();

        Object response = messageInfo.getResponse();

        BodyBlock bodyBlock = null;
        if (getJAXBContext(messageInfo) != null) {
            JAXBBeanInfo jaxbBean = new JAXBBeanInfo(response, getJAXBContext(messageInfo));
            bodyBlock = new BodyBlock(jaxbBean);
        } else if (response instanceof Source) {
            bodyBlock = new BodyBlock((Source) response);
        }

        internalMessage.setBody(bodyBlock);
        return internalMessage;
    }


    public SOAPXMLEncoder() {
    }


    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public ByteBuffer encode(MessageInfo messageInfo) {
        try {
            InternalMessage request = toInternalMessage(messageInfo);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

            startEnvelope(writer);
            writeHeaders(writer, request, messageInfo);
            writeBody(writer, request, messageInfo);
            endEnvelope(writer);
            writer.writeEndDocument();
            writer.close();

            return ByteBuffer.wrap(baos.toByteArray());
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }
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

    /**
     * @param messageInfo
     */
    protected void processProperties(MessageInfo messageInfo) {
        SOAPMessageContext messageContext = new SOAPMessageContext();
        SOAPMessage soapMessage = messageContext.createMessage(getBindingId());
        MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();

        ContextMap properties = (ContextMap) messageInfo
            .getMetaData(JAXWS_CONTEXT_PROPERTY);

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
                if (propName.equals(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY)) {
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
            if (getBindingId().equals(SOAPBinding.SOAP12HTTP_BINDING)) {
                mimeHeaders.addHeader(ACCEPT_PROPERTY, SOAP12_XML_ACCEPT_VALUE);
            } else {
                mimeHeaders.addHeader(ACCEPT_PROPERTY, XML_ACCEPT_VALUE);
            }
        }

        RuntimeContext runtimeContext = (RuntimeContext) messageInfo.getMetaData(JAXWS_RUNTIME_CONTEXT);
        if (runtimeContext != null) {
            JavaMethod javaMethod = runtimeContext.getModel().getJavaMethod(messageInfo.getMethod());
            if (javaMethod != null) {
                String soapAction = ((com.sun.xml.ws.model.soap.SOAPBinding) javaMethod.getBinding()).getSOAPAction();
                if (soapAction != null)
                    messageContext.put(javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
            }
        }

        messageContext.setMessage(soapMessage);
        ClientTransport clientTransport = null;


            if (clientTransportFactory == null) {
                clientTransportFactory = new HttpClientTransportFactory();
            }
            if (clientTransportFactory instanceof HttpClientTransportFactory) {
                clientTransport = ((HttpClientTransportFactory) clientTransportFactory).create(getBindingId());
            } else {
                //local transport
                clientTransport = clientTransportFactory.create();
            }
            messageInfo.setConnection(new ClientConnectionBase((String) properties.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY), clientTransport,
            messageContext));
    }

    protected String getContentType(MessageInfo messageInfo) {

        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) bc.getAttachmentMarshaller();
                if (am.isXopped())
                    return "application/xop+xml;type=\"text/xml\"";
            }
        }
        return XML_CONTENT_TYPE_VALUE;
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     *
     * @return
     */
    protected String getBindingId() {
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
