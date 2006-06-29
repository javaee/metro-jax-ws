/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;

import javax.xml.namespace.QName;

public interface SOAPConstants {

    // namespace URIs
    public static final String URI_ENVELOPE = SOAPNamespaceConstants.ENVELOPE;
    public static final String URI_ENVELOPE12 = SOAP12NamespaceConstants.ENVELOPE;

    public static final String NS_WSDL_SOAP =
        "http://schemas.xmlsoap.org/wsdl/soap/";

    public static final String NS_WSDL_SOAP12 =
        "http://schemas.xmlsoap.org/wsdl/soap12/";

    public static final String NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";

    // other URIs
    public final String URI_SOAP_TRANSPORT_HTTP =
        "http://schemas.xmlsoap.org/soap/http";

    // QNames
    public static final QName QNAME_ADDRESS =
        new QName(NS_WSDL_SOAP, "address");
    public static final QName QNAME_SOAP12ADDRESS =
        new QName(NS_WSDL_SOAP12, "address");
    public static final QName QNAME_BINDING =
        new QName(NS_WSDL_SOAP, "binding");
    public static final QName QNAME_BODY = new QName(NS_WSDL_SOAP, "body");
    public static final QName QNAME_SOAP12BODY = new QName(NS_WSDL_SOAP12, "body");
    public static final QName QNAME_FAULT = new QName(NS_WSDL_SOAP, "fault");
    public static final QName QNAME_HEADER = new QName(NS_WSDL_SOAP, "header");
    public static final QName QNAME_SOAP12HEADER = new QName(NS_WSDL_SOAP12, "header");
    public static final QName QNAME_HEADERFAULT =
        new QName(NS_WSDL_SOAP, "headerfault");
    public static final QName QNAME_OPERATION =
        new QName(NS_WSDL_SOAP, "operation");
    public static final QName QNAME_MUSTUNDERSTAND =
        new QName(URI_ENVELOPE, "mustUnderstand");


}
