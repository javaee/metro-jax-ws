/*
 * $Id: SOAPXMLDecoder.java,v 1.2 2005-05-24 17:48:11 vivekp Exp $
 */

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.transport.Connection;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.streaming.*;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * @author JAX-RPC RI Development Team
 */

public class SOAPXMLDecoder extends SOAPDecoder {

    //private Method methodName = null;
    private static final XMLReaderFactory factory = XMLReaderFactory.newInstance();

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public void decode(MessageInfo messageInfo) {
        receiveAndDecode(messageInfo);
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#receiveAndDecode(com.sun.pept.ept.MessageInfo)
     */
    /*
    public void receiveAndDecode(MessageInfo messageInfo) {
        Connection connection = messageInfo.getConnection();
        ByteBuffer responseBuffer = connection.readUntilEnd();
        ByteInputStream inputStream = new ByteInputStream(responseBuffer.array(),
                responseBuffer.array().length);
        //methodName = messageInfo.getMethod();
        XMLReader reader = factory.createXMLReader(inputStream);
        reader.nextElementContent();
        decodeEnvelope(reader, messageInfo);
    }
    */
    public void receiveAndDecode(MessageInfo messageInfo) {


        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) !=
            Service.Mode.MESSAGE) {
            Connection connection = messageInfo.getConnection();
            ByteBuffer responseBuffer = connection.readUntilEnd();
            ByteInputStream inputStream = new ByteInputStream(responseBuffer.array(),
                responseBuffer.array().length);

            //methodName = messageInfo.getMethod();
            XMLReader reader = factory.createXMLReader(inputStream);
            reader.nextElementContent();
            decodeEnvelope(reader, messageInfo);
        } else {
            SOAPMessage sm = toSOAPMessage(messageInfo);
            messageInfo.setResponse(sm);
        }
    }

    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        SOAPConnection connection = (SOAPConnection) messageInfo.getConnection();
        SOAPMessage sm = connection.getSOAPMessage(messageInfo);

        return sm;
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) {
        RuntimeContext rtContext =
            (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXRPC_RUNTIME_CONTEXT);
        LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
        InternalEncoder encoder = eptf.getInternalEncoder();
        encoder.toMessageInfo(internalMessage, messageInfo);
    }

    protected void decodeEnvelope(XMLReader reader, MessageInfo messageInfo) {
        InternalMessage im = decodeInternalMessage(reader, messageInfo);
        toMessageInfo(im, messageInfo);
    }

    protected InternalMessage decodeInternalMessage(XMLReader reader, MessageInfo messageInfo) {
        InternalMessage response = new InternalMessage();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, getEnvelopeTag());
        reader.nextElementContent();
        decodeHeader(reader, messageInfo, response);
        decodeBody(reader, response, messageInfo);
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, getEnvelopeTag());
        reader.nextElementContent();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.EOF);

        return response;
    }

    public InternalMessage toInternalMessage(SOAPMessage soapMessage, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLReader reader = null;
        try {
            InternalMessage response = new InternalMessage();
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = factory.createXMLReader(source, true);
            reader.nextElementContent();
            decodeEnvelope(reader, response, false, messageInfo);
            return response;
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                                             InternalMessage response, MessageInfo messageInfo) {

        // TODO handle exceptions, attachments
        XMLReader reader = null;
        try {
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = factory.createXMLReader(source, true);
            reader.nextElementContent();
            decodeEnvelope(reader, response, true, messageInfo);
            convertBodyBlock(response, messageInfo);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return response;

    }

    /**
     * @return Returns the soap binding - SOAP 1.1 namespace.
     */
    public String getSOAPBindingId() {
        return SOAPConstants.NS_WSDL_SOAP;
    }

    protected SOAPFaultInfo decodeFault(XMLReader reader, InternalMessage internalMessage,
                                        MessageInfo messageInfo) {

        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);

        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        Method methodName = messageInfo.getMethod();

        // faultcode
        reader.nextElementContent();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_CODE);
        reader.nextContent();
        QName faultcode = null;
        String tokens = reader.getValue();
        String uri = "";
        tokens = EncoderUtils.collapseWhitespace(tokens);
        String prefix = XmlUtil.getPrefix(tokens);
        if (prefix != null) {
            uri = reader.getURI(prefix);
            if (uri == null) {
                throw new DeserializationException("xsd.unknownPrefix", prefix);
            }
        }
        String localPart = XmlUtil.getLocalPart(tokens);
        faultcode = new QName(uri, localPart);
        reader.next();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_CODE);

        // faultstring
        reader.nextElementContent();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_STRING);
        reader.nextContent();
        String faultstring = reader.getValue();
        reader.next();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_STRING);

        String faultactor = null;
        Object faultdetail = null;
        QName faultName = null;
        if (reader.nextElementContent() == XMLReader.START) {
            QName elementName = reader.getName();
            // faultactor
            if (elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_ACTOR)) {
                reader.nextContent();
                // faultactor may be empty
                if (reader.getState() == XMLReader.CHARS) {
                    faultactor = reader.getValue();
                    reader.next();
                }
                XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
                XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_ACTOR);
                reader.nextElementContent();
                elementName = reader.getName();
            }

            // faultdetail
            if (elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL)) {
                reader.nextContent();
                faultName = reader.getName();

                if (((SOAPRuntimeModel) rtCtxt.getModel()).isKnownFault(faultName, methodName)) {
                    Object decoderInfo = rtCtxt.getDecoderInfo(faultName);
                    if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                        JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                        // JAXB leaves on </env:Header> or <nextHeaderElement>
                        JAXBTypeSerializer.getInstance().deserialize(reader, bridgeInfo,
                            rtCtxt.getBridgeContext());
                        faultdetail = bridgeInfo;
                    }

                    // all the siblings are assigned the same elementID, though
                    // not at the same time. the parent is assigned elementID
                    // one less than the child.

                    // this will skip the subsequent detail entries
                    // and position the reader at </detail>
                    elementName = reader.getName();
                    if (!elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL))
                        reader.skipElement(reader.getElementId() - 1);
                } else {
                    faultdetail = decodeFaultDetail(reader);
                }
                reader.next();
            } else {
                if (internalMessage.getHeaders() != null) {
                    boolean isHeaderFault = false;
                    // could be a header fault or a protocol exception with no detail
                    for (HeaderBlock headerBlock : internalMessage.getHeaders()) {
                        if (((SOAPRuntimeModel) rtCtxt.getModel()).isKnownFault(headerBlock.getName(), methodName)) {
                            isHeaderFault = true;
                            faultdetail = headerBlock.getValue();
                        }
                    }

                    // if not a header fault, then it is a protocol exception with no detail
                    if (!isHeaderFault) {
                        faultdetail = null;
                    }
                    reader.next();
                }
            }
        } else {
            // a header fault (with no faultactor)
            if (internalMessage.getHeaders() != null) {
                for (HeaderBlock headerBlock : internalMessage.getHeaders()) {
                    if (((SOAPRuntimeModel) rtCtxt.getModel()).isKnownFault(headerBlock.getName(), methodName)) {
                        faultdetail = headerBlock.getValue();
                    }
                }
            }
        }

        //SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);
        SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);

        // reader could be left on CHARS token rather than </fault>
        if (reader.getState() == XMLReader.CHARS &&
            reader.getValue().trim().length() == 0) {
            reader.nextContent();
        }

        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        reader.nextElementContent();

        return soapFaultInfo;
    }

    private Detail decodeFaultDetail(XMLReader reader) {
        Detail detail = null;

        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            detail = soapFactory.createDetail();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLWriter writer = XMLWriterFactory.newInstance().createXMLWriter(baos);

            writer.startElement(SOAPConstants.QNAME_SOAP_FAULT_DETAIL.getLocalPart());
            while (!((reader.getState() == XMLReader.END) && reader.getName().equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL))) {
                if (reader.getState() == XMLReader.START) {
                    QName name = reader.getName();
                    writer.startElement(name.getLocalPart(), name.getNamespaceURI(), name.getPrefix());
                    Attributes atts = reader.getAttributes();
                    writer.flush();
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (atts.isNamespaceDeclaration(i)) {
                            // namespace declaration for the element is written during previous writeElement
                            if (!name.getPrefix().equals(atts.getName(i).getLocalPart()))
                                writer.writeNamespaceDeclaration(atts.getName(i).getLocalPart(), atts.getValue(i));
                        } else {
                            writer.writeAttribute(atts.getLocalName(i), atts.getURI(i), atts.getValue(i));
                        }
                    }
                } else if (reader.getState() == XMLReader.END) {
                    writer.endElement();
                } else if (reader.getState() == XMLReader.CHARS) {
                    writer.writeChars(reader.getValue());
                }
                reader.next();
            }
            writer.endElement();    // detail

            writer.flush();
            writer.close();

            DOMResult dom = new DOMResult();
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperties(oprops);
            trans.transform(new StreamSource(new ByteArrayInputStream(baos.toString().getBytes())), dom);

            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            SOAPBody soapBody = soapMessage.getSOAPBody();
            soapBody.addDocument((Document) dom.getNode());
            SOAPElement soapElement = (SOAPElement) soapBody.getFirstChild();
            soapBody.removeContents();

            SOAPFault fault = soapBody.addFault();
            detail = fault.addDetail();
            detail.addChildElement(soapElement);
        } catch (SOAPException e) {
            throw new SenderException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        } catch (TransformerException e) {
            throw new SenderException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        } catch (TransformerFactoryConfigurationError e) {
            throw new SenderException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        }

        return detail;
    }


    /*
     * Throws RuntimeException
     */
    protected void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(faultCode, faultString, null, null);
    }
}

