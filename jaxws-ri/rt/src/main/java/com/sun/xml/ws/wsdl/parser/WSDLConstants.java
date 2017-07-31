/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
    static final String PREFIX_NS_WSDL = "wsdl";
    static final String NS_XMLNS = "http://www.w3.org/2001/XMLSchema";
    static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    static final String NS_SOAP11_HTTP_BINDING = "http://schemas.xmlsoap.org/soap/http";

    static final QName QNAME_SCHEMA = new QName(NS_XMLNS, "schema");

    // QNames
    static final QName QNAME_BINDING = new QName(NS_WSDL, "binding");
    static final QName QNAME_DEFINITIONS = new QName(NS_WSDL, "definitions");
    static final QName QNAME_DOCUMENTATION = new QName(NS_WSDL, "documentation");
    static final QName NS_SOAP_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    static final QName NS_SOAP_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding");
    static final QName NS_SOAP12_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "binding");
    static final QName NS_SOAP12_BINDING_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap12/", "address");

    //static final QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    static final QName QNAME_IMPORT = new QName(NS_WSDL, "import");

    //static final QName QNAME_INPUT = new QName(NS_WSDL, "input");
    static final QName QNAME_MESSAGE = new QName(NS_WSDL, "message");
    static final QName QNAME_PART = new QName(NS_WSDL, "part");
    static final QName QNAME_OPERATION = new QName(NS_WSDL, "operation");
    static final QName QNAME_INPUT = new QName(NS_WSDL, "input");
    static final QName QNAME_OUTPUT = new QName(NS_WSDL, "output");

    //static final QName QNAME_OUTPUT = new QName(NS_WSDL, "output");
    //static final QName QNAME_PART = new QName(NS_WSDL, "part");
    static final QName QNAME_PORT = new QName(NS_WSDL, "port");
    static final QName QNAME_ADDRESS = new QName(NS_WSDL, "address");
    static final QName QNAME_PORT_TYPE = new QName(NS_WSDL, "portType");
    static final QName QNAME_FAULT = new QName(NS_WSDL, "fault");
    static final QName QNAME_SERVICE = new QName(NS_WSDL, "service");
    static final QName QNAME_TYPES = new QName(NS_WSDL, "types");

    static final String ATTR_TRANSPORT = "transport";
    static final String ATTR_LOCATION = "location";
    static final String ATTR_NAME = "name";
    static final String ATTR_TNS = "targetNamespace";

    //static final QName QNAME_ATTR_ARRAY_TYPE = new QName(NS_WSDL, "arrayType");
}
