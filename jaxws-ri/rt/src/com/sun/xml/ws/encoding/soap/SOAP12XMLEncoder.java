/**
 * $Id: SOAP12XMLEncoder.java,v 1.4 2005-07-18 18:55:44 kwalsh Exp $
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
