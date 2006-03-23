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

import com.sun.xml.ws.pept.encoding.Decoder;
import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.pept.presentation.MessageStruct;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.messaging.saaj.packaging.mime.internet.ParseException;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.JAXWSAttachmentUnmarshaller;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBridgeInfo;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayload;
import com.sun.xml.ws.encoding.jaxb.RpcLitPayloadSerializer;
import com.sun.xml.ws.encoding.soap.internal.AttachmentBlock;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.HeaderBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPFaultInfo;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.handler.HandlerContext;
import com.sun.xml.ws.handler.MessageContextUtil;
import com.sun.xml.ws.model.soap.SOAPRuntimeModel;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.streaming.SourceReaderFactory;
import com.sun.xml.ws.streaming.XMLReaderException;
import com.sun.xml.ws.streaming.XMLStreamReaderException;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.util.SOAPUtil;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.client.dispatch.impl.encoding.DispatchUtil;
import com.sun.xml.ws.client.BindingProviderProperties;

import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.xml.stream.XMLStreamReader.*;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author WS Development Team
 */
public abstract class SOAPDecoder implements Decoder {

    public final static String NOT_UNDERSTOOD_HEADERS =
        "not-understood soap headers";

    protected static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".soap.decoder");

    protected final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING =
        "SOAP must understand error";

    /**
     * FI <code>FastInfosetSource.getInputStream()</code> method via reflection.
     */
    protected static Method FastInfosetSource_getInputStream;

    static {
        // Use reflection to avoid static dependency with FI jar
        try {
            Class clazz = Class.forName("org.jvnet.fastinfoset.FastInfosetSource");
            FastInfosetSource_getInputStream = clazz.getMethod("getInputStream");
        }
        catch (Exception e) {
            // Falls through
        }
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#decode(com.sun.pept.ept.MessageInfo)
     */
    public void decode(MessageInfo arg0) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Decoder#receieveAndDecode(com.sun.pept.ept.MessageInfo)
     */
    public void receiveAndDecode(MessageInfo arg0) {
        throw new UnsupportedOperationException();
    }

    /**
     * parses and binds headers, body from SOAPMessage.
     *
     * @param soapMessage
     * @return the <code>InternalMessage</code> for the <code>soapMessage</code>
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                                             MessageInfo messageInfo) {
        return null;
    }

    /**
     * Returns the roles required for the type of binding. Returns
     * an empty set if there are none.
     */
    public Set<String> getRequiredRoles() {
        return new HashSet<String>();
    }

    /**
     * Parses and binds headers from SOAPMessage.
     *
     * @param soapMessage
     * @param internalMessage
     * @param messageInfo
     * @return the InternalMessage representation of the SOAPMessage
     */
    public InternalMessage toInternalMessage(SOAPMessage soapMessage,
                                             InternalMessage internalMessage, MessageInfo messageInfo) {
        return null;
    }

    public SOAPMessage toSOAPMessage(MessageInfo messageInfo) {
        return null;
    }

    public void toMessageInfo(InternalMessage internalMessage, MessageInfo messageInfo) {
    }

    protected QName getEnvelopeTag() {
        return SOAPConstants.QNAME_SOAP_ENVELOPE;
    }

    protected QName getBodyTag() {
        return SOAPConstants.QNAME_SOAP_BODY;
    }

    protected QName getHeaderTag() {
        return SOAPConstants.QNAME_SOAP_HEADER;
    }

    protected QName getMUAttrQName() {
        return SOAPConstants.QNAME_MUSTUNDERSTAND;
    }

    protected QName getRoleAttrQName() {
        return SOAPConstants.QNAME_ROLE;
    }

    protected QName getFaultTag() {
        return SOAPConstants.QNAME_SOAP_FAULT;
    }

    protected QName getFaultDetailTag() {
        return SOAPConstants.QNAME_SOAP_FAULT_DETAIL;
    }


    protected void skipBody(XMLStreamReader reader) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        XMLStreamReaderUtil.skipElement(reader);    // Moves to </Body>
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    protected void skipHeader(XMLStreamReader reader, MessageInfo messageInfo) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        if (!isDispatch(messageInfo))
            return;

        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;
        }

        //XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());

        dispatchUtil.collectPrefixes(reader);

        XMLStreamReaderUtil.skipElement(reader);    // Moves to </Header>

        try {
            reader.next();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected boolean skipHeader(MessageInfo messageInfo) {
        if (messageInfo.getMetaData(DispatchContext.DISPATCH_MESSAGE_MODE) ==
            Service.Mode.PAYLOAD) {
            return true;
        }
        return false;
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
            skipHeader(reader, messageInfo);
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

    protected void decodeHeader(XMLStreamReader reader, MessageInfo messageInfo,
                                InternalMessage request) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);


        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;
        }
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        if (isDispatch(messageInfo))
            dispatchUtil.collectPrefixes(reader);
        XMLStreamReaderUtil.nextElementContent(reader);
        while (true) {
            if (reader.getEventType() == START_ELEMENT) {
                decodeHeaderElement(reader, messageInfo, request);
            } else {
                break;
            }
        }
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        XMLStreamReaderUtil.nextElementContent(reader);
    }

