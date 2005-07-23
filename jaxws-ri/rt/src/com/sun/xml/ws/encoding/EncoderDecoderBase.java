/**
 * $Id: EncoderDecoderBase.java,v 1.2 2005-07-23 04:10:01 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding;

import javax.xml.bind.JAXBException;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.bind.api.AccessorException;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.RawAccessor;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

/**
 * @author Vivek Pandey
 * 
 * Base Abstract class to be used for encoding-decoding a given binding.
 */
public abstract class EncoderDecoderBase {
    /**
     * Creates an internal message based thats binding dependent.
     * 
     * @param messageInfo
     * @return the internal message given a messageInfo
     */
    public Object toInternalMessage(MessageInfo messageInfo) {
        throw new UnsupportedOperationException("Not Implementated!");
    }

    /**
     * Fills in MessageInfo from binding dependent internal message.
     * 
     * @param internalMessage
     * @param messageInfo
     */
    public void toMessageInfo(Object internalMessage, MessageInfo messageInfo) {
        throw new UnsupportedOperationException("Not Implementated!");
    }

    /**
     * Get the wrapper child value from a jaxb style wrapper bean.
     * 
     * @param context
     *            RuntimeContext to be passed by the encoder/decoder processing
     *            SOAP message during toMessageInfo()
     * @param wrapperBean
     *            The wrapper bean instance
     * @param nsURI
     *            namespace of the wrapper child property
     * @param localName
     *            local name of the wrapper child property
     * @return The wrapper child
     * 
     */
    protected Object getWrapperChildValue(RuntimeContext context, Object wrapperBean, String nsURI,
            String localName) {
        if (wrapperBean == null)
            return null;
        JAXBRIContext jaxbContext = context.getModel().getJAXBContext();
        try {
            RawAccessor ra = jaxbContext.getElementPropertyAccessor(wrapperBean.getClass(), nsURI,
                    localName);
            return ra.get(wrapperBean);
        } catch (AccessorException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        } catch (JAXBException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }

    /**
     * Set the wrapper child value from a jaxb style wrapper bean.
     * 
     * @param context
     *            context RuntimeContext to be passed by the encoder/decoder
     *            processing SOAP message during toMessageInfo()
     * @param wrapperBean
     *            The wrapper bean instance
     * @param value
     *            value of the wrapper child property
     * @param nsURI
     *            namespace of the wrapper child property
     * @param localName
     *            localName local name of the wrapper child property
     */
    protected void setWrapperChildValue(RuntimeContext context, Object wrapperBean, Object value,
            String nsURI, String localName) {
        if (wrapperBean == null)
            return;
        JAXBRIContext jaxbContext = context.getModel().getJAXBContext();
        try {
            RawAccessor ra = jaxbContext.getElementPropertyAccessor(wrapperBean.getClass(), nsURI,
                    localName);
            ra.set(wrapperBean, value);
        } catch (AccessorException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        } catch (JAXBException e) {
            throw new SerializationException(new LocalizableExceptionAdapter(e));
        }
    }
}
