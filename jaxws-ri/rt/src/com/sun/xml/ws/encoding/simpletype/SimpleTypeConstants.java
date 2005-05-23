/*
 * $Id: SimpleTypeConstants.java,v 1.1 2005-05-23 22:28:40 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package com.sun.xml.ws.encoding.simpletype;

import javax.xml.namespace.QName;

import com.sun.xml.ws.encoding.soap.SerializerConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

/**
 *
 * @author JAX-RPC Development Team
 */
public interface SimpleTypeConstants extends SerializerConstants {

    public static final String URI_XSI = SOAPNamespaceConstants.XSI;
    public static final String URI_XSD = SOAPNamespaceConstants.XSD;

    public static final QName QNAME_XSI_TYPE = new QName(URI_XSI, "type");
    public static final QName QNAME_XSI_NIL = new QName(URI_XSI, "nil");

}
