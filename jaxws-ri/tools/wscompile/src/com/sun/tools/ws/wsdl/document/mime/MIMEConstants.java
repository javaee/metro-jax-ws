/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.wsdl.document.mime;

import javax.xml.namespace.QName;

/**
 * Interface defining MIME-extension-related constants.
 *
 * @author WS Development Team
 */
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