/*
* If JAXB can deserialize a header, deserialize it.
* Otherwise, just ignore the header
*/

    protected void decodeHeaderElement(XMLStreamReader reader, MessageInfo messageInfo,
                                       InternalMessage msg) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        if (rtCtxt == null) {

            XMLStreamReaderUtil.skipElement(reader);           // Moves to END state
            XMLStreamReaderUtil.nextElementContent(reader);
            return;
        }
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        Set<QName> knownHeaders = ((SOAPRuntimeModel) rtCtxt.getModel()).getKnownHeaders();
        QName name = reader.getName();
        if (knownHeaders != null && knownHeaders.contains(name)) {
            QName headerName = reader.getName();
            if (msg.isHeaderPresent(name)) {
                // More than one instance of header whose QName is mapped to a
                // method parameter. Generates a runtime error.
                raiseFault(getSenderFaultCode(), DUPLICATE_HEADER + headerName);
            }
            Object decoderInfo = rtCtxt.getDecoderInfo(name);
            if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo) {
                JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
// JAXB leaves on </env:Header> or <nextHeaderElement>
                bridgeInfo.deserialize(reader, bridgeContext);
                HeaderBlock headerBlock = new HeaderBlock(bridgeInfo);
                msg.addHeader(headerBlock);
            }
        } else {
            XMLStreamReaderUtil.skipElement(reader);           // Moves to END state
            XMLStreamReaderUtil.nextElementContent(reader);
        }
    }

    protected void decodeBody(XMLStreamReader reader, InternalMessage response,
                              MessageInfo messageInfo) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        int state = XMLStreamReaderUtil.nextElementContent(reader);
        decodeBodyContent(reader, response, messageInfo);
        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    protected void decodeBodyContent(XMLStreamReader reader, InternalMessage response,
                                     MessageInfo messageInfo) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        BridgeContext bridgeContext = rtCtxt.getBridgeContext();
        decodeDispatchMethod(reader, response, messageInfo);
        if (reader.getEventType() == START_ELEMENT) {
            QName name = reader.getName(); // Operation name
            if (name.getNamespaceURI().equals(getEnvelopeTag().getNamespaceURI()) &&
                name.getLocalPart().equals(SOAPNamespaceConstants.TAG_FAULT)) {
                SOAPFaultInfo soapFaultInfo = decodeFault(reader, response, messageInfo);
                BodyBlock responseBody = new BodyBlock(soapFaultInfo);
                response.setBody(responseBody);
            } else {
                Object decoderInfo = rtCtxt.getDecoderInfo(name);
                if (decoderInfo != null && decoderInfo instanceof JAXBBridgeInfo)
                {
                    JAXBBridgeInfo bridgeInfo = (JAXBBridgeInfo) decoderInfo;
                    bridgeInfo.deserialize(reader, bridgeContext);
                    BodyBlock responseBody = new BodyBlock(bridgeInfo);
                    response.setBody(responseBody);
                } else
                if (decoderInfo != null && decoderInfo instanceof RpcLitPayload)
                {
                    RpcLitPayload rpcLitPayload = (RpcLitPayload) decoderInfo;
                    RpcLitPayloadSerializer.deserialize(reader, rpcLitPayload, bridgeContext);
                    BodyBlock responseBody = new BodyBlock(rpcLitPayload);
                    response.setBody(responseBody);
                }
            }
        }
    }

    public void decodeDispatchMethod(XMLStreamReader reader, InternalMessage request, MessageInfo messageInfo) {
    }

    protected SOAPFaultInfo decodeFault(XMLStreamReader reader, InternalMessage internalMessage,
                                        MessageInfo messageInfo) {
        return null;
    }

