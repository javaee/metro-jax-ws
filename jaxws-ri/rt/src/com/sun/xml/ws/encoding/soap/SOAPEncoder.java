/*
 * $Id: SOAPEncoder.java,v 1.5 2005-05-26 18:48:18 vivekp Exp $
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
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
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLReader;
import com.sun.xml.ws.streaming.XMLReaderFactory;
import com.sun.xml.ws.streaming.XMLWriter;
import com.sun.xml.ws.streaming.XMLWriterFactory;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * @author JAX-RPC RI Development Team
 */
public abstract class SOAPEncoder implements Encoder {
    private static final XMLWriterFactory factory = XMLWriterFactory.newInstance();

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
            XMLWriter writer = factory.createXMLWriter(baos);
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
            XMLWriter writer = factory.createXMLWriter(baos);
            writeFault(faultInfo, messageInfo, writer);
            writer.close();
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

    protected void writeRpcLitPayload(RpcLitPayload rpcLitPayload, MessageInfo messageInfo,
        XMLWriter writer) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, writer);
    }

    protected void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, XMLWriter writer) {
        JAXBTypeSerializer.getInstance().serialize(
                beanInfo.getBean(), writer, beanInfo.getJAXBContext());
    }

    /*
    protected void writeJAXBTypeInfo(JAXBTypeInfo typeInfo, XMLWriter writer) {
        QName name = typeInfo.getName();
        Object value = typeInfo.getType();
        writeJAXBTypeInfo(name, value, writer);
    }


    protected void writeJAXBTypeInfo(QName name, Object value, XMLWriter writer) {
        JAXBContext jaxbContext = encoderDecoderUtil.getJAXBContext();
        Map<QName, Class> typeMapping = encoderDecoderUtil.getTypeMapping();
        Class type = typeMapping.get(name);
        JAXBTypeSerializer.getInstance().serialize(name, type, value,
                writer, jaxbContext);
    }
     */

    protected void writeJAXBBridgeInfo(JAXBBridgeInfo bridgeInfo,
        MessageInfo messageInfo, XMLWriter writer) {
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

    protected void serializeReader(XMLReader reader, XMLWriter writer) {
        int state = XMLReader.BOF;
        do {
            state = reader.next();
            switch (state) {
                case XMLReader.START:
                    QName elementName = reader.getName();
                    String localPart = elementName.getLocalPart();
                    String namespaceURI = elementName.getNamespaceURI();
                    String prefix = elementName.getPrefix();

                    //System.out.println("Localpart = " + localPart);
                    //System.out.println("namespaceURI = " + namespaceURI);
                    //System.out.println("Prefix = |" + prefix+"|");

                    writer.startElement(localPart, namespaceURI, prefix);
                    // Write namespace declarations
                    Iterator it = reader.getPrefixes();
                    while (it.hasNext()) {
                        String nsLocal = (String)it.next();
                        String writerURI = writer.getURI(nsLocal);
                        String readerURI = reader.getURI(nsLocal);
                        if (writerURI == null || !writerURI.equals(readerURI)) {
                            writer.writeNamespaceDeclaration(nsLocal, reader.getURI(nsLocal));
                        }
                    }

                    // Write rest of the attributes
                    Attributes attrs = reader.getAttributes();
                    for(int i=0; i < attrs.getLength(); i++) {
                        //System.out.println("Attr prefix " + attrs.getPrefix(i));
                        //System.out.println("Attr URI " + attrs.getURI(i));
                        //System.out.println("Attr name " + attrs.getName(i));
                        //System.out.println("Attr value --- " + attrs.getValue(i));
                        //System.out.println("Attr local " + attrs.getLocalName(i));

                        if (!attrs.isNamespaceDeclaration(i)) {
                            writer.writeAttribute(attrs.getName(i), attrs.getValue(i));
                        } else {
                            // Taking care of default NS
                            if (attrs.getPrefix(i) == null) {
                                writer.writeNamespaceDeclaration("", attrs.getValue(i));
                            }
                        }
                    }
                    break;
                case XMLReader.END:
                    writer.endElement();
                    break;
                case XMLReader.CHARS:
                    writer.writeChars(reader.getValue());
            }
        } while (state != XMLReader.EOF);
    }

    protected void serializeSource(Source source, XMLWriter writer) {
        XMLReader reader = XMLReaderFactory.newInstance().createXMLReader(source, true);
        serializeReader(reader, writer);
        reader.close();
    }

    /*
     * writes start tag of envelope: <env:Envelope>
     */
    protected void startEnvelope(XMLWriter writer) {
        //write SOAP Envelope
        writer.startElement(SOAPNamespaceConstants.TAG_ENVELOPE,
                SOAPNamespaceConstants.ENVELOPE,
                SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE);
    }

    /*
     * writes start tag of Body: <env:Body>
     */
    protected void startBody(XMLWriter writer) {
        //write SOAP Body
        writer.startElement(SOAPNamespaceConstants.TAG_BODY,
                SOAPNamespaceConstants.ENVELOPE,
                SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE);
    }

    /*
     * writes start tag of Header: <env:Header>
     */
    protected void startHeader(XMLWriter writer){
        writer.startElement(SOAPNamespaceConstants.TAG_HEADER,
                SOAPNamespaceConstants.ENVELOPE,
                SOAPNamespaceConstants.NSPREFIX_SOAP_ENVELOPE); // <env:Header>
    }

    /*
     * writes multiple header elements in <env:Header> ... </env:Header>
     */
    protected void writeHeaders(XMLWriter writer, InternalMessage response,
        MessageInfo messageInfo) {

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
        writer.endElement();                                // </env:Header>
    }

    /*
     * writes <env:Body> ... </env:Body>
     */
    protected void writeBody(XMLWriter writer, InternalMessage response,
        MessageInfo messageInfo) {

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
        writer.endElement();                // </env:body>
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
            AttachmentPart ap = msg.createAttachmentPart();
            ap.setRawContent(block.getValue(), block.getType());
            ap.setContentId(id);
            msg.addAttachmentPart(ap);
        }
    }

    /*
     * writes end tag of envelope: </env:Envelope>
     */
    protected void endEnvelope(XMLWriter writer) {
        writer.endElement();
    }

    protected void writeFault(SOAPFaultInfo instance, MessageInfo messageInfo, XMLWriter writer) {
        throw new UnsupportedOperationException();
    }

}
