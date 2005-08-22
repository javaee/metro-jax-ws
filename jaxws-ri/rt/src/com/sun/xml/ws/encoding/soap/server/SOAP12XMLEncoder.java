/**
 * $Id: SOAP12XMLEncoder.java,v 1.5 2005-08-22 18:06:44 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.soap.server;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import javax.xml.namespace.QName;
import javax.xml.XMLConstants;
import javax.xml.soap.Detail;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.message.SOAP12FaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.server.*;
import com.sun.xml.ws.streaming.DOMStreamReader;

import static com.sun.xml.ws.client.BindingProviderProperties.*;

public class SOAP12XMLEncoder extends SOAPXMLEncoder {
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
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(new LocalizableExceptionAdapter(e));           
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
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(new LocalizableExceptionAdapter(e));           
        }        
    }

    /*
     * @see SOAPEncoder#startHeader(XMLStreamWriter)
     */
    @Override
    protected void startHeader(XMLStreamWriter writer) {
        try {
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_HEADER, SOAP12NamespaceConstants.ENVELOPE); // <env:Header>
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(new LocalizableExceptionAdapter(e));           
        }       
    }

    /* (non-Javadoc)
     * @see com.sun.xml.rpc.rt.server.SOAPXMLEncoder#writeFault(com.sun.xml.rpc.soap.message.SOAPFaultInfo, com.sun.pept.ept.MessageInfo, com.sun.xml.rpc.streaming.XMLStreamWriter)
     */
    @Override
    protected void writeFault(SOAPFaultInfo faultInfo, MessageInfo messageInfo, XMLStreamWriter writer) {
        if(!(faultInfo instanceof SOAP12FaultInfo))
            return;
        ((SOAP12FaultInfo)faultInfo).write(writer, messageInfo);
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
    protected String getBindingId(){
        return SOAPBinding.SOAP12HTTP_BINDING;
    }
}