/*
*
*/

    protected void convertBodyBlock(InternalMessage request, MessageInfo messageInfo) {
        BodyBlock bodyBlock = request.getBody();
        if (bodyBlock != null) {
            Object value = bodyBlock.getValue();
            if (value instanceof JAXBBridgeInfo || value instanceof RpcLitPayload)
            {
                // Nothing to do
            } else if (value instanceof Source) {
                Source source = (Source) value;
                XMLStreamReader reader = SourceReaderFactory.createSourceReader(source, true);
                XMLStreamReaderUtil.nextElementContent(reader);
                decodeBodyContent(reader, request, messageInfo);
            } else {
                throw new WebServiceException("Shouldn't happen. Unknown type in BodyBlock =" + value.getClass());
            }
        }
    }

    /**
     * @param mi
     * @param im
     * @param message
     * @throws SOAPException
     * @throws ParseException
     */
    protected void processAttachments(MessageInfo mi, InternalMessage im, SOAPMessage message) throws SOAPException, ParseException, IOException {
        Iterator iter = message.getAttachments();
        if (iter.hasNext()) {
            JAXWSAttachmentUnmarshaller au = null;
            if (MessageInfoUtil.getRuntimeContext(mi) != null)
                au = (JAXWSAttachmentUnmarshaller) MessageInfoUtil.getRuntimeContext(mi).getBridgeContext().getAttachmentUnmarshaller();
            else {
                //for dispatch
                Unmarshaller m = (Unmarshaller)mi.getMetaData(BindingProviderProperties.DISPATCH_UNMARSHALLER);
                if (m != null)
                    au = (JAXWSAttachmentUnmarshaller) m.getAttachmentUnmarshaller();
            }
            if (au != null){
                au.setXOPPackage(isXOPPackage(message));
                au.setAttachments(im.getAttachments());
            }
        }

        while (iter.hasNext()) {
            AttachmentPart ap = (AttachmentPart) iter.next();
            im.addAttachment(AttachmentBlock.fromSAAJ(ap));
        }
    }

    /**
     * From the SOAP message header find out if its a XOP package.
     *
     * @param sm
     * @return
     * @throws ParseException
     */
    private boolean isXOPPackage(SOAPMessage sm) throws ParseException {
        String ct = getContentType(sm.getSOAPPart());
        ContentType contentType = new ContentType(ct);
        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();
        if (primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("xop+xml"))
        {
            String type = contentType.getParameter("type");
            if (type.toLowerCase().startsWith("text/xml") || type.toLowerCase().startsWith("application/soap+xml"))
                return true;
        }
        return false;
    }

    private String getContentType(SOAPPart part) {
        String[] values = part.getMimeHeader("Content-Type");
        if (values == null)
            return null;
        else
            return values[0];
    }

