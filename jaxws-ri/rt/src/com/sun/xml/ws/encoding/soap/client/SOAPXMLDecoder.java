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

package com.sun.xml.ws.encoding.soap.client;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchSerializer;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Properties;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.ws.soap.SOAPBinding;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.SOAPConnectionUtil;
import com.sun.xml.ws.util.SOAPUtil;

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

/**
 * @author WS Development Team
 */

public class SOAPXMLDecoder extends SOAPDecoder {
    public SOAPXMLDecoder() {
    }

    protected DispatchSerializer getSerializerInstance(){
        return DispatchSerializer.SOAP_1_0;
    }

    @Override
    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        WSConnection connection = (WSConnection) messageInfo.getConnection();
        return SOAPConnectionUtil.getSOAPMessage(connection, messageInfo, getBindingId());
    }

    @Override
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
                
                if (responseBodyName.equals(getFaultTag())){
                    SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);
                    responseBody = new BodyBlock(soapFaultInfo);
                } else {
                    JAXBContext jaxbContext = getJAXBContext(messageInfo);
                    //jaxb will leave reader on ending </body> element
                    JAXBBeanInfo jaxBean = JAXBBeanInfo.fromStAX(reader, jaxbContext);
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

    @Override
    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) {

        RuntimeContext rtContext =
            (RuntimeContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtContext != null) {
            SOAPEPTFactory eptf = (SOAPEPTFactory) messageInfo.getEPTFactory();
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

    @Override
    public InternalMessage toInternalMessage(SOAPMessage soapMessage, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            InternalMessage response = new InternalMessage();
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = SourceReaderFactory.createSourceReader(source, true, getSOAPMessageCharsetEncoding(soapMessage));
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

    @Override
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                                             InternalMessage response, MessageInfo messageInfo) {
        // TODO handle exceptions, attachments
        XMLStreamReader reader = null;
        try {
            processAttachments(messageInfo, response, soapMessage);
            Source source = soapMessage.getSOAPPart().getContent();
            reader = SourceReaderFactory.createSourceReader(source, true, getSOAPMessageCharsetEncoding(soapMessage));
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

    @Override
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
                if (rtCtxt.getModel().isKnownFault(faultName, methodName)) {
                    Object decoderInfo = rtCtxt.getDecoderInfo(faultName);
                    if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                        JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                        // JAXB leaves on </env:Header> or <nextHeaderElement>
                        bridgeInfo.deserialize(reader,rtCtxt.getBridgeContext());
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
                        if (rtCtxt.getModel().isKnownFault(headerBlock.getName(), methodName)) {
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
                    if (rtCtxt.getModel().isKnownFault(headerBlock.getName(), methodName)) {
                        faultdetail = headerBlock.getValue();
                    }
                }
            }
        }


        SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultstring, faultcode, faultactor, faultdetail, getBindingId());

        // reader could be left on CHARS token rather than </fault>
        if (reader.getEventType() == CHARACTERS && reader.isWhiteSpace()) {
            XMLStreamReaderUtil.nextContent(reader);
        }

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent(reader);

        return soapFaultInfo;
    }

    protected Detail decodeFaultDetail(XMLStreamReader reader) {
        Detail detail;

        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            detail = soapFactory.createDetail();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

            writer.writeStartElement(getFaultDetailTag().getLocalPart());
            while (!((reader.getEventType() == END_ELEMENT) &&
                reader.getName().equals(getFaultDetailTag()))) {
                if (reader.getEventType() == START_ELEMENT) {
                    QName name = reader.getName();
                    writer.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                    String uri = reader.getNamespaceURI(name.getPrefix());
                    if (uri != null) {
                        writer.setPrefix(name.getPrefix(), name.getNamespaceURI());
                        writer.writeNamespace(name.getPrefix(), name.getNamespaceURI());
                    }
                    Attributes atts = XMLStreamReaderUtil.getAttributes(reader);
                    writer.flush();
                    for (int i = 0; i < atts.getLength(); i++) {
                        if (atts.isNamespaceDeclaration(i)) {
                            String value = atts.getValue(i);
                            String localName = atts.getName(i).getLocalPart();
                            if (writer.getPrefix(value) == null) {
                                writer.setPrefix(localName, value);
                                writer.writeNamespace(localName, value);
                            }
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
            throw new WebServiceException("sender.response.cannotDecodeFaultDetail", e);
        } catch (TransformerException e) {
            e.printStackTrace();
            throw new WebServiceException("sender.response.cannotDecodeFaultDetail", e);
        } catch (TransformerFactoryConfigurationError e) {
             throw new WebServiceException("sender.response.cannotDecodeFaultDetail", e);
        } catch (XMLStreamException e) {
             throw new WebServiceException("sender.response.cannotDecodeFaultDetail", e);
        }

        return detail;
    }


    /*
     * Throws RuntimeException
     */
    protected void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(SOAPUtil.createSOAPFault(faultString, faultCode, null, null, SOAPBinding.SOAP11HTTP_BINDING));
    }

    protected JAXBContext getJAXBContext(MessageInfo messageInfo) {
        JAXBContext jc = null;
        
        RequestContext requestContext = (RequestContext) messageInfo.getMetaData (BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        if (requestContext != null)
            // TODO: does this really need to be copied?
            jc = (JAXBContext)requestContext.copy ().get (BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        return jc;
    }

    protected String getBindingId(MessageInfo messageInfo){
        RequestContext requestContext = (RequestContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        String bindingId = null;
        if (requestContext != null){
            bindingId = (String)requestContext.get(BindingProviderProperties.BINDING_ID_PROPERTY);
            if (bindingId != null)
                return bindingId;
        }
        return getBindingId();
    }
}

