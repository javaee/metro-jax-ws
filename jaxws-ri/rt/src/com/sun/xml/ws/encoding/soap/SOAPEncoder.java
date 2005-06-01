/*
 * $Id: SOAPEncoder.java,v 1.9 2005-06-01 18:34:38 spericas Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.encoding.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.ws.WebServiceException;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.activation.DataHandler;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.pept.encoding.Encoder;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayloadSerializer;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * @author JAX-RPC RI Development Team
 */
public abstract class SOAPEncoder implements Encoder {

    /*
     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
     */
    public abstract void encodeAndSend(MessageInfo messageInfo);

    /*
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public abstract ByteBuffer encode(MessageInfo messageInfo);

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        return null;
    }

    public DOMSource toDOMSource(JAXBBridgeInfo bridgeInfo, MessageInfo messageInfo) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        DOMResult domResult = new DOMResult();
        JAXBTypeSerializer.getInstance().serialize(bridgeInfo, bridgeContext,
            domResult.getNode());
        return new DOMSource(domResult.getNode());
    }

    public DOMSource toDOMSource(RpcLitPayload rpcLitPayload, MessageInfo messageInfo) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
            writeRpcLitPayload(rpcLitPayload, messageInfo, writer);
            byte[] buf = baos.toByteArray();
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(new ByteArrayInputStream(buf));
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            return new DOMSource(domResult.getNode());
        } catch(TransformerException te) {
            throw new WebServiceException(te);
        }
    }

    public DOMSource toDOMSource(SOAPFaultInfo faultInfo, MessageInfo messageInfo) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
            writeFault(faultInfo, messageInfo, writer);
            writer.writeEndDocument();
            byte[] buf = baos.toByteArray();
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(new ByteArrayInputStream(buf));
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            return new DOMSource(domResult.getNode());
        } 
        catch (TransformerException te) {
            throw new WebServiceException(te);
        }
        catch (XMLStreamException xe) {
            throw new WebServiceException(xe);
        }
    }

    protected void writeRpcLitPayload(RpcLitPayload rpcLitPayload, MessageInfo messageInfo,
        XMLStreamWriter writer) {        
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, writer);
    }

    protected void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, XMLStreamWriter writer) {
        JAXBTypeSerializer.getInstance().serialize(
                beanInfo.getBean(), writer, beanInfo.getJAXBContext());
    }

    /*
    protected void writeJAXBTypeInfo(JAXBTypeInfo typeInfo, XMLStreamWriter writer) {
        QName name = typeInfo.getName();
        Object value = typeInfo.getType();
        writeJAXBTypeInfo(name, value, writer);
    }


    protected void writeJAXBTypeInfo(QName name, Object value, XMLStreamWriter writer) {
        JAXBContext jaxbContext = encoderDecoderUtil.getJAXBContext();
        Map<QName, Class> typeMapping = encoderDecoderUtil.getTypeMapping();
        Class type = typeMapping.get(name);
        JAXBTypeSerializer.getInstance().serialize(name, type, value,
                writer, jaxbContext);
    }
     */

