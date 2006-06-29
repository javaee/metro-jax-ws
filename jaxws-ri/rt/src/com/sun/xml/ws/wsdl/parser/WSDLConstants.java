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

import javax.xml.namespace.QName;


/**
 * Interface defining WSDL-related constants.
 *
 * @author WS Development Team
 */
public interface WSDLConstants {
    // namespace URIs
    public static final String NS_XMLNS = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String NS_SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";

    public static final QName QNAME_SCHEMA = new QName(NS_XMLNS, "schema");

    // QNames
    public static final QName QNAME_BINDING = new QName(NS_WSDL, "binding");
    public static final QName QNAME_DEFINITIONS = new QName(NS_WSDL, "definitions");
    public static final QName QNAME_DOCUMENTATION = new QName(NS_WSDL, "documentation");
    public static final QName NS_SOAP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
            "address");
    public static final QName NS_SOAP_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
            "binding");
    public static final QName NS_SOAP12_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap12/",
            "binding");
    public static final QName NS_SOAP12_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap12/",
            "address");

    //public static final QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    public static final QName QNAME_IMPORT = new QName(NS_WSDL, "import");

    //public static final QName QNAME_INPUT = new QName(NS_WSDL, "input");
    public static final QName QNAME_MESSAGE = new QName(NS_WSDL, "message");
    public static final QName QNAME_PART = new QName(NS_WSDL, "part");
    public static final QName QNAME_OPERATION = new QName(NS_WSDL, "operation");
    public static final QName QNAME_INPUT = new QName(NS_WSDL, "input");
    public static final QName QNAME_OUTPUT = new QName(NS_WSDL, "output");

    //public static final QName QNAME_OUTPUT = new QName(NS_WSDL, "output");
    //public static final QName QNAME_PART = new QName(NS_WSDL, "part");
    public static final QName QNAME_PORT = new QName(NS_WSDL, "port");
    public static final QName QNAME_ADDRESS = new QName(NS_WSDL, "address");
    public static final QName QNAME_PORT_TYPE = new QName(NS_WSDL, "portType");
    public static final QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    public static final QName QNAME_SERVICE = new QName(NS_WSDL, "service");
    public static final QName QNAME_TYPES = new QName(NS_WSDL, "types");

    public static final String ATTR_TRANSPORT = "transport";
    public static final String ATTR_LOCATION = "location";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TNS = "targetNamespace";

    //public static final QName QNAME_ATTR_ARRAY_TYPE = new QName(NS_WSDL, "arrayType");
}
