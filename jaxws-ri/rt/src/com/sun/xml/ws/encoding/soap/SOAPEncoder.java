/*
 * $Id: SOAPEncoder.java,v 1.30 2005-09-23 22:05:28 kohsuke Exp $
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
package com.sun.xml.ws.encoding.soap;

import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
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
import com.sun.xml.ws.encoding.ByteArray;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.spi.runtime.MtomCallback;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.handler.HandlerContext;

import static com.sun.xml.ws.client.BindingProviderProperties.JAXB_OUTPUTSTREAM;
import java.io.IOException;

/**
 * @author WS Development Team
 */
public abstract class SOAPEncoder implements Encoder, InternalSoapEncoder {

    /*
     * @see Encoder#encodeAndSend(MessageInfo)
     */
    public void encodeAndSend(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see Encoder#encode(MessageInfo)
     */
    public ByteBuffer encode(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        return null;
    }

    public DOMSource toDOMSource(JAXBBridgeInfo bridgeInfo, MessageInfo messageInfo) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        Document doc = DOMUtil.createDom();
        JAXBTypeSerializer.getInstance().serialize(bridgeInfo, bridgeContext,
            doc);
        return new DOMSource(doc);
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
            writer.close();
            baos.close();
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
        } catch (IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    protected void writeRpcLitPayload(RpcLitPayload rpcLitPayload, MessageInfo messageInfo,
        XMLStreamWriter writer) {        
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, messageInfo, writer);
    }