/*
* It does mustUnderstand processing, and does best guess of MEP
*
* Avoids SAAJ call that create DOM.
*
*/

    public boolean doMustUnderstandProcessing(SOAPMessage soapMessage,
                                              MessageInfo mi, HandlerContext handlerContext, boolean getMEP)
        throws SOAPException, IOException {
        try {
            boolean oneway = false;
            Source source = soapMessage.getSOAPPart().getContent();
            ByteInputStream bis = null;

            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                InputStream is = streamSource.getInputStream();
                if (is != null && is instanceof ByteInputStream) {
                    bis = ((ByteInputStream) is);
                } else {
                    logger.fine("SAAJ StreamSource doesn't have ByteInputStream " + is);
                }
            } else
            if (source.getClass().getName().equals("org.jvnet.fastinfoset.FastInfosetSource"))
            {
                try {
                    bis = (ByteInputStream) FastInfosetSource_getInputStream.invoke(source);
                }
                catch (Exception e) {
                    throw new XMLReaderException("fastinfoset.noImplementation");
                }
            } else {
                logger.fine("Inefficient Use - SOAPMessage is already parsed");
            }

            XMLStreamReader reader =
                SourceReaderFactory.createSourceReader(source, true);
            XMLStreamReaderUtil.nextElementContent(reader);
            checkMustUnderstandHeaders(reader, mi, handlerContext);

            if (getMEP) {
                oneway = isOneway(reader, mi);
            }
            XMLStreamReaderUtil.close(reader);
            if (bis != null) {
                bis.close();            // resets stream; SAAJ has whole stream
            }

            return oneway;
        } catch (XMLStreamReaderException xe) {
            raiseBadXMLFault(handlerContext);
            throw xe;
        }
    }

