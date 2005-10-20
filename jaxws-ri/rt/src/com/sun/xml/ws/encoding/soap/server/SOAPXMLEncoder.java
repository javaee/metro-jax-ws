/*
 * $Id: SOAPXMLEncoder.java,v 1.14 2005-10-20 01:58:43 jitu Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.encoding.soap.server;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.client.BindingProviderProperties;
import static com.sun.xml.ws.client.BindingProviderProperties.*;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.streaming.DOMStreamReader;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author WS Development Team
 */
public class SOAPXMLEncoder extends SOAPEncoder {

    protected static final String FAULTCODE_NAME   = "faultcode";
    protected static final String FAULTSTRING_NAME = "faultstring";
    protected static final String FAULTACTOR_NAME  = "faultactor";
    protected static final String DETAIL_NAME      = "detail";
    
    public SOAPXMLEncoder() {
    }

    public SOAPMessage toSOAPMessage(InternalMessage response, MessageInfo messageInfo) {
        XMLStreamWriter writer = null;
        JAXWSAttachmentMarshaller marshaller = null;
        boolean xopEnabled = false;
        
        try {
            setAttachmentsMap(messageInfo, response);
            ByteArrayBuffer bab = new ByteArrayBuffer();
            
            if (messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY) == "optimistic") {
                writer = XMLStreamWriterFactory.createFIStreamWriter(bab);
                
                // Turn XOP off for FI
                marshaller = getAttachmentMarshaller(messageInfo);
                if (marshaller != null) {
                    xopEnabled = marshaller.isXOPPackage();     // last value
                    marshaller.setXOPPackage(false);
                }
            }
            else {
                // Store output stream to use in JAXB bridge (not with FI)
                messageInfo.setMetaData(JAXB_OUTPUTSTREAM, bab);
                writer = XMLStreamWriterFactory.createXMLStreamWriter(bab);
            }
            
            writer.writeStartDocument();
            startEnvelope(writer);
            writeEnvelopeNamespaces(writer, messageInfo);
            writeHeaders(writer, response, messageInfo);
            writeBody(writer, response, messageInfo);
            endEnvelope(writer);
            writer.writeEndDocument();
            writer.close();

            MimeHeaders mh = new MimeHeaders();
            mh.addHeader("Content-Type", getContentType(messageInfo, marshaller));
            SOAPMessage msg = SOAPUtil.createMessage(mh, bab.newInputStream(), getBindingId());
            processAttachments(response, msg);
            
            // Restore default XOP processing before returning
            if (marshaller != null) {
                marshaller.setXOPPackage(xopEnabled);
            }            
            
            return msg;
        } 
        catch (Exception e) {
            throw new ServerRtException("soapencoder.err", new Object[]{e});
        } 
        finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (XMLStreamException e) {
                    throw new ServerRtException(e);
                }
            }
        }
    }
    
    protected JAXWSAttachmentMarshaller getAttachmentMarshaller(MessageInfo messageInfo) {
        Object rtc = messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                return (JAXWSAttachmentMarshaller) bc.getAttachmentMarshaller();
            }
        }        
        return null;
    }
    
    protected String getContentType(MessageInfo messageInfo, 
        JAXWSAttachmentMarshaller marshaller) 
    {
        String contentNegotiation = (String)
            messageInfo.getMetaData(BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY);

        if (marshaller == null) {
            marshaller = getAttachmentMarshaller(messageInfo);
        }
        
        if (marshaller != null && marshaller.isXopped()) {
            return XOP_SOAP11_XML_TYPE_VALUE;
        }
        else {
            return (contentNegotiation == "optimistic") ? 
                FAST_INFOSET_TYPE_SOAP11 : XML_CONTENT_TYPE_VALUE;
        }
    }

    /*
     * writes <env:Fault> ... </env:Fault>. JAXB serializes the contents
     * in the <detail> for service specific exceptions. We serialize protocol
     * specific exceptions ourselves
     */
    protected void writeFault(SOAPFaultInfo instance, MessageInfo messageInfo, XMLStreamWriter writer) {
        try {
            // Set a status code for Fault
            MessageContext ctxt = MessageInfoUtil.getMessageContext(messageInfo);
            if (MessageContextUtil.getHttpStatusCode(ctxt) == null) {
                MessageContextUtil.setHttpStatusCode(ctxt, WSConnection.INTERNAL_ERR);
            }
            
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPConstants.QNAME_SOAP_FAULT.getLocalPart(),
                SOAPConstants.QNAME_SOAP_FAULT.getNamespaceURI());
            // Writing NS since this may be called without writing envelope
            writer.writeNamespace(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                    SOAPConstants.QNAME_SOAP_FAULT.getNamespaceURI());

            writer.writeStartElement(FAULTCODE_NAME);   // <faultcode>
            String prefix = SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE;
            QName faultCode = instance.getCode();
            String nsURI = faultCode.getNamespaceURI();
            if (!nsURI.equals(SOAPNamespaceConstants.ENVELOPE)) {
                    // Need to add namespace declaration for this custom fault code
                if (nsURI.equals(XMLConstants.NULL_NS_URI)) {
                    prefix = XMLConstants.DEFAULT_NS_PREFIX;
                } else {
                    prefix = faultCode.getPrefix();
                    if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                        prefix = "ans";
                    }
                    writer.setPrefix(prefix, nsURI);
                    writer.writeNamespace(prefix, nsURI);
                }
            }
            if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                writer.writeCharacters(instance.getCode().getLocalPart());
            } else {
                    writer.writeCharacters(prefix+":"+instance.getCode().getLocalPart());
            }
            writer.writeEndElement();                    // </faultcode>

            writer.writeStartElement(FAULTSTRING_NAME);
            writer.writeCharacters(instance.getString());
            writer.writeEndElement();

            if (instance.getActor() != null) {
                writer.writeStartElement(FAULTACTOR_NAME);
                writer.writeCharacters(instance.getActor());
                writer.writeEndElement();
            }

            Object detail = instance.getDetail();
            if (detail != null) {
                // Not RuntimeException, Not header fault
                if (detail instanceof Detail) {
                    // SOAPFaultException
                    encodeDetail((Detail)detail, writer);
                } else if (detail instanceof JAXBBridgeInfo) {
                    // Service specific exception
                    writer.writeStartElement(DETAIL_NAME);
                    writeJAXBBridgeInfo((JAXBBridgeInfo)detail, messageInfo, writer);
                    writer.writeEndElement();        // </detail>
                }
            }

            writer.writeEndElement();                // </env:Fault>
        }
        catch (XMLStreamException e) {
            throw new ServerRtException(e);
        }
    }

    /*
     * Serializes javax.xml.soap.Detail. Detail is of type SOAPElement.
     * XmlTreeReader is used to traverse the SOAPElement/DOM Node and serializes
     * the XML.
     */
    protected void encodeDetail(Detail detail, XMLStreamWriter writer) {
        serializeReader(new DOMStreamReader(detail), writer);
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return the BindingID associated with this encoder
     */
    protected String getBindingId(){
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
