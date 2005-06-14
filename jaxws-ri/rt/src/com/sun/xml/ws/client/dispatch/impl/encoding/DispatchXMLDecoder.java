/*
 * $Id: DispatchXMLDecoder.java,v 1.12 2005-06-14 15:35:15 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client.dispatch.impl.encoding;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.client.BindingProviderProperties;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.SenderException;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.sun.pept.presentation.MessageStruct.UNCHECKED_EXCEPTION_RESPONSE;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 * @author JAX-RPC Development Team
 */

public class DispatchXMLDecoder extends com.sun.xml.ws.client.SOAPXMLDecoder {
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private static DispatchSerializer dispatchSerializer;
    //jaxbcontext can not be static
    private JAXBContext jc;

    public DispatchXMLDecoder() {
        dispatchSerializer = DispatchSerializer.getInstance();
    }

    private void displayDOM(org.w3c.dom.Node node, java.io.OutputStream ostream) {
        try {
            System.out.println("\n====\n");
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new javax.xml.transform.dom.DOMSource(node),
                new javax.xml.transform.stream.StreamResult(ostream));
            System.out.println("\n====\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void decodeBody(XMLStreamReader reader, InternalMessage response, MessageInfo messageInfo) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        int state = XMLStreamReaderUtil.nextElementContent(reader);
        // if Body is not empty, then deserialize the Body
        if (state != END_ELEMENT) {
            BodyBlock responseBody = null;

            QName responseBodyName = reader.getName();   // Operation name
            if (responseBodyName.getNamespaceURI().equals(SOAPNamespaceConstants.ENVELOPE) &&
                responseBodyName.getLocalPart().equals(SOAPNamespaceConstants.TAG_FAULT)) {
                SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);

                responseBody = new BodyBlock(soapFaultInfo);
            } else {
                JAXBContext jaxbContext = getJAXBContext(messageInfo);
                //jaxb will leave reader on ending </body> element
                Object jaxbBean =
                    dispatchSerializer.deserialize(reader,
                        jaxbContext);
                JAXBBeanInfo jaxBean = new JAXBBeanInfo(jaxbBean, jaxbContext);
                responseBody = new BodyBlock(jaxBean);
            }
            response.setBody(responseBody);
        }

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    /*
    * skipBody is true, the body is skipped during parsing.
    */
    protected void decodeEnvelope(XMLStreamReader reader, InternalMessage request,
                                  boolean skipBody, MessageInfo messageInfo) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);

        if (skipHeader(messageInfo))
            skipHeader(reader);
        else
            decodeHeader(reader, messageInfo, request);

        if (skipBody) {
            skipBody(reader);
        } else {
            decodeBody(reader, request, messageInfo);
        }
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.verifyReaderState(reader, END_DOCUMENT);
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) {

        if (internalMessage.getBody().getValue() instanceof SOAPFaultInfo) {
            messageInfo.setResponseType(MessageStruct.CHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(internalMessage.getBody().getValue());
        } else if (internalMessage.getBody().getValue() instanceof Exception) {
            messageInfo.setResponseType(UNCHECKED_EXCEPTION_RESPONSE);
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


    protected void skipBody(XMLStreamReader reader) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
        XMLStreamReaderUtil.skipElement(reader);                     // Moves to </Body>
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    protected void skipHeader(XMLStreamReader reader) {

        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;
        }
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_HEADER);
        XMLStreamReaderUtil.skipElement(reader);                     // Moves to </Header>
        XMLStreamReaderUtil.nextElementContent(reader);
    }


    private boolean skipHeader(MessageInfo messageInfo) {
        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) ==
            Service.Mode.PAYLOAD) {
            return true;
        }
        return false;
    }

    protected void decodeBodyContent
        (XMLStreamReader
        reader, InternalMessage
        response, MessageInfo
        messageInfo) {
        //kw-decodeDispatchMethod(reader, response, messageInfo);
        if (reader.getEventType() == START_ELEMENT) {
            QName name = reader.getName(); // Operation name
            JAXBContext jaxbContext = getJAXBContext(messageInfo);
            RpcLitPayload rpcLitPayload = null;//getRpcLitPayload(name);
            if (name.getNamespaceURI().equals(SOAPNamespaceConstants.ENVELOPE) &&
                name.getLocalPart().equals(SOAPNamespaceConstants.TAG_FAULT)) {
                SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);
                BodyBlock responseBody = new BodyBlock(soapFaultInfo);
                response.setBody(responseBody);
            } else {
                //jaxb will leave reader on ending </body> element
                Object jaxbBean = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);
                BodyBlock responseBody = new BodyBlock(new JAXBBeanInfo(jaxbBean, jaxbContext));
                response.setBody(responseBody);
            }
        }
    }

    protected SOAPFaultInfo decodeFault
        (XMLStreamReader
        reader, InternalMessage
        internalMessage,
         MessageInfo
        messageInfo) {
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

                if (isKnownFault(faultName, methodName)) {
                    JAXBContext jaxbContext = getJAXBContext(messageInfo);

                    //faultdetail = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);
                    Map<QName, Class> typeMapping = null;//getTypeMapping();
                    faultdetail = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);

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
                        if (isKnownFault(headerBlock.getName(), methodName)) {
                            isHeaderFault = true;
                            Object obj = headerBlock.getValue();
                            faultdetail = new JAXBBeanInfo(obj, getJAXBContext(messageInfo));
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
                    if (isKnownFault(headerBlock.getName(), methodName)) {
                        //faultdetail = headerBlock.getValue();
                        Object obj = headerBlock.getValue();
                        faultdetail = new JAXBBeanInfo(obj, getJAXBContext(messageInfo));
                    }
                }
            }
        }

        //SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);
        SOAPFaultInfo soapFaultInfo = new SOAPFaultInfo(faultcode, faultstring, faultactor, faultdetail);

        // reader could be left on CHARS token rather than </fault>
        if (reader.getEventType() == XMLStreamReader.CHARACTERS &&
            reader.getText().trim().length() == 0) {
            XMLStreamReaderUtil.nextContent(reader);
        }

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_FAULT);
        XMLStreamReaderUtil.nextElementContent(reader);
        return soapFaultInfo;
    }

    private Detail decodeFaultDetail
        (XMLStreamReader
        reader) {
        Detail detail = null;

        try {
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            detail = soapFactory.createDetail();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

            writer.writeStartElement(SOAPConstants.QNAME_SOAP_FAULT_DETAIL.getLocalPart());
            while (!((reader.getEventType() == END_ELEMENT) && reader.getName().equals(SOAPConstants.QNAME_SOAP_FAULT_DETAIL))) {
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
        } catch (XMLStreamException e) {
            throw new SenderException("sender.response.cannotDecodeFaultDetail", new LocalizableExceptionAdapter(e));
        }

        return detail;
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


    public boolean isKnownFault
        (QName
        name, Method
        methodName) {
        return false;               // TODO
    }

    public Set<QName> getKnownHeaders
        () {
        return null;               // TODO
    }

    public String getActor
        () {
        return null;               // TODO
    }

}

