/**
 * $Id: SOAP12XMLEncoder.java,v 1.7 2005-08-22 18:06:44 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap.client;

import static java.util.logging.Logger.getLogger;

import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.pept.ept.MessageInfo;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.client.SenderException;
import com.sun.xml.ws.client.BindingProviderProperties;

import java.util.logging.Logger;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;

import static com.sun.xml.ws.client.BindingProviderProperties.*;

/**
 * @author WS Development Team
 */
public class SOAP12XMLEncoder extends SOAPXMLEncoder {


    private static final Logger logger =
           getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch.util").toString());

    /*
     * @see SOAPEncoder#startEnvelope(XMLStreamWriter)
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
     * @see SOAPEncoder#startBody(XMLStreamWriter)
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
     * @see SOAPEncoder#startHeader(XMLStreamWriter)
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
    
    protected String getContentType(MessageInfo messageInfo) {
        String contentNegotiation = (String)
            messageInfo.getMetaData(BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY);

        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) bc.getAttachmentMarshaller();
                if (am.isXopped()) {
                    return XOP_SOAP12_XML_TYPE_VALUE;
                }                
            }
        }
        
        return (contentNegotiation == "optimistic") ? 
            FAST_INFOSET_TYPE_SOAP12 : SOAP12_XML_CONTENT_TYPE_VALUE;
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return the BindingID associated with this encoder
     */
    @Override
    protected String getBindingId (){
        return SOAPBinding.SOAP12HTTP_BINDING;
    }
}
