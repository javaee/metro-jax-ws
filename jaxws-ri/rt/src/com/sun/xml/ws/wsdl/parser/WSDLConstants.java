/**
 * $Id: WSDLConstants.java,v 1.6 2005-08-13 19:32:44 vivekp Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
    public static String NS_XMLNS = "http://www.w3.org/2000/xmlns/";
    public static String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static String NS_SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";

    // QNames
    public static QName QNAME_BINDING = new QName(NS_WSDL, "binding");
    public static QName QNAME_DEFINITIONS = new QName(NS_WSDL, "definitions");
    public static QName QNAME_DOCUMENTATION = new QName(NS_WSDL, "documentation");
    public static QName NS_SOAP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
            "address");
    public static QName NS_SOAP_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap/",
            "binding");
    public static QName NS_SOAP12_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap12/",
            "binding");
    public static QName NS_SOAP12_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap12/",
            "address");

    //public static QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    public static QName QNAME_IMPORT = new QName(NS_WSDL, "import");

    //public static QName QNAME_INPUT = new QName(NS_WSDL, "input");
    public static QName QNAME_MESSAGE = new QName(NS_WSDL, "message");
    public static QName QNAME_PART = new QName(NS_WSDL, "part");
    public static QName QNAME_OPERATION = new QName(NS_WSDL, "operation");
    public static QName QNAME_INPUT = new QName(NS_WSDL, "input");
    public static QName QNAME_OUTPUT = new QName(NS_WSDL, "output");

    //public static QName QNAME_OUTPUT = new QName(NS_WSDL, "output");
    //public static QName QNAME_PART = new QName(NS_WSDL, "part");
    public static QName QNAME_PORT = new QName(NS_WSDL, "port");
    public static QName QNAME_ADDRESS = new QName(NS_WSDL, "address");
    public static QName QNAME_PORT_TYPE = new QName(NS_WSDL, "portType");
    public static QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    public static QName QNAME_SERVICE = new QName(NS_WSDL, "service");
    public static QName QNAME_TYPES = new QName(NS_WSDL, "types");

    public static String ATTR_TRANSPORT = "transport";
    public static String ATTR_LOCATION = "location";
    public static String ATTR_NAME = "name";
    public static String ATTR_TNS = "targetNamespace";

    //public static QName QNAME_ATTR_ARRAY_TYPE = new QName(NS_WSDL, "arrayType");
}
