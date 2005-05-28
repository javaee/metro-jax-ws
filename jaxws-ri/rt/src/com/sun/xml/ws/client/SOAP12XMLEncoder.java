/**
 * $Id: SOAP12XMLEncoder.java,v 1.5 2005-05-28 01:10:09 spericas Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.client;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.pept.ept.MessageInfo;

import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

public class SOAP12XMLEncoder extends SOAPXMLEncoder {
    
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
}