    protected void writeJAXBBridgeInfo(JAXBBridgeInfo bridgeInfo,
        MessageInfo messageInfo, XMLStreamWriter writer) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        JAXBTypeSerializer.getInstance().serialize(bridgeInfo, bridgeContext, writer);
    }

    public SOAPMessage toSOAPMessage(InternalMessage internalMessage, MessageInfo messageInfo) {
        return null;
    }

    /*
     * Replace the body in SOAPMessage with the BodyBlock of InternalMessage
     */
    public SOAPMessage toSOAPMessage(InternalMessage internalMessage,
            SOAPMessage soapMessage) {
        try {
            BodyBlock bodyBlock = internalMessage.getBody();
            Object value = bodyBlock.getValue();
            if (value == null) {
                return soapMessage;
            }
            if (value instanceof Source) {
                Source source = (Source)value;
                Transformer transformer = XmlUtil.newTransformer();
                DOMResult domResult = new DOMResult();
                transformer.transform(source, domResult);
                SOAPBody body = soapMessage.getSOAPBody();
                body.removeContents();
                Document doc = (Document)domResult.getNode();
                Node elem = body.getOwnerDocument().importNode(
                        doc.getDocumentElement(), true);
                body.appendChild(elem);
            } else {
                throw new UnsupportedOperationException("Unknown object in BodyBlock:"+value.getClass());
            }
            return soapMessage;
        } catch(Exception e) {
            throw new ServerRtException("soapencoder.err", new Object[]{e});
        }
    }

    protected void serializeReader(XMLStreamReader reader, XMLStreamWriter writer) {
        try {
            int state = XMLStreamConstants.START_DOCUMENT;
            do {
                state = reader.next();
                switch (state) {
                    case XMLStreamConstants.START_ELEMENT:                        
                        /*
                         * TODO: Is this necessary, shouldn't zephyr return "" instead of 
                         * null for getNamespaceURI() and getPrefix()?
                         */
                        String uri = reader.getNamespaceURI();
                        String prefix = reader.getPrefix();
                        String localName = reader.getLocalName();

                        if (prefix == null) {
                            if (uri == null) {
                                writer.writeStartElement(localName);                                
                            }
                            else {
                                writer.writeStartElement(uri, localName);
                            }
                        }
                        else {
                            assert uri != null;
                            // [1] When writing an element with an unseen prefix,
                            // Zephyr calls setPrefix(prefix, uri). Is this OK?
                            writer.writeStartElement(prefix, localName, uri);
                        }

                        // Write namespace declarations
                        int n = reader.getNamespaceCount();
                        for (int i = 0; i < n; i++) {
                            String nsPrefix = reader.getNamespacePrefix(i);                             
                            if (nsPrefix == null) nsPrefix = "";    // StAX returns null for default ns
                            String writerURI = writer.getNamespaceContext().getNamespaceURI(nsPrefix);
                            
                            // Zephyr: Why is this returning null?
                            // Compare nsPrefix with prefix because of [1] (above)
                            String readerURI = reader.getNamespaceURI(i);
                            if (writerURI == null || nsPrefix.equals(prefix) || !writerURI.equals(readerURI)) {                                
                                writer.setPrefix(nsPrefix, readerURI != null ? readerURI : "");
                                writer.writeNamespace(nsPrefix, readerURI != null ? readerURI : "");
                            }
                        }

                        // Write attributes
                        n = reader.getAttributeCount();
                        for (int i = 0; i < n; i++) {
                            writer.writeAttribute(reader.getAttributePrefix(i), 
                                reader.getAttributeLocalName(i), reader.getAttributeNamespace(i));
                        }                        
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        writer.writeEndElement();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        writer.writeCharacters(reader.getText());
                }
            } while (state != XMLStreamConstants.END_DOCUMENT);
        }
        catch (XMLStreamException e) {            
            throw new WebServiceException(e);
        }
    }

    protected void serializeSource(Source source, XMLStreamWriter writer) {
        try {
            XMLStreamReader reader = SourceReaderFactory.createSourceReader(source, true);
            serializeReader(reader, writer);
            reader.close();
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * writes start tag of envelope: <env:Envelope>
     */
    protected void startEnvelope(XMLStreamWriter writer) {
        try {
            //write SOAP Envelope
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_ENVELOPE, SOAPNamespaceConstants.ENVELOPE);
            writer.setPrefix(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                             SOAPNamespaceConstants.ENVELOPE);
            writer.writeNamespace(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                                  SOAPNamespaceConstants.ENVELOPE);                
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * writes start tag of Body: <env:Body>
     */
    protected void startBody(XMLStreamWriter writer) {
        try {
            //write SOAP Body
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE,
                SOAPNamespaceConstants.TAG_BODY, SOAPNamespaceConstants.ENVELOPE);
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * writes start tag of Header: <env:Header>
     */
    protected void startHeader(XMLStreamWriter writer) {
        try {      
            writer.writeStartElement(SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE, 
                SOAPNamespaceConstants.TAG_HEADER, SOAPNamespaceConstants.ENVELOPE); // <env:Header>
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * writes multiple header elements in <env:Header> ... </env:Header>
     */
    protected void writeHeaders(XMLStreamWriter writer, InternalMessage response,
        MessageInfo messageInfo) 
    {
        try {
            List<HeaderBlock> headerBlocks = response.getHeaders();
            if (headerBlocks == null || headerBlocks.isEmpty()) {
                return;
            }
            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
            BridgeContext bridgeContext = rtCtxt.getBridgeContext();
            startHeader(writer); // <env:Header>
            for (HeaderBlock headerBlock: headerBlocks) {
                Object value = headerBlock.getValue();
                if (value instanceof JAXBBridgeInfo) {
                    writeJAXBBridgeInfo((JAXBBridgeInfo)value, messageInfo, writer);
                } else {
                    System.out.println("Unknown object in BodyBlock:"+value.getClass());
                }
            }
            writer.writeEndElement();                                // </env:Header>
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /*
     * writes <env:Body> ... </env:Body>
     */
    protected void writeBody(XMLStreamWriter writer, InternalMessage response,
        MessageInfo messageInfo) 
    {
        try {
            startBody(writer);
            BodyBlock bodyBlock = response.getBody();
            // BodyBlock can be null if there is no part in wsdl:message
            if (bodyBlock != null) {
                Object value = bodyBlock.getValue();
                if (value instanceof Source) {
                    serializeSource((Source)value, writer);
                } else if (value instanceof SOAPFaultInfo) {
                    writeFault((SOAPFaultInfo)value, messageInfo, writer);
                } else if (value instanceof RpcLitPayload) {
                    writeRpcLitPayload((RpcLitPayload)value, messageInfo, writer);
                } else if (value instanceof JAXBBeanInfo) {
                    writeJAXBBeanInfo((JAXBBeanInfo)value, writer);
                } else if (value instanceof JAXBBridgeInfo) {
                    writeJAXBBridgeInfo((JAXBBridgeInfo)value, messageInfo, writer);
                } else {
                    System.out.println("Unknown object in BodyBlock:"+value.getClass());
                }
            }
            writer.writeEndElement();                // </env:body>
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Pass reference of attachments Map from InternalMessage to JAXWSAttachmentMarshaller.
     *
     * @param mi
     * @param im
     */
    protected void setAttachmentsMap(MessageInfo mi, InternalMessage im){
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if(rtc != null){
            BridgeContext bc = ((RuntimeContext)rtc).getBridgeContext();
            if(bc == null)
                return;
            JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)((RuntimeContext)rtc).getBridgeContext().getAttachmentMarshaller();
            am.setAttachments(im.getAttachments());
        }
    }


    /**
     * Add all the attachments in the InternalMessage to the SOAPMessage
     * @param im
     * @param msg
     */
    protected void processAttachments(InternalMessage im, SOAPMessage msg) throws SOAPException {
        Map<String, AttachmentBlock> attachments = im.getAttachments();
        for(String id : attachments.keySet()){
            AttachmentBlock block = attachments.get(id);
            if(block == null)
                continue;
            if(block.getValue() == null)
                continue;
            Object value = block.getValue();
            AttachmentPart ap = msg.createAttachmentPart();
            if(value instanceof DataHandler)
                ap.setDataHandler((DataHandler)value);
            else if(value instanceof InputStream)
                ap.setRawContent((InputStream)value, block.getType());
            ap.setContentId(id);
            msg.addAttachmentPart(ap);
        }
    }

    /*
     * writes end tag of envelope: </env:Envelope>
     */
    protected void endEnvelope(XMLStreamWriter writer) {
        try {
            writer.writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    protected void writeFault(SOAPFaultInfo instance, MessageInfo messageInfo, XMLStreamWriter writer) {
        throw new UnsupportedOperationException();
    }

}
