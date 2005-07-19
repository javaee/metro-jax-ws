/*
 * $Id: SOAPXMLDecoder.java,v 1.6 2005-07-19 18:10:02 arungupta Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 *
 */

package com.sun.xml.ws.encoding.soap;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.pept.transport.Connection;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchSerializer;
import com.sun.xml.ws.client.dispatch.impl.encoding.SerializerIF;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.streaming.*;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.ws.soap.SOAPBinding;

/**
 * @author WS Development Team
 */

public class SOAPXMLDecoder extends SOAPDecoder {
    private JAXBContext jc;
    //private Method methodName = null;


    public SOAPXMLDecoder() {
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public void decode(MessageInfo messageInfo) {
        receiveAndDecode(messageInfo);
    }

    protected SerializerIF getSerializerInstance(){
        return DispatchSerializer.getInstance();
    }

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
        WSConnection connection = (WSConnection) messageInfo.getConnection();
        
        SOAPMessage sm = null;
        
        try {
            MessageFactory messageFactory = MessageFactory.newInstance ();
            // TODO: can the header on WSConnection be MimeHeaders instead of Map<String, List<String>>

            Map<String, List<String>> headers = connection.getHeaders ();
            MimeHeaders mimeHeaders = new MimeHeaders();
            for (String headerName : headers.keySet()) {
                MimeHeader mimeHeader = new MimeHeader(headerName,  headers.get(headerName).get(0));
            }
            sm = SOAPUtil.createMessage (mimeHeaders, connection.getInput(), getBindingId());
        } catch (SOAPException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return sm;
    }

    protected void decodeBody(XMLStreamReader reader, InternalMessage response, MessageInfo messageInfo) {
        DispatchContext context = (DispatchContext) messageInfo.getMetaData(BindingProviderProperties.DISPATCH_CONTEXT);
        if (context != null) {
            XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
            XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
            int state = XMLStreamReaderUtil.nextElementContent(reader);
            // if Body is not empty, then deserialize the Body
            if (state != END_ELEMENT) {
                BodyBlock responseBody = null;

                QName responseBodyName = reader.getName();   // Operation name
                if (responseBodyName.getNamespaceURI().equals(getEnvelopeTag()) &&
                    responseBodyName.getLocalPart().equals(getFaultTag())) {
                    SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);

                    responseBody = new BodyBlock(soapFaultInfo);
                } else {
                    JAXBContext jaxbContext = getJAXBContext(messageInfo);
                    //jaxb will leave reader on ending </body> element
                    Object jaxbBean =
                        getSerializerInstance().deserialize(reader,
                            jaxbContext);
                    JAXBBeanInfo jaxBean = new JAXBBeanInfo(jaxbBean, jaxbContext);
                    responseBody = new BodyBlock(jaxBean);
                }
                response.setBody(responseBody);
            }

            XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
            XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
            XMLStreamReaderUtil.nextElementContent(reader);
        } else
            super.decodeBody(reader, response, messageInfo);
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) {

        RuntimeContext rtContext =
            (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtContext != null) {
            LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
            InternalEncoder encoder = eptf.getInternalEncoder();
            encoder.toMessageInfo(internalMessage, messageInfo);

        } else {
            if (internalMessage.getBody().getValue() instanceof SOAPFaultInfo) {
                messageInfo.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
                messageInfo.setResponse(internalMessage.getBody().getValue());
            } else if (internalMessage.getBody().getValue() instanceof Exception) {
                messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
                messageInfo.setResponse(internalMessage.getBody().getValue());
            } else {
                messageInfo.setResponseType(MessageStruct.NORMAL_RESPONSE);
                //unfortunately we must do this
                if (internalMessage.getBody().getValue() instanceof JAXBBeanInfo)
                    messageInfo.setResponse(((JAXBBeanInfo) internalMessage.getBody().getValue()).getBean());
                else
                    messageInfo.setResponse(internalMessage.getBody().getValue());
            }
        }
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

    public String getBindingId() {
        return SOAPBinding.SOAP11HTTP_BINDING;
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
        String faultstring = null;
        if (reader.getEventType() == CHARACTERS) {
            faultstring = reader.getText();
            XMLStreamReaderUtil.next(reader);
        }
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

                    // Position the reader at </detail>
                    elementName = reader.getName();
                    if (!elementName.equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL)) {
                        XMLStreamReaderUtil.skipSiblings(reader,
                            SOAPConstants.QNAME_SOAP_FAULT_DETAIL);
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
                reader.getName().equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL))) {
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
            throw new WebServiceException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        } catch (TransformerException e) {
            throw new WebServiceException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        } catch (TransformerFactoryConfigurationError e) {
             throw new WebServiceException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        } catch (XMLStreamException e) {
             throw new WebServiceException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        }

        return detail;
    }


    /*
     * Throws RuntimeException
     */
    protected void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(faultCode, faultString, null, null);
    }

    protected JAXBContext getJAXBContext
        (MessageInfo
        messageInfo) {
        if (jc == null) {
            RequestContext requestContext = (RequestContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
            jc = (JAXBContext)
                requestContext.copy().get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        }
        return jc;
    }

    protected JAXBContext getJAXBContext() {
        return jc;
    }
}

