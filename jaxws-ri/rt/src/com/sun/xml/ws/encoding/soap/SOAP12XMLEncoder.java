/**
 * $Id: SOAP12XMLEncoder.java,v 1.1 2005-07-14 20:21:05 kwalsh Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap;

import static java.util.logging.Logger.getLogger;

import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.BindingProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.transport.Connection;
import com.sun.pept.presentation.MessageStruct;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.DispatchBase;
import com.sun.xml.ws.client.SenderException;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;

import java.util.logging.Logger;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;

public class SOAP12XMLEncoder extends SOAPXMLEncoder {


    private static final Logger logger =
           getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch.util").toString());
       //jaxbcontext can not be static
       private JAXBContext jc = null;

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


     /*  public InternalMessage toInternalMessage(MessageInfo messageInfo) {
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

           messageContext.setMessage(soapMessage);
           ClientTransportFactory clientTransportFactory = null;
           ClientTransport clientTransport = null;
           if (clientTransportFactory == null)
               clientTransportFactory = DispatchBase.getDefaultTransportFactory();

           //set the HTTPClientTransport with appropriate binding
           if(clientTransportFactory != null && clientTransportFactory instanceof HttpClientTransportFactory){
               clientTransport = ((HttpClientTransportFactory)clientTransportFactory).create(SOAPBinding.SOAP12HTTP_BINDING);
           }else{
               clientTransport = clientTransportFactory.create();
           }
           messageInfo.setConnection(new ClientConnectionBase((String) context.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY),
               clientTransport,
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


    */


    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startEnvelope(com.sun.xml.rpc.streaming.XMLStreamWriter)
     */
    @Override
    protected void startEnvelope(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_ENVELOPE, SOAP12NamespaceConstants.ENVELOPE);
            writer.setPrefix(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                             SOAP12NamespaceConstants.ENVELOPE);
            writer.writeNamespace(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                                  SOAP12NamespaceConstants.ENVELOPE);
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }
    }

    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startBody(com.sun.xml.rpc.streaming.XMLStreamWriter)
     */
    @Override
        protected void startBody(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_BODY, SOAP12NamespaceConstants.ENVELOPE);
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }
    }

    /*
     * @see com.sun.xml.rpc.rt.encoding.soap.SOAPEncoder#startHeader(com.sun.xml.rpc.streaming.XMLStreamWriter)
     */
    @Override
        protected void startHeader(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_HEADER,
                SOAP12NamespaceConstants.ENVELOPE);     // <env:Header>
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.client.SOAPXMLEncoder#getContentType()
     */
    @Override
    protected String getContentType(MessageInfo messageInfo){
        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if(rtc != null){
            BridgeContext bc = ((RuntimeContext)rtc).getBridgeContext();
            if(bc != null){
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)bc.getAttachmentMarshaller();
                if(am.isXopped())
                    return "application/xop+xml;type=\"application/soap+xml\"";
                }
        }
        return "application/soap+xml";
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return
     */
    protected String getBindingId(){
        return SOAPBinding.SOAP12HTTP_BINDING;
    }
}
