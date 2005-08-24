/**
 * $Id: InternalSoapEncoder.java,v 1.1 2005-08-24 03:24:48 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.spi.runtime;

import com.sun.pept.ept.MessageInfo;
import java.io.OutputStream;
import javax.xml.stream.XMLStreamWriter;


public interface InternalSoapEncoder {
    public void write(Object obj, MessageInfo mi, OutputStream out);
    public void write(Object obj, MessageInfo mi, XMLStreamWriter out);
}
