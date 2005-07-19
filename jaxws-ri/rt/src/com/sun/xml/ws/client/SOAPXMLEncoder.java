/*
 * $Id: SOAPXMLEncoder.java,v 1.16 2005-07-19 18:10:02 arungupta Exp $
 */

/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved.
*/
package com.sun.xml.ws.client;

import javax.xml.stream.XMLStreamWriter;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.model.JavaMethod;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.transport.http.client.HttpClientTransportFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.bind.api.BridgeContext;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.ws.BindingProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.sun.xml.ws.util.SOAPUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.ws.soap.SOAPBinding;

import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;

/**
 * @author WS Development Team
 */
public class SOAPXMLEncoder extends SOAPEncoder {

    public SOAPXMLEncoder() {
    }

    /* (non-Javadoc)
     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
     */
    public ByteBuffer encode(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

    /*
     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
     */
    public void encodeAndSend(MessageInfo messageInfo) {
        throw new UnsupportedOperationException();
    }

//    /* (non-Javadoc)
//     * @see com.sun.pept.encoding.Encoder#encode(com.sun.pept.ept.MessageInfo)
//     */
//    public ByteBuffer encode(MessageInfo messageInfo) {
//        try {
//            InternalMessage request = toInternalMessage(messageInfo);
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);
//
//            startEnvelope(writer);
//            writeHeaders(writer, request, messageInfo);
//            writeBody(writer, request, messageInfo);
//            endEnvelope(writer);
//            writer.writeEndDocument();
//            writer.close();
//
//            return ByteBuffer.wrap(baos.toByteArray());
//        }
//        catch (XMLStreamException e) {
//            throw new SenderException(new LocalizableExceptionAdapter(e));
//        }
//    }
//
//    /* (non-Javadoc)
//     * @see com.sun.pept.encoding.Encoder#encodeAndSend(com.sun.pept.ept.MessageInfo)
//     */
//    public void encodeAndSend(MessageInfo messageInfo) {
//        ByteBuffer buffer = encode(messageInfo);
//
//        // sending the request over the wire
//        Connection connection = messageInfo.getConnection();
//        if (connection == null)
//            throw new SenderException("sender.request.connectionNotInitialized");
//
//        connection.write(buffer);
//    }

    public InternalMessage toInternalMessage(MessageInfo messageInfo) {
        LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory();
        InternalEncoder internalEncoder = eptf.getInternalEncoder();
        InternalMessage im = (InternalMessage) internalEncoder.toInternalMessage(messageInfo);

        return im;
    }

    public SOAPMessage toSOAPMessage(InternalMessage internalMessage,
        MessageInfo messageInfo) {
        setAttachmentsMap(messageInfo, internalMessage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter(baos);

        SOAPMessage message = null;
        try {
            startEnvelope(writer);
            writeHeaders(writer, internalMessage, messageInfo);
            writeBody(writer, internalMessage, messageInfo);
            endEnvelope(writer);
            writer.writeEndDocument();
            writer.close();

            byte[] buf = baos.toByteArray();
            ByteInputStream bis = new ByteInputStream(buf, 0, buf.length);

            // TODO: Copy the mime headers from messageInfo.METADATA
            MimeHeaders mh = new MimeHeaders();
            mh.addHeader("Content-Type", getContentType(messageInfo));
            message = SOAPUtil.createMessage(mh, bis, getBindingId());
            processAttachments(internalMessage, message);
        } catch (IOException e) {
            throw new SenderException("sender.request.messageNotReady", new LocalizableExceptionAdapter(e));
        } catch (SOAPException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        } catch (XMLStreamException e) {
            throw new SenderException(new LocalizableExceptionAdapter(e));
        }

        return message;
    }

    protected String getContentType (MessageInfo messageInfo){
        
        Object rtc = messageInfo.getMetaData (BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if(rtc != null){
            BridgeContext bc = ((RuntimeContext)rtc).getBridgeContext ();
            if(bc != null){
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller)bc.getAttachmentMarshaller ();
                if(am.isXopped ())
                    return "application/xop+xml;type=\"text/xml\"";
            }
        }
        return XML_CONTENT_TYPE_VALUE;
    }

    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     * @return
     */
    protected String getBindingId(){
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
