/*
 * $Id: HTTPConstants.java,v 1.3 2005-09-10 19:49:58 kohsuke Exp $
 */

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

package com.sun.tools.ws.wsdl.document.http;

import javax.xml.namespace.QName;

/**
 * Interface defining HTTP-extension-related constants.
 *
 * @author WS Development Team
 */
public interface HTTPConstants {

    // namespace URIs
    public static String NS_WSDL_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";

    // QNames
    public static QName QNAME_ADDRESS = new QName(NS_WSDL_HTTP, "address");
    public static QName QNAME_BINDING = new QName(NS_WSDL_HTTP, "binding");
    public static QName QNAME_OPERATION = new QName(NS_WSDL_HTTP, "operation");
    public static QName QNAME_URL_ENCODED =
        new QName(NS_WSDL_HTTP, "urlEncoded");
    public static QName QNAME_URL_REPLACEMENT =
        new QName(NS_WSDL_HTTP, "urlReplacement");
}
