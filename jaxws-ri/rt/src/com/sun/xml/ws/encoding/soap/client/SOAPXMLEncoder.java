/*
 * $Id: SOAPXMLEncoder.java,v 1.4 2005-07-27 00:38:44 arungupta Exp $
 */

/*
 * Copyright (c) 2004 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.soap.client;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.dispatch.DispatchContext;
import com.sun.xml.ws.encoding.JAXWSAttachmentMarshaller;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.streaming.XMLStreamWriterFactory;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import com.sun.xml.ws.client.BindingProviderProperties;
import static java.util.logging.Logger.getLogger;
import static com.sun.xml.ws.client.BindingProviderProperties.JAXWS_CONTEXT_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.XML_CONTENT_TYPE_VALUE;
import com.sun.xml.ws.client.RequestContext;
import com.sun.xml.ws.client.SenderException;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;

/**
 * @author WS Development Team
 */
public class SOAPXMLEncoder extends SOAPEncoder {
    
    private static final Logger logger =
        getLogger (new StringBuffer ().append (com.sun.xml.ws.util.Constants.LoggingDomain).append (".client.dispatch.util").toString ());
    
    public SOAPXMLEncoder () {
    }
    
    protected JAXBContext getJAXBContext (MessageInfo messageInfo) {
        JAXBContext jc = null;
        RequestContext context = (RequestContext)messageInfo.getMetaData (BindingProviderProperties.JAXWS_CONTEXT_PROPERTY);
        if (context != null)
            jc = (JAXBContext)context.get (BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        
        return jc;
    }
    
    private boolean skipHeader (MessageInfo messageInfo) {
        if (messageInfo.getMetaData (DispatchContext.DISPATCH_MESSAGE_MODE) ==
            Service.Mode.PAYLOAD) {
            return true;
        }
        return false;
    }
    
    @Override
    public InternalMessage toInternalMessage (MessageInfo messageInfo) {
        InternalMessage internalMessage = new InternalMessage ();
        DispatchContext context = (DispatchContext) messageInfo.getMetaData (BindingProviderProperties.DISPATCH_CONTEXT);
        if (context != null) {
            DispatchContext.MessageType type =
                (DispatchContext.MessageType) context.getProperty (DispatchContext.DISPATCH_MESSAGE);
            Object[] data = messageInfo.getData ();
            BodyBlock bodyBlock = null;
            switch (type) {
                case JAXB_MESSAGE:
                    break;
                case JAXB_PAYLOAD:
                    JAXBBeanInfo jaxbInfo = new JAXBBeanInfo (data[0], getJAXBContext (messageInfo));
                    bodyBlock = new BodyBlock (jaxbInfo);
                    break;
                case SOURCE_PAYLOAD:
                    data = messageInfo.getData ();
                    bodyBlock = new BodyBlock ((Source) data[0]);
                    break;
                default:
            }
            if (bodyBlock != null)
                internalMessage.setBody (bodyBlock);
            
        } else {
            LogicalEPTFactory eptf = (LogicalEPTFactory) messageInfo.getEPTFactory ();
            InternalEncoder internalEncoder = eptf.getInternalEncoder ();
            //processProperties(messageInfo);
            return (InternalMessage) internalEncoder.toInternalMessage (messageInfo);
        }
        return internalMessage;
    }
    
    @Override
    public SOAPMessage toSOAPMessage (InternalMessage internalMessage,
        MessageInfo messageInfo) {
        setAttachmentsMap (messageInfo, internalMessage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        XMLStreamWriter writer = XMLStreamWriterFactory.createXMLStreamWriter (baos);
        
        SOAPMessage message = null;
        try {
            startEnvelope (writer);
            writeHeaders (writer, internalMessage, messageInfo);
            writeBody (writer, internalMessage, messageInfo);
            endEnvelope (writer);
            writer.writeEndDocument ();
            writer.close ();
            
            byte[] buf = baos.toByteArray ();
            ByteInputStream bis = new ByteInputStream (buf, 0, buf.length);
            
            // TODO: Copy the mime headers from messageInfo.METADATA
            MimeHeaders mh = new MimeHeaders ();
            mh.addHeader ("Content-Type", getContentType (messageInfo));
            message = new SOAPMessageContext ().createMessage (mh, bis, getBindingId ());
            processAttachments (internalMessage, message);
        } catch (IOException e) {
            throw new SenderException ("sender.request.messageNotReady", new LocalizableExceptionAdapter (e));
        } catch (SOAPException e) {
            throw new SenderException (new LocalizableExceptionAdapter (e));
        } catch (XMLStreamException e) {
            throw new SenderException (new LocalizableExceptionAdapter (e));
        }
        
        return message;
    }
    
    public InternalMessage createInternalMessage (MessageInfo messageInfo) {
        
        InternalMessage internalMessage = new InternalMessage ();
        Method method = messageInfo.getMethod ();
        
        Object response = messageInfo.getResponse ();
        
        BodyBlock bodyBlock = null;
        if (getJAXBContext (messageInfo) != null) {
            JAXBBeanInfo jaxbBean = new JAXBBeanInfo (response, getJAXBContext (messageInfo));
            bodyBlock = new BodyBlock (jaxbBean);
        } else if (response instanceof Source) {
            bodyBlock = new BodyBlock ((Source) response);
        }
        
        internalMessage.setBody (bodyBlock);
        return internalMessage;
    }
    
    
    protected String getContentType (MessageInfo messageInfo) {
        Object rtc = messageInfo.getMetaData (BindingProviderProperties.JAXWS_RUNTIME_CONTEXT);
        if (rtc != null) {
            BridgeContext bc = ((RuntimeContext) rtc).getBridgeContext ();
            if (bc != null) {
                JAXWSAttachmentMarshaller am = (JAXWSAttachmentMarshaller) bc.getAttachmentMarshaller ();
                if (am.isXopped ())
                    return "application/xop+xml;type=\"text/xml\"";
            }
        }
        return XML_CONTENT_TYPE_VALUE;
    }
    
    /**
     * This method is used to create the appropriate SOAPMessage (1.1 or 1.2 using SAAJ api).
     *
     * @return the BindingID associated with this encoder
     */
    protected String getBindingId () {
        return SOAPBinding.SOAP11HTTP_BINDING;
    }
}
