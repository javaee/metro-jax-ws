/**
 * $Id: InternalSoapEncoder.java,v 1.2 2005-08-26 23:40:08 vivekp Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.spi.runtime;

import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;

public interface InternalSoapEncoder {
    /**
     *  Writes an object to output stream
     * @param obj payload to be written
     * @param messageInfo object containing informations to help JAXWS write the objects. Get
     *        this object from SOAPMessageContext.getMessageInfo()
     * @param out stream to write to
     */
    public void write(Object obj, Object messageInfo, OutputStream out);

    /**
     * Writes an object to output stream
     * @param obj payload to be written
     * @param messageInfo object containing informations to help JAXWS write the objects. Get
     *        this object from SOAPMessageContext.getMessageInfo()
     * @param out stream to write to
     */
    public void write(Object obj, Object messageInfo, XMLStreamWriter out);
}
