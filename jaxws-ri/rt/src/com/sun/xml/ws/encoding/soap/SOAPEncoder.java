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

import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXB_OUTPUTSTREAM;
import com.sun.xml.ws.client.ContextMap;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.JAXWSAttachmentUnmarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayloadSerializer;
import com.sun.xml.ws.encoding.soap.internal.*;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.handler.SOAPHandlerContext;
import com.sun.xml.ws.pept.encoding.Encoder;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.spi.runtime.InternalSoapEncoder;
import com.sun.xml.ws.spi.runtime.MtomCallback;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

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
        bridgeInfo.serialize(bridgeContext, doc);
        return new DOMSource(doc);
    }

    public DOMSource toDOMSource(RpcLitPayload rpcLitPayload, MessageInfo messageInfo) {
        try {
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
            writeRpcLitPayload(rpcLitPayload, messageInfo, writer);
            writer.close();
            baos.close();
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(baos.newInputStream());
            DOMResult domResult = new DOMResult();
            transformer.transform(source, domResult);
            return new DOMSource(domResult.getNode());
        } catch (TransformerException te) {
            throw new WebServiceException(te);
        } catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

    public DOMSource toDOMSource(SOAPFaultInfo faultInfo, MessageInfo messageInfo) {
        try {
            ByteArrayBuffer baos = new ByteArrayBuffer();
            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
            writeFault(faultInfo, messageInfo, writer);
            writer.writeEndDocument();
            writer.close();
            baos.close();
            Transformer transformer = XmlUtil.newTransformer();
            StreamSource source = new StreamSource(baos.newInputStream());
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
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, messageInfo, writer);
    }

    protected void writeRpcLitPayload(RpcLitPayload rpcLitPayload, MessageInfo messageInfo,
                                      OutputStream writer) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        RpcLitPayloadSerializer.serialize(rpcLitPayload, bridgeContext, writer);
    }
    protected JAXBContext getJAXBContext(MessageInfo messageInfo) {
            JAXBContext jc = null;
            RequestContext context = (RequestContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
            if (context != null)
                jc = (JAXBContext) context.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);

            return jc;
        }

    private void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, MessageInfo messageInfo,
                                   XMLStreamWriter writer) {
        // Pass output stream directly to JAXB when available
        OutputStream os = (OutputStream) messageInfo.getMetaData(JAXB_OUTPUTSTREAM);
        BridgeContext bc = (BridgeContext) messageInfo.getMetaData("dispatch.bridge.context");
        if (bc != null)
                beanInfo.setBridgeContext(bc);
        Marshaller m = (Marshaller) messageInfo.getMetaData("dispatch.beaninfo.marshaller");
        Unmarshaller u = (Unmarshaller) messageInfo.getMetaData("dispatch.beaninfo.unmarshaller");
        beanInfo.setMarshallers(m, u);
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

            
            beanInfo.writeTo(os);

        } else {

            beanInfo.writeTo(writer);
        }
    }

    private void writeJAXBBeanInfo(JAXBBeanInfo beanInfo, OutputStream writer) {
        beanInfo.writeTo(writer);
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

            bridgeInfo.serialize(bridgeContext, os, writer.getNamespaceContext());
        } else {
            bridgeInfo.serialize(bridgeContext, writer);
        }
    }

    protected void writeJAXBBridgeInfo(JAXBBridgeInfo bridgeInfo,
                                       MessageInfo messageInfo, OutputStream writer) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        bridgeInfo.serialize(bridgeContext, writer, null);
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
                Source source = (Source) value;
                SOAPBody body = soapMessage.getSOAPBody();
                body.removeContents();

                Transformer transformer = XmlUtil.newTransformer();
                transformer.transform(source, new DOMResult(body));
            } else {
                throw new UnsupportedOperationException("Unknown object in BodyBlock:" + value.getClass());
            }
            return soapMessage;
        } catch (Exception e) {
            throw new ServerRtException("soapencoder.err", new Object[]{e});
        }
    }

    public static void serializeReader(XMLStreamReader reader, XMLStreamWriter writer) {
        try {
            int state;
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
                            } else {
                                writer.writeStartElement(uri, localName);
                            }
                        } else {
                            assert uri != null;

                            if (prefix.length() > 0) {
                                /**
                                 * Before we write the
                                 */
                                String writerURI = null;
                                if (writer.getNamespaceContext() != null)
                                    writerURI = writer.getNamespaceContext().getNamespaceURI(prefix);
                                String writerPrefix = writer.getPrefix(uri);
                                if (declarePrefix(prefix, uri, writerPrefix, writerURI))
                                {
                                    writer.writeStartElement(prefix, localName, uri);
                                    writer.setPrefix(prefix, uri != null ? uri : "");
                                    writer.writeNamespace(prefix, uri);
                                } else {
                                    writer.writeStartElement(prefix, localName, uri);
                                }
                            } else {
                                writer.writeStartElement(prefix, localName, uri);
                            }
                        }

                        int n = reader.getNamespaceCount();
                        // Write namespace declarations
                        for (int i = 0; i < n; i++) {
                            String nsPrefix = reader.getNamespacePrefix(i);
                            if (nsPrefix == null) nsPrefix = "";
                            // StAX returns null for default ns
                            String writerURI = null;
                            if (writer.getNamespaceContext() != null)
                                writerURI = writer.getNamespaceContext().getNamespaceURI(nsPrefix);

                            // Zephyr: Why is this returning null?
                            // Compare nsPrefix with prefix because of [1] (above)
                            String readerURI = reader.getNamespaceURI(i);

                            /**
                             * write the namespace in 3 conditions
                             *  - when the namespace URI is not bound to the prefix in writer(writerURI == 0)
                             *  - when the readerPrefix and writerPrefix are ""
                             *  - when readerPrefix and writerPrefix are not equal and the URI bound to them
                             *    are different
                             */
                            if (writerURI == null || ((nsPrefix.length() == 0) || (prefix.length() == 0)) ||
                                (!nsPrefix.equals(prefix) && !writerURI.equals(readerURI)))
                            {
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
                            // if the attribute prefix is undeclared in current writer scope then declare it
                            setUndeclaredPrefix(attrPrefix, attrURI, writer);
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

    /**
     * sets undeclared prefixes on the writer
     *
     * @param prefix
     * @param writer
     * @throws XMLStreamException
     */
    private static void setUndeclaredPrefix(String prefix, String readerURI, XMLStreamWriter writer) throws XMLStreamException {
        String writerURI = null;
        if (writer.getNamespaceContext() != null)
            writerURI = writer.getNamespaceContext().getNamespaceURI(prefix);

        if (writerURI == null) {
            writer.setPrefix(prefix, readerURI != null ? readerURI : "");
            writer.writeNamespace(prefix, readerURI != null ? readerURI : "");
        }
    }

    /**
     * check if we need to declare
     *
     * @param rPrefix
     * @param rUri
     * @param wPrefix
     * @param wUri
     * @return
     */
    private static boolean declarePrefix(String rPrefix, String rUri, String wPrefix, String wUri) {
        if (wUri == null || ((wPrefix != null) && !rPrefix.equals(wPrefix)) ||
            (rUri != null && !wUri.equals(rUri)))
            return true;
        return false;
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
            Transformer t = XmlUtil.newTransformer();
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
     * write the known namespace declaration to the envelope
     */
    protected void writeEnvelopeNamespaces(XMLStreamWriter writer, MessageInfo messageInfo)
        throws XMLStreamException {

        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        if (rtCtxt != null && rtCtxt.getModel() != null) {
            writer.setPrefix("xsd", SOAPNamespaceConstants.XSD);
            writer.writeNamespace("xsd", SOAPNamespaceConstants.XSD);
            int i = 1;
            String prefix;
            for (String namespace : rtCtxt.getModel().getKnownNamespaceURIs()) {
                prefix = "ns" + i++;
                writer.setPrefix(prefix, namespace);
                writer.writeNamespace(prefix, namespace);
            }
            writer.writeCharacters("");
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
                                MessageInfo messageInfo) {
        try {
            List<HeaderBlock> headerBlocks = response.getHeaders();
            if (headerBlocks == null || headerBlocks.isEmpty()) {
                return;
            }
            startHeader(writer); // <env:Header>
            for (HeaderBlock headerBlock : headerBlocks) {
                Object value = headerBlock.getValue();
                if (value != null) {
                    if (value instanceof JAXBBridgeInfo) {
                        writeJAXBBridgeInfo((JAXBBridgeInfo) value,
                            messageInfo, writer);
                    } else {
                        throw new SerializationException("unknown.object",
                            value.getClass().getName());
                    }
                } else {
                    // currently only in soap 1.2
                    if (headerBlock instanceof SOAP12NotUnderstoodHeaderBlock) {
                        ((SOAP12NotUnderstoodHeaderBlock) headerBlock).write(
                            writer);
                    }
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
                             MessageInfo messageInfo) {
        try {
            startBody(writer);
            BodyBlock bodyBlock = response.getBody();
            // BodyBlock can be null if there is no part in wsdl:message
            if (bodyBlock != null) {
                Object value = bodyBlock.getValue();
                if (value instanceof JAXBBridgeInfo) {
                    writeJAXBBridgeInfo((JAXBBridgeInfo) value, messageInfo, writer);
                } else if (value instanceof RpcLitPayload) {
                    writeRpcLitPayload((RpcLitPayload) value, messageInfo, writer);
                } else if (value instanceof Source) {
                    serializeSource((Source) value, writer);
                } else if (value instanceof SOAPFaultInfo) {
                    writeFault((SOAPFaultInfo) value, messageInfo, writer);
                } else if (value instanceof JAXBBeanInfo) {                    
                    writeJAXBBeanInfo((JAXBBeanInfo) value, messageInfo, writer);
                } else if (value == null && (DispatchContext)
                    messageInfo.getMetaData(BindingProviderProperties.DISPATCH_CONTEXT) != null) {
                    //bug 6400596  -Dispatch and null invocation parameter payload mode
                    //can skip this here as we want to write and empty SOAPBody
                    //writeJAXBBeanInfo((JAXBBeanInfo) value, messageInfo, writer);
                } else {
                    throw new SerializationException("unknown.object",
                        value.getClass().getName());
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
    public void setAttachmentsMap(MessageInfo mi, InternalMessage im) {
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc == null)
                return;
            JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) ((RuntimeContext) rtc).getBridgeContext().getAttachmentMarshaller();
            am.setAttachments(im.getAttachments());
            am.setHandlerContaxt(((RuntimeContext) rtc).getHandlerContext());

            HandlerContext hc = ((RuntimeContext) rtc).getHandlerContext();
            Object mtomThreshold;
            if (hc == null) {
                //to be removed when client guarantees handlerContext
                mtomThreshold = mi.getMetaData(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
            } else {
                mtomThreshold = hc.getMessageContext().get(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
            }
            if (mtomThreshold != null)
                am.setMtomThresholdValue((Integer) mtomThreshold);
        } else if (mi.getMetaData(BindingProviderProperties.DISPATCH_CONTEXT) != null){
            ContextMap map = (ContextMap) mi.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
            BindingProvider provider = (BindingProvider)
                map.get(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);

            JAXBContext jc = (JAXBContext) map.get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);

            //com.sun.xml.bind.v2.runtime.BridgeContextImpl bc = getBridgeContext(jc);
            BridgeContext bc = getBridgeContext(jc);
            if (jc != null) {
                try {
                    Marshaller marshaller = jc.createMarshaller();
                    Unmarshaller unmarshaller = jc.createUnmarshaller();
                    JAXWSAttachmentMarshaller am = new JAXWSAttachmentMarshaller(((SOAPBinding) provider.getBinding()).isMTOMEnabled());
                    marshaller.setAttachmentMarshaller(am);
                    JAXWSAttachmentUnmarshaller uam = new JAXWSAttachmentUnmarshaller();
                    unmarshaller.setAttachmentUnmarshaller(uam);
                    mi.setMetaData("dispatch.beaninfo.marshaller", marshaller);
                    mi.setMetaData("dispatch.beaninfo.unmarshaller", unmarshaller);
                    am.setAttachments(im.getAttachments());
                    bc.setAttachmentMarshaller(am);
                    bc.setAttachmentUnmarshaller(uam);
                    //using this to hold attachment marshallers
                    mi.setMetaData("dispatch.bridge.context", bc);
                    //ok so where do I get handlerContext from
                    //right now it may be null
                    //right now assuming it comes from messageInfo if set
                    am.setHandlerContaxt((SOAPHandlerContext)mi.getMetaData("handler.context"));
                    
                    HandlerContext hc = null; //todo:temp for now//((RuntimeContext) rtc).getHandlerContext();
                    am.setXOPPackage(((SOAPBinding)provider.getBinding()).isMTOMEnabled());
                    uam.setXOPPackage(((SOAPBinding)provider.getBinding()).isMTOMEnabled());
                    Object mtomThreshold;
                    if (hc == null) {
                        //to be removed when client guarantees handlerContext
                        mtomThreshold = mi.getMetaData(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
                    } else {
                        mtomThreshold = hc.getMessageContext().get(JAXWSProperties.MTOM_THRESHOLOD_VALUE);
                    }
                    if (mtomThreshold != null)
                        am.setMtomThresholdValue((Integer) mtomThreshold);
                } catch (Exception e) {
                    throw new WebServiceException(e);
                }

            }

        }
    }
        /**
         * Add all the attachments in the InternalMessage to the SOAPMessage
         *
         * @param im
         * @param msg
         */
        protected void processAttachments
        (InternalMessage
        im, SOAPMessage
        msg) throws SOAPException
        {
            for (Map.Entry<String, AttachmentBlock> e : im.getAttachments().entrySet())
            {
                AttachmentBlock block = e.getValue();
                block.addTo(msg);
            }
        }

        /*
        * writes end tag of envelope: </env:Envelope>
        */
        protected void endEnvelope
        (XMLStreamWriter
        writer) {
        try {
            writer.writeEndElement();
        }
        catch (XMLStreamException e) {
            throw new WebServiceException(e);
        }
    }

        protected void writeFault
        (SOAPFaultInfo
        instance, MessageInfo
        messageInfo, XMLStreamWriter
        writer) {
        throw new UnsupportedOperationException();
    }

        protected void writeFault
        (SOAPFaultInfo
        instance, MessageInfo
        messageInfo, OutputStream
        out) {
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(out);
        writeFault(instance, messageInfo, writer);
    }


        public void write
        (Object
        value, Object
        obj, OutputStream
        writer, MtomCallback
        mtomCallback) {
        if (!(obj instanceof MessageInfo))
            throw new SerializationException("incorrect.messageinfo", obj.getClass().getName());
        MessageInfo mi = (MessageInfo) obj;
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) ((RuntimeContext) rtc).getBridgeContext().getAttachmentMarshaller();
                am.setMtomCallback(mtomCallback);
            }
        } else {
            //dispatch
            BridgeContext bc = (BridgeContext)mi.getMetaData("dispatch.bridge.context");
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)bc.getAttachmentMarshaller();
                am.setMtomCallback(mtomCallback);
            }
        }
        if (value instanceof JAXBBridgeInfo) {
            writeJAXBBridgeInfo((JAXBBridgeInfo) value, mi, writer);
        } else if (value instanceof RpcLitPayload) {
            writeRpcLitPayload((RpcLitPayload) value, mi, writer);
        } else if (value instanceof Source) {
            serializeSource((Source) value, writer);
        } else if (value instanceof SOAPFaultInfo) {
            writeFault((SOAPFaultInfo) value, mi, writer);
        } else if (value instanceof JAXBBeanInfo) {
            writeJAXBBeanInfo((JAXBBeanInfo) value, writer);
        } else {
            throw new SerializationException("unknown.object", value.getClass().getName());
        }
    }

        public void write
        (Object
        value, Object
        obj, XMLStreamWriter
        writer, MtomCallback
        mtomCallback) {
        if (!(obj instanceof MessageInfo))
            throw new SerializationException("incorrect.messageinfo", obj.getClass().getName());
        MessageInfo mi = (MessageInfo) obj;
        Object rtc = mi.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext();
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) ((RuntimeContext) rtc).getBridgeContext().getAttachmentMarshaller();
                am.setMtomCallback(mtomCallback);
            }
        } else {
            //dispatch
            BridgeContext bc = (BridgeContext)mi.getMetaData("dispatch.bridge.context");
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)bc.getAttachmentMarshaller();
                am.setMtomCallback(mtomCallback);
            }
        }
        if (value instanceof JAXBBridgeInfo) {
            writeJAXBBridgeInfo((JAXBBridgeInfo) value, mi, writer);
        } else if (value instanceof RpcLitPayload) {
            writeRpcLitPayload((RpcLitPayload) value, mi, writer);
        } else if (value instanceof Source) {
            serializeSource((Source) value, writer);
        } else if (value instanceof SOAPFaultInfo) {
            writeFault((SOAPFaultInfo) value, mi, writer);
        } else if (value instanceof JAXBBeanInfo) {
            writeJAXBBeanInfo((JAXBBeanInfo) value, mi, writer);
        } else {
            throw new SerializationException("unknown.object", value.getClass().getName());
        }
    }

        public BridgeContext getBridgeContext
        (JAXBContext
        jaxbContext) {
        BridgeContext bc = null;
        if (jaxbContext == null)
            return null;

        bc = ((JAXBRIContext) jaxbContext).createBridgeContext();
        bc.setAttachmentMarshaller(new JAXWSAttachmentMarshaller(true));
        bc.setAttachmentUnmarshaller(new JAXWSAttachmentUnmarshaller());
        //bridgeContext.set(bc);
        //}
        return bc;
    }

}       