/*
* returns Oneway or not. reader is on <Body>
*
* Peek into the body and make a best guess as to whether the request
* is one-way or not. Assume request-response if it cannot be determined.
*
*/

    private boolean isOneway(XMLStreamReader reader, MessageInfo mi) {
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getBodyTag());    // <Body>
        int state = XMLStreamReaderUtil.nextElementContent(reader);
        QName operationName = null;
        if (state == START_ELEMENT) {   // handles empty Body i.e. <Body/>
            operationName = reader.getName();
        }
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        rtCtxt.setMethodAndMEP(operationName, mi);
        return (mi.getMEP() == MessageStruct.ONE_WAY_MEP);
    }

    /*
     * Does MU processing. reader is on <Envelope>, at the end of this method
     * leaves it on <Body>. Once the roles and understood headers are
     * known, this calls a separate method to check the message headers
     * since a different behavior is expected with different bindings.
     *
     * Also assume handler chain caller is null unless one is found.
     */
    private void checkMustUnderstandHeaders(XMLStreamReader reader,
                                            MessageInfo mi, HandlerContext context) {

        // Decode envelope
        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        QName got = reader.getName();
        QName exp = getEnvelopeTag();
        if (got.getLocalPart().equals(exp.getLocalPart())) {
            if (!got.getNamespaceURI().equals(exp.getNamespaceURI())) {
                raiseFault(getVersionMismatchFaultCode(),
                    "Invalid SOAP envelope version");
            }
        }
        XMLStreamReaderUtil.verifyTag(reader, getEnvelopeTag());
        XMLStreamReaderUtil.nextElementContent(reader);


        XMLStreamReaderUtil.verifyReaderState(reader, START_ELEMENT);
        if (!SOAPNamespaceConstants.TAG_HEADER.equals(reader.getLocalName())) {
            return;             // No Header, no MU processing
        }
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        XMLStreamReaderUtil.nextElementContent(reader);

        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);

        // start with just the endpoint roles
        Set<String> roles = new HashSet<String>();
        roles.addAll(getRequiredRoles());
        HandlerChainCaller hcCaller = MessageInfoUtil.getHandlerChainCaller(mi);
        if (hcCaller != null) {
            roles.addAll(hcCaller.getRoles());
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("roles:");
            for (String r : roles) {
                logger.finest("\t\"" + r + "\"");
            }
        }

        // keep set=null if there are no understood headers
        Set<QName> understoodHeaders = null;
        if (rtCtxt != null) {
            SOAPRuntimeModel model = (SOAPRuntimeModel) rtCtxt.getModel();
            if (model != null && model.getKnownHeaders() != null) {
                understoodHeaders = new HashSet<QName>(
                    ((SOAPRuntimeModel) rtCtxt.getModel()).getKnownHeaders());
            }
        }
        if (understoodHeaders == null) {
            if (hcCaller != null) {
                understoodHeaders = hcCaller.getUnderstoodHeaders();
            }
        } else {
            if (hcCaller != null) {
                understoodHeaders.addAll(hcCaller.getUnderstoodHeaders());
            }
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("understood headers:");
            if (understoodHeaders == null || understoodHeaders.isEmpty()) {
                logger.finest("\tnone");
            } else {
                for (QName nameX : understoodHeaders) {
                    logger.finest("\t" + nameX.toString());
                }
            }
        }

        checkHeadersAgainstKnown(reader, roles, understoodHeaders, mi);

        XMLStreamReaderUtil.verifyReaderState(reader, END_ELEMENT);
        XMLStreamReaderUtil.verifyTag(reader, getHeaderTag());
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    /*
     * This method is overridden for other bindings
     */
    protected void checkHeadersAgainstKnown(XMLStreamReader reader,
                                            Set<String> roles, Set<QName> understoodHeaders, MessageInfo mi) {

        while (true) {
            if (reader.getEventType() == START_ELEMENT) {
                // check MU header for each role
                QName qName = reader.getName();
                String mu = reader.getAttributeValue(
                    getMUAttrQName().getNamespaceURI(),
                    getMUAttrQName().getLocalPart());
                if (mu != null && (mu.equals("1") ||
                    mu.equalsIgnoreCase("true"))) {
                    String role = reader.getAttributeValue(
                        getRoleAttrQName().getNamespaceURI(),
                        getRoleAttrQName().getLocalPart());
                    if (role != null && roles.contains(role)) {
                        logger.finest("Element=" + qName +
                            " targeted at=" + role);
                        if (understoodHeaders == null ||
                            !understoodHeaders.contains(qName)) {
                            logger.finest("Element not understood=" + qName);

                            SOAPFault sf = SOAPUtil.createSOAPFault(
                                MUST_UNDERSTAND_FAULT_MESSAGE_STRING,
                                SOAPConstants.FAULT_CODE_MUST_UNDERSTAND,
                                role, null, SOAPBinding.SOAP11HTTP_BINDING);
                            throw new SOAPFaultException(sf);
                        }
                    }
                }
                XMLStreamReaderUtil.skipElement(reader);   // Moves to END state
                XMLStreamReaderUtil.nextElementContent(reader);
            } else {
                break;
            }
        }
    }

    protected boolean isDispatch(MessageInfo messageInfo) {

        DispatchContext context = (DispatchContext)
            messageInfo.getMetaData(BindingProviderProperties.DISPATCH_CONTEXT);
        if (context != null)
            return true;
        return false;
    }

    protected String getSOAPMessageCharsetEncoding(SOAPMessage sm) throws SOAPException {
        String charset = (String) sm.getProperty(SOAPMessage.CHARACTER_SET_ENCODING);
        return (charset != null) ? charset : "UTF-8";
    }

    protected final void raiseFault(QName faultCode, String faultString) {
        throw new SOAPFaultException(SOAPUtil.createSOAPFault(faultString, faultCode, null, null, getBindingId()));
    }

    protected void raiseBadXMLFault(HandlerContext ctxt) {
    }

    protected abstract QName getSenderFaultCode();

    protected abstract QName getReceiverFaultCode();

    protected abstract QName getVersionMismatchFaultCode();

    public abstract String getBindingId();

    private final static String DUPLICATE_HEADER =
        "Duplicate Header in the message:";

    public DispatchUtil getDispatchUtil() {
        return dispatchUtil;
    }

    protected DispatchUtil dispatchUtil = new DispatchUtil();

}
