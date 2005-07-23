/*
 * $Id: SOAPMessageContext.java,v 1.6 2005-07-23 04:10:06 kohlert Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.encoding.soap.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.util.xml.XmlUtil;
import java.lang.reflect.Method;

/**
 * A MessageContext holds a SOAP message as well as a set
 * (possibly transport-specific) properties.
 *
 * @author WS Development Team
 */
public class SOAPMessageContext extends MessageContextImpl
    implements com.sun.xml.ws.spi.runtime.SOAPMessageContext {

    public SOAPMessageContext() {
    }

    public Set<URI> getRoles() {
        return roles;
    }

    /*
    * Method called internally to set the roles
    */
    public void setRoles(Set<URI> roles) {
        this.roles = roles;
    }

    public SOAPMessage getMessage() {
        return _message;
    }

    public void setMessage(SOAPMessage message) {
        _message = message;
    }

    public Object[] getHeaders(QName header, JAXBContext context,
        boolean allRoles){

        return null;
    }


    public boolean isFailure() {
        return _failure;
    }

    public void setFailure(boolean b) {
        _failure = b;
    }

    int currentHandler = -1;

    public void setCurrentHandler(int i) {
        currentHandler = i;
    }

    public int getCurrentHandler() {
        return currentHandler;
    }

//    public SOAPMessage createMessage() {
//        try {
//            return getMessageFactory().createMessage();
//        } catch (SOAPException e) {
//            throw new SOAPMsgCreateException(
//                    "soap.msg.create.err",
//                    new Object[] { e });
//        }
//    }
//
//    public SOAPMessage createMessage(MimeHeaders headers, InputStream in)
//        throws IOException {
//        try {
//            return getMessageFactory().createMessage(headers, in);
//        } catch (SOAPException e) {
//            throw new SOAPMsgCreateException(
//                    "soap.msg.create.err",
//                    new Object[] { e });
//        }
//    }

    public SOAPMessage createMessage() {
        return createMessage(SOAPBinding.SOAP11HTTP_BINDING);
    }

    /**
     *
     * @param binding
     * @return a <code>SOAPMessage</code> with Binding <code>binding</code>
     */
    public static SOAPMessage createMessage(String binding) {
        try {
            return getMessageFactory(binding).createMessage();
        } catch (SOAPException e) {
            throw new SOAPMsgCreateException(
                    "soap.msg.create.err",
                    new Object[] { e });
        }
    }

    /**
     *
     * @param binding
     * @param headers
     * @param in
     * @return a <code>SOAPMessage</code> created from headers, in, and binding
     * @throws IOException
     */
    public static SOAPMessage createMessage(MimeHeaders headers, InputStream in, String binding)
        throws IOException {
        try {
            return getMessageFactory(binding).createMessage(headers, in);
        } catch (SOAPException e) {
            throw new SOAPMsgCreateException(
                    "soap.msg.create.err",
                    new Object[] { e });
        }
    }

    public SOAPMessage createMessage(MimeHeaders headers, InputStream in)
        throws IOException {
        return createMessage(headers, in, SOAPBinding.SOAP11HTTP_BINDING);
    }

    public void writeInternalServerErrorResponse() {
        try {
            setFailure(true);
            SOAPMessage message = createMessage();
            message.getSOAPPart().setContent(
                    new StreamSource(
                                    XmlUtil.getUTF8ByteInputStream(
                                    DEFAULT_SERVER_ERROR_ENVELOPE)));
            setMessage(message);
        } catch (SOAPException e) {
            // this method is called as a last resort, so it fails we cannot possibly recover
        }
    }

    public void writeSimpleErrorResponse(QName faultCode, String faultString) {
        try {
            setFailure(true);
            SOAPMessage message = createMessage();
            ByteArrayOutputStream bufferedStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bufferedStream, "UTF-8");
            writer.write(
                    "<?xml version='1.0' encoding='UTF-8'?>\n"
                            + "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                            + "<env:Body><env:Fault><faultcode>env:");
            writer.write(faultCode.getLocalPart());
            writer.write("</faultcode>" + "<faultstring>");
            writer.write(faultString);
            writer.write(
                    "</faultstring>" + "</env:Fault></env:Body></env:Envelope>");
            writer.close();
            byte[] data = bufferedStream.toByteArray();
            message.getSOAPPart().setContent(
                        new StreamSource(new ByteInputStream(data, data.length)));
            setMessage(message);
        } catch (Exception e) {
            writeInternalServerErrorResponse();
        }
    }

//    private static MessageFactory getMessageFactory() {
//        try {
//            if (_messageFactory == null) {
//                _messageFactory = MessageFactory.newInstance();
//            }
//        } catch(SOAPException e) {
//            throw new SOAPMsgFactoryCreateException(
//                "soap.msg.factory.create.err",
//                new Object[] { e });
//        }
//        return _messageFactory;
//    }

    private static MessageFactory getMessageFactory(String binding) {
        if(binding.equals(SOAPBinding.SOAP11HTTP_BINDING))
            return _soap11messageFactory;
        else if(binding.equals(SOAPBinding.SOAP12HTTP_BINDING))
            return _soap12messageFactory;
        return _soap11messageFactory;
    }

    private static MessageFactory createMessageFactory(String bindingId) {
        try {
            return MessageFactory.newInstance(bindingId);
        } catch(SOAPException e) {
            throw new SOAPMsgFactoryCreateException(
                "soap.msg.factory.create.err",
                new Object[] { e });
        }
    }
    
    /*
     * Returns the invocation method
     */
    public Method getMethod() {
        // TODO
        return null;
    }


    private SOAPMessage _message;
    private boolean _failure;
    private Set<URI> roles;
    private static final MessageFactory _soap11messageFactory = createMessageFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
    private static final MessageFactory _soap12messageFactory = createMessageFactory(SOAPConstants.SOAP_1_2_PROTOCOL);;

    private final static String DEFAULT_SERVER_ERROR_ENVELOPE =
                "<?xml version='1.0' encoding='UTF-8'?>"
                + "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<env:Body>"
                + "<env:Fault>"
                + "<faultcode>env:Server</faultcode>"
                + "<faultstring>Internal server error</faultstring>"
                + "</env:Fault>"
                + "</env:Body>"
                + "</env:Envelope>";
}
