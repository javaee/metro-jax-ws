/*
 * $Id: SOAPUtil.java,v 1.1 2005-07-13 01:37:26 jitu Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.util;

import com.sun.xml.ws.encoding.soap.message.SOAPMsgCreateException;
import com.sun.xml.ws.encoding.soap.message.SOAPMsgFactoryCreateException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPBinding;

/**
 * $author: JAXWS Development Team
 */

/**
 * Has utility methods to create SOAPMessage
 */
public class SOAPUtil {
    
    private static final MessageFactory soap11messageFactory =
            createMessageFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
    private static final MessageFactory soap12messageFactory =
            createMessageFactory(SOAPConstants.SOAP_1_2_PROTOCOL);
    
    public static SOAPMessage createMessage() {
        return createMessage(SOAPBinding.SOAP11HTTP_BINDING);
    }

    /**
     *
     * @param binding
     * @return
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
     * @return
     * @throws IOException
     */
    public static SOAPMessage createMessage(MimeHeaders headers, InputStream in,
            String binding) throws IOException {
        try {
            return getMessageFactory(binding).createMessage(headers, in);
        } catch (SOAPException e) {
            throw new SOAPMsgCreateException(
                    "soap.msg.create.err",
                    new Object[] { e });
        }
    }

    public static SOAPMessage createMessage(MimeHeaders headers, InputStream in)
    throws IOException {
        return createMessage(headers, in, SOAPBinding.SOAP11HTTP_BINDING);
    }

    private static MessageFactory getMessageFactory(String binding) {
        if (binding.equals(SOAPBinding.SOAP11HTTP_BINDING)) {
            return soap11messageFactory;
        } else if (binding.equals(SOAPBinding.SOAP12HTTP_BINDING)) {
            return soap12messageFactory;
        }
        return soap11messageFactory;
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

}
