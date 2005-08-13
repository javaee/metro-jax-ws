/**
 * $Id: MIMEConstants.java,v 1.1 2005-08-13 19:30:36 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.wsdl.parser;

import javax.xml.namespace.QName;


public interface MIMEConstants {
    // namespace URIs
    public static String NS_WSDL_MIME = "http://schemas.xmlsoap.org/wsdl/mime/";

    // QNames
    public static QName QNAME_CONTENT = new QName(NS_WSDL_MIME, "content");
    public static QName QNAME_MULTIPART_RELATED =
        new QName(NS_WSDL_MIME, "multipartRelated");
    public static QName QNAME_PART = new QName(NS_WSDL_MIME, "part");
    public static QName QNAME_MIME_XML = new QName(NS_WSDL_MIME, "mimeXml");
}
