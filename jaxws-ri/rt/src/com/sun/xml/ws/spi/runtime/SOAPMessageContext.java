/**
 * $Id: SOAPMessageContext.java,v 1.2 2005-08-22 22:26:19 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

/**
 * This class is implemented by
 * com.sun.xml.rpc.soap.message.SOAPMessageContext
 */
public interface SOAPMessageContext
    extends javax.xml.ws.handler.soap.SOAPMessageContext, MessageContext {
}
