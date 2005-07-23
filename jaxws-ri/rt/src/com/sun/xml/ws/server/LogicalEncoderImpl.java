/**
 * $Id: LogicalEncoderImpl.java,v 1.2 2005-07-23 04:10:11 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.encoding.soap.DeserializationException;
import com.sun.xml.ws.encoding.soap.SerializationException;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;

public class LogicalEncoderImpl implements LogicalEncoder {

    /*
     * Unmarshall source to JAXB bean using context
     * @see LogicalEncoder#toJAXBBean(Source, JAXBContext)
     */
    public JAXBBeanInfo toJAXBBeanInfo(Source source, JAXBContext context) {
        Object obj = JAXBTypeSerializer.getInstance().deserialize(source, context);
        return new JAXBBeanInfo(obj, context);
    }

    /*
     */
    public DOMSource toDOMSource(JAXBBeanInfo beanInfo) {
        return JAXBTypeSerializer.getInstance().serialize(beanInfo.getBean(),
                beanInfo.getJAXBContext());
    }

}