    protected void writeRpcLitPayload(RpcLitPayload rpcLitPayload, MessageInfo messageInfo,
        OutputStream writer) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, writer);
    }

    protected void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, MessageInfo messageInfo,
        XMLStreamWriter writer) 
    {
        // Pass output stream directly to JAXB when available
        OutputStream os = (OutputStream) messageInfo.getMetaData(JAXB_OUTPUTSTREAM);
        
        if (os != null) {
            try {
                /*
                 * Make sure that current element is closed before passing the
                 * output stream to JAXB. Using Zephyr, it suffices to write
                 * an empty string (TODO: other StAX impls?).
                 */
                writer.writeCharacters("");
                
                // Flush output of StAX serializer
                writer.flush();
            }
            catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
            
            JAXBTypeSerializer.getInstance().serialize(
                    beanInfo.getBean(), os, beanInfo.getJAXBContext());            
        }
        else {
            JAXBTypeSerializer.getInstance().serialize(
                    beanInfo.getBean(), writer, beanInfo.getJAXBContext());
        }
    }

    protected void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, MessageInfo messageInfo,
        OutputStream writer)
    {
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
        
        // Pass output stream directly to JAXB when available
        OutputStream os = (OutputStream) messageInfo.getMetaData(JAXB_OUTPUTSTREAM);
        if (os != null) {
            try {
                /*
                 * Make sure that current element is closed before passing the
                 * output stream to JAXB. Using Zephyr, it suffices to write
                 * an empty string (TODO: other StAX impls?).
                 */
                writer.writeCharacters("");
                
                // Flush output of StAX serializer
                writer.flush();
            }
            catch (XMLStreamException e) {
                throw new WebServiceException(e);
            }
            
            JAXBTypeSerializer.getInstance().serialize(bridgeInfo, bridgeContext, os);            
        }
        else {
            JAXBTypeSerializer.getInstance().serialize(bridgeInfo, bridgeContext, writer);
        }
    }

    protected void writeJAXBBridgeInfo(JAXBBridgeInfo bridgeInfo,
        MessageInfo messageInfo, OutputStream writer) {
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
            if (bodyBlock == null)
                return soapMessage;
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

    public static void serializeReader(XMLStreamReader reader, XMLStreamWriter writer) {
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

                            //Workaround for soapenv:Detail, Zephyr doesnt write the unknown prefix
                            if(uri.equals(SOAP12Constants.QNAME_FAULT_DETAIL.getNamespaceURI()) &&
                                    localName.equals("Detail")){
                                prefix = SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE;
                            }
                            
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
                            String attrPrefix = reader.getAttributePrefix(i);
                            String attrURI = reader.getAttributeNamespace(i);
                            
                            writer.writeAttribute(attrPrefix != null ? attrPrefix : "", 
                                attrURI != null ? attrURI : "",
                                reader.getAttributeLocalName(i), 
                                reader.getAttributeValue(i)); 
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

    protected void serializeSource(Source source, OutputStream writer) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(source, new StreamResult(writer));
        }
        catch (Exception e) {
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
                if (value instanceof JAXBBridgeInfo) {
                    writeJAXBBridgeInfo((JAXBBridgeInfo)value, messageInfo, writer);
                } else if (value instanceof RpcLitPayload) {
                    writeRpcLitPayload((RpcLitPayload)value, messageInfo, writer);
                } else if (value instanceof Source) {
                    serializeSource((Source)value, writer);
                } else if (value instanceof SOAPFaultInfo) {
                    writeFault((SOAPFaultInfo)value, messageInfo, writer);
                } else if (value instanceof JAXBBeanInfo) {
                    writeJAXBBeanInfo((JAXBBeanInfo)value, messageInfo, writer);
                }else {
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
            am.setHandlerContaxt(((RuntimeContext)rtc).getHandlerContext());

            HandlerContext hc = ((RuntimeContext)rtc).getHandlerContext();
            Object mtomThreshold = null;
            if(hc == null){
                //to be removed when client guarantees handlerContext
                mtomThreshold = mi.getMetaData(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
            }else{
                mtomThreshold = hc.getMessageContext().get(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
            }
            if(mtomThreshold != null)
                am.setMtomThresholdValue((Integer)mtomThreshold);
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
            if(value instanceof DataHandler){
                ap.setDataHandler((DataHandler)value);
            }else if(value instanceof byte[]){
                byte[] data = (byte[])value;
                ap.setRawContentBytes(data, 0, data.length, block.getType());
            }else if(value instanceof ByteArray){
                ByteArray data = (ByteArray) value;
                ap.setRawContentBytes(data.bytes, data.offset, data.length, block.getType());
            }else{
                ap.setContent(value, block.getType());
            }
            ap.setContentId(id);
            //it may be safe to say the encoding is binary meaning the bytes are not subjected any 
            // specific encoding.
            ap.setMimeHeader("Content-transfer-encoding", "binary");
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

    protected void writeFault(SOAPFaultInfo instance, MessageInfo messageInfo, OutputStream out) {
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(out);
        writeFault(instance, messageInfo, writer);
    }


    public void write(Object value, Object obj, OutputStream writer, MtomCallback mtomCallback){
        if(!(obj instanceof MessageInfo))
            throw new SerializationException("incorrect.messageinfo", obj.getClass().getName());
        MessageInfo mi = (MessageInfo)obj;
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if(rtc != null){
            BridgeContext bc = ((RuntimeContext)rtc).getBridgeContext();
            if(bc == null)
                return;
            JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)((RuntimeContext)rtc).getBridgeContext().getAttachmentMarshaller();
            am.setMtomCallback(mtomCallback);
        }
        if (value instanceof JAXBBridgeInfo) {
            writeJAXBBridgeInfo((JAXBBridgeInfo)value, mi, writer);
        } else if (value instanceof RpcLitPayload) {
            writeRpcLitPayload((RpcLitPayload)value, mi, writer);
        } else if (value instanceof Source) {
            serializeSource((Source)value, writer);
        } else if (value instanceof SOAPFaultInfo) {
            writeFault((SOAPFaultInfo)value, mi, writer);
        } else if (value instanceof JAXBBeanInfo) {
            writeJAXBBeanInfo((JAXBBeanInfo)value, mi, writer);
        }else {
            throw new SerializationException("unknown.object", value.getClass().getName());
        }
    }
    public void write(Object value, Object obj, XMLStreamWriter writer, MtomCallback mtomCallback){
        if(!(obj instanceof MessageInfo))
            throw new SerializationException("incorrect.messageinfo", obj.getClass().getName());
        MessageInfo mi = (MessageInfo)obj;
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if(rtc != null){
            BridgeContext bc = ((RuntimeContext)rtc).getBridgeContext();
            if(bc == null)
                return;
            JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)((RuntimeContext)rtc).getBridgeContext().getAttachmentMarshaller();
            am.setMtomCallback(mtomCallback);
        }
        if (value instanceof JAXBBridgeInfo) {
            writeJAXBBridgeInfo((JAXBBridgeInfo)value, mi, writer);
        } else if (value instanceof RpcLitPayload) {
            writeRpcLitPayload((RpcLitPayload)value, mi, writer);
        } else if (value instanceof Source) {
            serializeSource((Source)value, writer);
        } else if (value instanceof SOAPFaultInfo) {
            writeFault((SOAPFaultInfo)value, mi, writer);
        } else if (value instanceof JAXBBeanInfo) {
            writeJAXBBeanInfo((JAXBBeanInfo)value, mi, writer);
        }else {
            throw new SerializationException("unknown.object", value.getClass().getName());
        }
    }
}
