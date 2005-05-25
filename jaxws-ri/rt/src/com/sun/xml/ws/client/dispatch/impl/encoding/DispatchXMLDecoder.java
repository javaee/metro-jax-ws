/*
 * $Id: DispatchXMLDecoder.java,v 1.2 2005-05-25 18:22:10 kohlert Exp $
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
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayloadSerializer;
import com.sun.xml.ws.encoding.simpletype.EncoderUtils;
import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.streaming.*;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.sun.pept.presentation.MessageStruct.UNCHECKED_EXCEPTION_RESPONSE;

/**
 * @author JAX-RPC Development Team
 */

public class DispatchXMLDecoder extends com.sun.xml.ws.client.SOAPXMLDecoder {
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private DispatchSerializer dispatchSerializer;
    private static JAXBContext jc;

    public DispatchXMLDecoder() {
        dispatchSerializer = new DispatchSerializer();
    }

    protected void decodeBody(XMLReader reader, InternalMessage response, MessageInfo messageInfo) {
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
        int state = reader.nextElementContent();

        // if Body is not empty, then deserialize the Body
        if (state != XMLReader.END) {
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

        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
        reader.nextElementContent();
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


    protected void skipBody(XMLReader reader) {
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
        reader.skipElement();                     // Moves to </Body>
        reader.nextElementContent();
    }

    /*
     * skipBody is true, the body is skipped during parsing.
     */
    protected void decodeEnvelope(XMLReader reader, InternalMessage request,
                                  boolean skipBody, MessageInfo messageInfo) {
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_ENVELOPE);
        reader.nextElementContent();
        decodeHeader(reader, request);
        if (skipBody) {
            skipBody(reader);
        } else {
            decodeBody(reader, request, messageInfo);
        }
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_ENVELOPE);
        reader.nextElementContent();
        XMLReaderUtil.verifyReaderState(reader, XMLReader.EOF);
    }

    protected void decodeHeader(XMLReader reader, InternalMessage request) {
        XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;
        }
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_HEADER);
        reader.nextElementContent();
        while (true) {
            if (reader.getState() == XMLReader.START) {
                decodeHeaderElement(reader, request);
            } else {
                break;
            }
        }
        XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
        XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_HEADER);
        reader.nextElementContent();
    }

    /*
     * If JAXB can deserialize a header, deserialize it.
     * Otherwise, just ignore the header
     */
    private void decodeHeaderElement(XMLReader reader, InternalMessage request) {
        /* Set<QName> knownHeaders = getKnownHeaders();
         QName requestHeaderName = reader.getName();
         if (knownHeaders != null && knownHeaders.contains(requestHeaderName)) {
             QName headerName = reader.getName();
             if (request.isHeaderPresent(headerName)) {
                 // More than one instance of header whose QName is mapped to a
                 // method parameter. Generates a runtime error.
                 raiseFault(SOAPConstants.FAULT_CODE_CLIENT, DUPLICATE_HEADER+headerName);
             }
             JAXBContext jaxbContext = getJAXBContext();
             // JAXB leaves on </env:Header> or <nextHeaderElement>
 //            Object jaxbBean = JAXBTypeSerializer.getInstance().deserialize(reader,
 //                    jaxbContext);
             if (reader.getState() == XMLReader.START){
                 QName name = reader.getName(); // header block name
                 Map<QName, Class> typeMapping = getTypeMapping();
                 if (typeMapping != null) {
                     Class type = typeMapping.get(name);
                     Object jaxbType = JAXBTypeSerializer.getInstance().deserialize(
                             type, reader, jaxbContext);
                     HeaderBlock requestHeader = new HeaderBlock(headerName, jaxbType);
                     request.addHeader(requestHeader);
                 } else {
                     //jaxb will leave reader on ending </body> element
                     Object jaxbBean = JAXBTypeSerializer.getInstance().deserialize(
                         reader, jaxbContext);
                     HeaderBlock requestHeader = new HeaderBlock(headerName, jaxbBean);
                     request.addHeader(requestHeader);
                 }
             }

 //            HeaderBlock requestHeader = new HeaderBlock(headerName, jaxbBean);
 //            request.addHeader(requestHeader);
         } else {
             reader.skipElement();                 // Moves to END state
             reader.nextElementContent();
         }
         */
    }

    /*  protected void decodeBody(XMLReader reader, InternalMessage response, MessageInfo messageInfo) {
          XMLReaderUtil.verifyReaderState(reader, XMLReader.START);
          XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
          int state = reader.nextElementContent();
          decodeBodyContent(reader, response, messageInfo);
          XMLReaderUtil.verifyReaderState(reader, XMLReader.END);
          XMLReaderUtil.verifyTag(reader, SOAPConstants.QNAME_SOAP_BODY);
          reader.nextElementContent();
      }
      */
    protected void decodeBodyContent(XMLReader reader, InternalMessage response, MessageInfo messageInfo) {
        decodeDispatchMethod(reader, response, messageInfo);
        if (reader.getState() == XMLReader.START) {
            QName name = reader.getName(); // Operation name
            JAXBContext jaxbContext = getJAXBContext(messageInfo);
            RpcLitPayload rpcLitPayload = null;//getRpcLitPayload(name);
            if (name.getNamespaceURI().equals(SOAPNamespaceConstants.ENVELOPE) &&
                name.getLocalPart().equals(SOAPNamespaceConstants.TAG_FAULT)) {
                SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);
                BodyBlock responseBody = new BodyBlock(soapFaultInfo);
                response.setBody(responseBody);
            } else {
                if (rpcLitPayload != null) {
                    RpcLitPayloadSerializer.deserialize(rpcLitPayload, reader, jaxbContext);
                    BodyBlock responseBody = new BodyBlock(rpcLitPayload);
                    response.setBody(responseBody);
                }

                //jaxb will leave reader on ending </body> element
                Object jaxbBean = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);
                BodyBlock responseBody = new BodyBlock(new JAXBBeanInfo(jaxbBean, jaxbContext));
                response.setBody(responseBody);

            }
        }
    }

    protected SOAPFaultInfo decodeFault(XMLReader reader, InternalMessage internalMessage,
                                        MessageInfo messageInfo) {
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

                if (isKnownFault(faultName, methodName)) {
                    JAXBContext jaxbContext = getJAXBContext(messageInfo);

                    //faultdetail = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);
                    Map<QName, Class> typeMapping = null;//getTypeMapping();

                    faultdetail = JAXBTypeSerializer.getInstance().deserialize(reader, jaxbContext);


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
                    reader.next();
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


    protected JAXBContext getJAXBContext(MessageInfo messageInfo) {
        if (jc == null) {
            RequestContext requestContext = (RequestContext) messageInfo.getMetaData(BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
            jc = (JAXBContext)
                requestContext.copy().get(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        }
        return jc;
    }


    public boolean isKnownFault(QName name, Method methodName) {
        return false;               // TODO
    }

    public Set<QName> getKnownHeaders() {
        return null;               // TODO
    }

    public String getActor() {
        return null;               // TODO
    }

}

