/*
 * $Id: SOAPXMLDecoder.java,v 1.8 2005-06-09 15:51:31 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 *
 */

package com.sun.xml.ws.client;

import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

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
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.streaming.SourceReaderFactory;

import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;

import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.SOAPConnection;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Properties;

import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author JAX-RPC RI Development Team
 */

public class SOAPXMLDecoder extends SOAPDecoder {

    //private Method methodName = null;

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
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);
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

            // TODO: Decide between XML and FI
            XMLStreamReader reader =
                    XMLStreamReaderFactory.createXMLStreamReader(inputStream, true);
            XMLStreamReaderUtil.nextElementContent(reader);
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
            (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
        InternalEncoder encoder = eptf.getInternalEncoder();
        encoder.toMessageInfo(internalMessage, messageInfo);
    }

    protected void decodeEnvelope(XMLStreamReader reader, MessageInfo messageInfo) {
        InternalMessage im = decodeInternalMessage(reader, messageInfo);
        toMessageInfo(im, messageInfo);
    }

    protected InternalMessage decodeInternalMessage(XMLStreamReader reader, MessageInfo messageInfo) {
        InternalMessage response = new InternalMessage();

        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        decodeHeader(reader, messageInfo, response);
        decodeBody(reader, response, messageInfo);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_DOCUMENT);

        return response;
    }

    public InternalMessage toInternalMessage(SOAPMessage soapMessage, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            InternalMessage response = new InternalMessage();
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();

            reader = SourceReaderFactory.createSourceReader(source, true);
            XMLStreamReaderUtil.nextElementContent(reader);
            decodeEnvelope(reader, response, false, messageInfo);
            return response;
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        } finally {
            if (reader != null) {
                XMLStreamReaderUtil.close(reader);
            }
        }
        return null;
    }

    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                                             InternalMessage response, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = SourceReaderFactory.createSourceReader(source, true);
            XMLStreamReaderUtil.nextElementContent(reader);
            decodeEnvelope(reader, response, true, messageInfo);
            convertBodyBlock(response, messageInfo);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        } finally {
            if (reader != null) {
                XMLStreamReaderUtil.close(reader);
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

    protected SOAPFaultInfo decodeFault(XMLStreamReader reader, InternalMessage internalMessage,
                                        MessageInfo messageInfo) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);

        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        Method methodName = messageInfo.getMethod();

        // faultcode
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_CODE);
        XMLStreamReaderUtil.nextContent(reader);
        QName faultcode = null;
        String tokens = reader.getText();
        String uri = "";
        tokens = EncoderUtils.collapseWhitespace(tokens);
        String prefix = XmlUtil.getPrefix(tokens);
        if (prefix != null) {
            uri = reader.getNamespaceURI(prefix);
            if (uri == null) {
                throw new DeserializationException("xsd.unknownPrefix", prefix);
            }
        }
        String localPart = XmlUtil.getLocalPart(tokens);
        faultcode = new QName(uri, localPart);
        XMLStreamReaderUtil.next(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_CODE);

        // faultstring
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_STRING);
        XMLStreamReaderUtil.nextContent(reader);
        String faultstring = reader.getText();
        XMLStreamReaderUtil.next(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_STRING);

        String faultactor = null;
        Object faultdetail = null;
        QName faultName = null;
        if (XMLStreamReaderUtil.nextElementContent(reader) == START_ELEMENT) {
            QName elementName = reader.getName();
            // faultactor
            if (elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_ACTOR)) {
                XMLStreamReaderUtil.nextContent(reader);
                // faultactor may be empty
                if (reader.getEventType() == CHARACTERS) {
                    faultactor = reader.getText();
                    XMLStreamReaderUtil.next(reader);
                }
                XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
                XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT_ACTOR);
                XMLStreamReaderUtil.nextElementContent(reader);
                elementName = reader.getName();
            }

            // faultdetail
            if (elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL)) {
                XMLStreamReaderUtil.nextContent(reader);
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
                    if (!elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL)) {
                        XMLStreamReaderUtil.skipSiblings(reader);
                    }
                } else {
                    faultdetail = decodeFaultDetail(reader);
                }
                XMLStreamReaderUtil.next(reader);
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
                    XMLStreamReaderUtil.next(reader);
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
        if (reader.getEventType() == CHARACTERS && reader.isWhiteSpace()) {
            XMLStreamReaderUtil.nextContent(reader);
        }

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent(reader);

        return soapFaultInfo;
    }

    private Detail decodeFaultDetail(XMLStreamReader reader) {
        Detail detail = null;

        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            detail = soapFactory.createDetail();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

            writer.writeStartElement(SOAPConstants.QNAME_SOAP_FAULT_DETAIL.getLocalPart());
            while (!((reader.getEventType() == END_ELEMENT) &&
                    reader.getName().equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL)))
            {
                if (reader.getEventType() == START_ELEMENT) {
                    QName name = reader.getName();
                    writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                    Attributes atts = XMLStreamReaderUtil.getAttributes(reader);
                    writer.flush();
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (atts.isNamespaceDeclaration(i)) {
                            String value = atts.getValue(i);
                            String localName = atts.getName(i).getLocalPart();
                            writer.setPrefix(localName, value);
                            writer.writeNamespace(localName, value);
                        } else {
                            writer.writeAttribute(atts.getPrefix(i), atts.getURI(i), atts.getLocalName(i),
                                atts.getValue(i));
                        }
                    }
                } else if (reader.getEventType() == END_ELEMENT) {
                    writer.writeEndElement();
                } else if (reader.getEventType() == CHARACTERS) {
                    writer.writeCharacters(reader.getText());
                }
                XMLStreamReaderUtil.next(reader);
            }
            writer.writeEndElement();    // detail
            writer.writeEndDocument();
            writer.flush();

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
        } catch (XMLStreamException e) {
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

