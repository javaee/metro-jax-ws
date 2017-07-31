/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.api.message;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.stream.buffer.XMLStreamBuffer;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.saaj.SAAJFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.Codecs;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.message.AttachmentSetImpl;
import com.sun.xml.ws.message.DOMMessage;
import com.sun.xml.ws.message.EmptyMessageImpl;
import com.sun.xml.ws.message.ProblemActionHeader;
import com.sun.xml.ws.message.stream.PayloadStreamReaderMessage;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.message.source.PayloadSourceMessage;
import com.sun.xml.ws.message.source.ProtocolSourceMessage;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.addressing.WsaTubeHelper;
import com.sun.xml.ws.addressing.model.MissingAddressingHeaderException;
import com.sun.xml.ws.resources.AddressingMessages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.WebServiceException;

/**
 * Factory methods for various {@link Message} implementations.
 *
 * <p>
 * This class provides various methods to create different
 * flavors of {@link Message} classes that store data
 * in different formats.
 *
 * <p>
 * This is a part of the JAX-WS RI internal API so that
 * {@link Tube} implementations can reuse the implementations
 * done inside the JAX-WS.
 *
 * <p>
 * If you find some of the useful convenience methods missing
 * from this class, please talk to us.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Messages {
    private Messages() {}

    /**
     * Creates a {@link Message} backed by a JAXB bean.
     * @deprecated
     * @param context
     *      The context to be used to produce infoset from the object. Must not be null.
     * @param jaxbObject
     *      The JAXB object that represents the payload. must not be null. This object
     *      must be bound to an element (which means it either is a {@link JAXBElement} or
     *      an instanceof a class with {@link XmlRootElement}).
     * @param soapVersion
     *      The SOAP version of the message. Must not be null.
     */
    public static Message create(JAXBContext context, Object jaxbObject, SOAPVersion soapVersion) {
        return JAXBMessage.create(context,jaxbObject,soapVersion);
    }
    
    /** 
     * @deprecated
     * For use when creating a Dispatch object with an unknown JAXB implementation
     * for he JAXBContext parameter.
     * 
     */ 
    public static Message createRaw(JAXBContext context, Object jaxbObject, SOAPVersion soapVersion) {
        return JAXBMessage.createRaw(context,jaxbObject,soapVersion);
    }

    /**
     * @deprecated
     *      Use {@link #create(JAXBRIContext, Object, SOAPVersion)}
     */
    public static Message create(Marshaller marshaller, Object jaxbObject, SOAPVersion soapVersion) {
        return create(BindingContextFactory.getBindingContext(marshaller).getJAXBContext(),jaxbObject,soapVersion);
    }

    /**
     * Creates a {@link Message} backed by a SAAJ {@link SOAPMessage} object.
     *
     * <p>
     * If the {@link SOAPMessage} contains headers and attachments, this method
     * does the right thing.
     *
     * @param saaj
     *      The SOAP message to be represented as a {@link Message}.
     *      Must not be null. Once this method is invoked, the created
     *      {@link Message} will own the {@link SOAPMessage}, so it shall
     *      never be touched directly.
     */
    public static Message create(SOAPMessage saaj) {
        return SAAJFactory.create(saaj);
    }

    /**
     * Creates a {@link Message} using {@link Source} as payload.
     *
     * @param payload
     *      Source payload is {@link Message}'s payload
     *      Must not be null. Once this method is invoked, the created
     *      {@link Message} will own the {@link Source}, so it shall
     *      never be touched directly.
     *
     * @param ver
     *      The SOAP version of the message. Must not be null.
     */
    public static Message createUsingPayload(Source payload, SOAPVersion ver) {
        if (payload instanceof DOMSource) {
            if (((DOMSource)payload).getNode() == null) {
                return new EmptyMessageImpl(ver);
            }
        } else if (payload instanceof StreamSource) {
            StreamSource ss = (StreamSource)payload;
            if (ss.getInputStream() == null && ss.getReader() == null && ss.getSystemId() == null) {
                return new EmptyMessageImpl(ver);
            }
        } else if (payload instanceof SAXSource) {
            SAXSource ss = (SAXSource)payload;
            if (ss.getInputSource() == null && ss.getXMLReader() == null) {
                return new EmptyMessageImpl(ver);
            }
        }
        return new PayloadSourceMessage(payload, ver);
    }

    /**
     * Creates a {@link Message} using {@link XMLStreamReader} as payload.
     *
     * @param payload
     *      XMLStreamReader payload is {@link Message}'s payload
     *      Must not be null. Once this method is invoked, the created
     *      {@link Message} will own the {@link XMLStreamReader}, so it shall
     *      never be touched directly.
     *
     * @param ver
     *      The SOAP version of the message. Must not be null.
     */
    public static Message createUsingPayload(XMLStreamReader payload, SOAPVersion ver) {
        return new PayloadStreamReaderMessage(payload, ver);
    }

    /**
     * Creates a {@link Message} from an {@link Element} that represents
     * a payload.
     *
     * @param payload
     *      The element that becomes the child element of the SOAP body.
     *      Must not be null.
     *
     * @param ver
     *      The SOAP version of the message. Must not be null.
     */
    public static Message createUsingPayload(Element payload, SOAPVersion ver) {
        return new DOMMessage(ver,payload);
    }

    /**
     * Creates a {@link Message} from an {@link Element} that represents
     * the whole SOAP message.
     *
     * @param soapEnvelope
     *      The SOAP envelope element.
     */
    public static Message create(Element soapEnvelope) {
        SOAPVersion ver = SOAPVersion.fromNsUri(soapEnvelope.getNamespaceURI());
        // find the headers
        Element header = DOMUtil.getFirstChild(soapEnvelope, ver.nsUri, "Header");
        HeaderList headers = null;
        if(header!=null) {
            for( Node n=header.getFirstChild(); n!=null; n=n.getNextSibling() ) {
                if(n.getNodeType()==Node.ELEMENT_NODE) {
                    if(headers==null)
                        headers = new HeaderList(ver);
                    headers.add(Headers.create((Element)n));
                }
            }
        }

        // find the payload
        Element body = DOMUtil.getFirstChild(soapEnvelope, ver.nsUri, "Body");
        if(body==null)
            throw new WebServiceException("Message doesn't have <S:Body> "+soapEnvelope);
        Element payload = DOMUtil.getFirstChild(soapEnvelope, ver.nsUri, "Body");

        if(payload==null) {
            return new EmptyMessageImpl(headers, new AttachmentSetImpl(), ver);
        } else {
            return new DOMMessage(ver,headers,payload);
        }
    }

    /**
     * Creates a {@link Message} using Source as entire envelope.
     *
     * @param envelope
     *      Source envelope is used to create {@link Message}
     *      Must not be null. Once this method is invoked, the created
     *      {@link Message} will own the {@link Source}, so it shall
     *      never be touched directly.
     *
     */
    public static Message create(Source envelope, SOAPVersion soapVersion) {
        return new ProtocolSourceMessage(envelope, soapVersion);
    }


    /**
     * Creates a {@link Message} that doesn't have any payload.
     */
    public static Message createEmpty(SOAPVersion soapVersion) {
        return new EmptyMessageImpl(soapVersion);
    }

    /**
     * Creates a {@link Message} from {@link XMLStreamReader} that points to
     * the start of the envelope.
     *
     * @param reader
     *      can point to the start document or the start element (of &lt;s:Envelope>)
     */
    public static @NotNull Message create(@NotNull XMLStreamReader reader) {
        // skip until the root element
        if(reader.getEventType()!=XMLStreamConstants.START_ELEMENT)
            XMLStreamReaderUtil.nextElementContent(reader);
        assert reader.getEventType()== XMLStreamConstants.START_ELEMENT :reader.getEventType();

        SOAPVersion ver = SOAPVersion.fromNsUri(reader.getNamespaceURI());

        return Codecs.createSOAPEnvelopeXmlCodec(ver).decode(reader);
    }

    /**
     * Creates a {@link Message} from {@link XMLStreamBuffer} that retains the
     * whole envelope infoset.
     *
     * @param xsb
     *      This buffer must contain the infoset of the whole envelope.
     */
    public static @NotNull Message create(@NotNull XMLStreamBuffer xsb) {
        // TODO: we should be able to let Messae know that it's working off from a buffer,
        // to make some of the operations more efficient.
        // meanwhile, adding this as an API so that our users can take advantage of it
        // when we get around to such an implementation later.
        try {
            return create(xsb.readAsXMLStreamReader());
        } catch (XMLStreamException e) {
            throw new XMLStreamReaderException(e);
        }
    }

    /**
     * Creates a {@link Message} that represents an exception as a fault. The
     * created message reflects if t or t.getCause() is SOAPFaultException.
     *
     * creates a fault message with default faultCode env:Server if t or t.getCause()
     * is not SOAPFaultException. Otherwise, it use SOAPFaultException's faultCode
     *
     * @return
     *      Always non-null. A message that wraps this {@link Throwable}.
     *
     */
    public static Message create(Throwable t, SOAPVersion soapVersion) {
        return SOAPFaultBuilder.createSOAPFaultMessage(soapVersion, null, t);
    }

    /**
     * Creates a fault {@link Message}.
     *
     * <p>
     * This method is not designed for efficiency, and we don't expect
     * to be used for the performance critical codepath.
     *
     * @param fault
     *      The populated SAAJ data structure that represents a fault
     *      in detail.
     *
     * @return
     *      Always non-null. A message that wraps this {@link SOAPFault}.
     */
    public static Message create(SOAPFault fault) {
        SOAPVersion ver = SOAPVersion.fromNsUri(fault.getNamespaceURI());
        return new DOMMessage(ver,fault);
    }

    /**
     * @deprecated
     *      Use {@link #createAddressingFaultMessage(WSBinding, Packet, QName)}
     */
    public static Message createAddressingFaultMessage(WSBinding binding, QName missingHeader) {
        return createAddressingFaultMessage(binding,null,missingHeader);
    }

    /**
     * Creates a fault {@link Message} that captures the code/subcode/subsubcode
     * defined by WS-Addressing if one of the expected WS-Addressing headers is
     * missing in the message
     *
     * @param binding WSBinding
     * @param p
     *      {@link Packet} that was missing a WS-Addressing header.
     * @param missingHeader The missing WS-Addressing Header
     * @return
     *      A message representing SOAPFault that contains the WS-Addressing code/subcode/subsubcode.
     */
    public static Message createAddressingFaultMessage(WSBinding binding, Packet p, QName missingHeader) {
        AddressingVersion av = binding.getAddressingVersion();
        if(av == null) {
            // Addressing is not enabled.
            throw new WebServiceException(AddressingMessages.ADDRESSING_SHOULD_BE_ENABLED());
        }
        WsaTubeHelper helper = av.getWsaHelper(null,null,binding);
        return create(helper.newMapRequiredFault(new MissingAddressingHeaderException(missingHeader,p)));
    }
    /**
     * Creates a fault {@link Message} that captures the code/subcode/subsubcode
     * defined by WS-Addressing if wsa:Action is not supported.
     *
     * @param unsupportedAction The unsupported Action. Must not be null.
     * @param av The WS-Addressing version of the message. Must not be null.
     * @param sv The SOAP Version of the message. Must not be null.
     *
     * @return
     *      A message representing SOAPFault that contains the WS-Addressing code/subcode/subsubcode.
     */
    public static Message create(@NotNull String unsupportedAction, @NotNull AddressingVersion av, @NotNull SOAPVersion sv) {
        QName subcode = av.actionNotSupportedTag;
        String faultstring = String.format(av.actionNotSupportedText, unsupportedAction);

        Message faultMessage;
        SOAPFault fault;
        try {
            if (sv == SOAPVersion.SOAP_12) {
                fault = SOAPVersion.SOAP_12.getSOAPFactory().createFault();
                fault.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
                fault.appendFaultSubcode(subcode);
                Detail detail = fault.addDetail();
                SOAPElement se = detail.addChildElement(av.problemActionTag);
                se = se.addChildElement(av.actionTag);
                se.addTextNode(unsupportedAction);
            } else {
                fault = SOAPVersion.SOAP_11.getSOAPFactory().createFault();
                fault.setFaultCode(subcode);
            }
            fault.setFaultString(faultstring);

            faultMessage = SOAPFaultBuilder.createSOAPFaultMessage(sv, fault);
            if (sv == SOAPVersion.SOAP_11) {
                faultMessage.getHeaders().add(new ProblemActionHeader(unsupportedAction, av));
            }
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }

        return faultMessage;
    }

    /**
     * To be called to convert a  {@link ProtocolException} and faultcode for a given {@link SOAPVersion} in to a {@link Message}.
     *
     * @param soapVersion {@link SOAPVersion#SOAP_11} or {@link SOAPVersion#SOAP_12}
     * @param pex a ProtocolException
     * @param faultcode soap faultcode. Its ignored if the {@link ProtocolException} instance is {@link javax.xml.ws.soap.SOAPFaultException} and it has a
     * faultcode present in the underlying {@link SOAPFault}.
     * @return {@link Message} representing SOAP fault
     */
    public static @NotNull Message create(@NotNull SOAPVersion soapVersion, @NotNull ProtocolException pex, @Nullable QName faultcode){
        return SOAPFaultBuilder.createSOAPFaultMessage(soapVersion, pex, faultcode);
    }
}
