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

package com.sun.tools.ws.wsdl.document.soap;

import javax.xml.namespace.QName;

/**
 * Interface defining SOAP1.2-related constants.
 *
 * @author WS Development Team
 */
public interface SOAP12Constants {

    // namespace URIs
    static final String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap12/";
    static final String NS_SOAP_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";

    // other URIs
    static final String URI_SOAP_TRANSPORT_HTTP = "http://www.w3.org/2003/05/soap/bindings/HTTP/";

    // QNames
    static final QName QNAME_ADDRESS = new QName(NS_WSDL_SOAP, "address");
    static final QName QNAME_BINDING = new QName(NS_WSDL_SOAP, "binding");
    static final QName QNAME_BODY = new QName(NS_WSDL_SOAP, "body");
    static final QName QNAME_FAULT = new QName(NS_WSDL_SOAP, "fault");
    static final QName QNAME_HEADER = new QName(NS_WSDL_SOAP, "header");
    static final QName QNAME_HEADERFAULT = new QName(NS_WSDL_SOAP, "headerfault");
    static final QName QNAME_OPERATION = new QName(NS_WSDL_SOAP, "operation");

    // SOAP encoding QNames
    static final QName QNAME_TYPE_ARRAY = new QName(NS_SOAP_ENCODING, "Array");
    static final QName QNAME_ATTR_GROUP_COMMON_ATTRIBUTES = new QName(NS_SOAP_ENCODING, "commonAttributes");
    static final QName QNAME_ATTR_ARRAY_TYPE = new QName(NS_SOAP_ENCODING, "arrayType");
    static final QName QNAME_ATTR_ITEM_TYPE = new QName(NS_SOAP_ENCODING, "itemType");
    static final QName QNAME_ATTR_ARRAY_SIZE = new QName(NS_SOAP_ENCODING, "arraySize");
    static final QName QNAME_ATTR_OFFSET = new QName(NS_SOAP_ENCODING, "offset");
    static final QName QNAME_ATTR_POSITION = new QName(NS_SOAP_ENCODING, "position");

    static final QName QNAME_TYPE_BASE64 = new QName(NS_SOAP_ENCODING, "base64");

    static final QName QNAME_ELEMENT_STRING = new QName(NS_SOAP_ENCODING, "string");
    static final QName QNAME_ELEMENT_NORMALIZED_STRING = new QName(NS_SOAP_ENCODING, "normalizedString");
    static final QName QNAME_ELEMENT_TOKEN = new QName(NS_SOAP_ENCODING, "token");
    static final QName QNAME_ELEMENT_BYTE = new QName(NS_SOAP_ENCODING, "byte");
    static final QName QNAME_ELEMENT_UNSIGNED_BYTE = new QName(NS_SOAP_ENCODING, "unsignedByte");
    static final QName QNAME_ELEMENT_BASE64_BINARY = new QName(NS_SOAP_ENCODING, "base64Binary");
    static final QName QNAME_ELEMENT_HEX_BINARY = new QName(NS_SOAP_ENCODING, "hexBinary");
    static final QName QNAME_ELEMENT_INTEGER = new QName(NS_SOAP_ENCODING, "integer");
    static final QName QNAME_ELEMENT_POSITIVE_INTEGER = new QName(NS_SOAP_ENCODING, "positiveInteger");
    static final QName QNAME_ELEMENT_NEGATIVE_INTEGER = new QName(NS_SOAP_ENCODING, "negativeInteger");
    static final QName QNAME_ELEMENT_NON_NEGATIVE_INTEGER = new QName(NS_SOAP_ENCODING, "nonNegativeInteger");
    static final QName QNAME_ELEMENT_NON_POSITIVE_INTEGER = new QName(NS_SOAP_ENCODING, "nonPositiveInteger");
    static final QName QNAME_ELEMENT_INT = new QName(NS_SOAP_ENCODING, "int");
    static final QName QNAME_ELEMENT_UNSIGNED_INT = new QName(NS_SOAP_ENCODING, "unsignedInt");
    static final QName QNAME_ELEMENT_LONG = new QName(NS_SOAP_ENCODING, "long");
    static final QName QNAME_ELEMENT_UNSIGNED_LONG = new QName(NS_SOAP_ENCODING, "unsignedLong");
    static final QName QNAME_ELEMENT_SHORT = new QName(NS_SOAP_ENCODING, "short");
    static final QName QNAME_ELEMENT_UNSIGNED_SHORT = new QName(NS_SOAP_ENCODING, "unsignedShort");
    static final QName QNAME_ELEMENT_DECIMAL = new QName(NS_SOAP_ENCODING, "decimal");
    static final QName QNAME_ELEMENT_FLOAT = new QName(NS_SOAP_ENCODING, "float");
    static final QName QNAME_ELEMENT_DOUBLE = new QName(NS_SOAP_ENCODING, "double");
    static final QName QNAME_ELEMENT_BOOLEAN = new QName(NS_SOAP_ENCODING, "boolean");
    static final QName QNAME_ELEMENT_TIME = new QName(NS_SOAP_ENCODING, "time");
    static final QName QNAME_ELEMENT_DATE_TIME = new QName(NS_SOAP_ENCODING, "dateTime");
    static final QName QNAME_ELEMENT_DURATION = new QName(NS_SOAP_ENCODING, "duration");
    static final QName QNAME_ELEMENT_DATE = new QName(NS_SOAP_ENCODING, "date");
    static final QName QNAME_ELEMENT_G_MONTH = new QName(NS_SOAP_ENCODING, "gMonth");
    static final QName QNAME_ELEMENT_G_YEAR = new QName(NS_SOAP_ENCODING, "gYear");
    static final QName QNAME_ELEMENT_G_YEAR_MONTH = new QName(NS_SOAP_ENCODING, "gYearMonth");
    static final QName QNAME_ELEMENT_G_DAY = new QName(NS_SOAP_ENCODING, "gDay");
    static final QName QNAME_ELEMENT_G_MONTH_DAY = new QName(NS_SOAP_ENCODING, "gMonthDay");
    static final QName QNAME_ELEMENT_NAME = new QName(NS_SOAP_ENCODING, "Name");
    static final QName QNAME_ELEMENT_QNAME = new QName(NS_SOAP_ENCODING, "QName");
    static final QName QNAME_ELEMENT_NCNAME = new QName(NS_SOAP_ENCODING, "NCName");
    static final QName QNAME_ELEMENT_ANY_URI = new QName(NS_SOAP_ENCODING, "anyURI");
    static final QName QNAME_ELEMENT_ID = new QName(NS_SOAP_ENCODING, "ID");
    static final QName QNAME_ELEMENT_IDREF = new QName(NS_SOAP_ENCODING, "IDREF");
    static final QName QNAME_ELEMENT_IDREFS = new QName(NS_SOAP_ENCODING, "IDREFS");
    static final QName QNAME_ELEMENT_ENTITY = new QName(NS_SOAP_ENCODING, "ENTITY");
    static final QName QNAME_ELEMENT_ENTITIES = new QName(NS_SOAP_ENCODING, "ENTITIES");
    static final QName QNAME_ELEMENT_NOTATION = new QName(NS_SOAP_ENCODING, "NOTATION");
    static final QName QNAME_ELEMENT_NMTOKEN = new QName(NS_SOAP_ENCODING, "NMTOKEN");
    static final QName QNAME_ELEMENT_NMTOKENS = new QName(NS_SOAP_ENCODING, "NMTOKENS");

    static final QName QNAME_TYPE_STRING = new QName(NS_SOAP_ENCODING, "string");
    static final QName QNAME_TYPE_NORMALIZED_STRING = new QName(NS_SOAP_ENCODING, "normalizedString");
    static final QName QNAME_TYPE_TOKEN = new QName(NS_SOAP_ENCODING, "token");
    static final QName QNAME_TYPE_BYTE = new QName(NS_SOAP_ENCODING, "byte");
    static final QName QNAME_TYPE_UNSIGNED_BYTE = new QName(NS_SOAP_ENCODING, "unsignedByte");
    static final QName QNAME_TYPE_BASE64_BINARY = new QName(NS_SOAP_ENCODING, "base64Binary");
    static final QName QNAME_TYPE_HEX_BINARY = new QName(NS_SOAP_ENCODING, "hexBinary");
    static final QName QNAME_TYPE_INTEGER = new QName(NS_SOAP_ENCODING, "integer");
    static final QName QNAME_TYPE_POSITIVE_INTEGER = new QName(NS_SOAP_ENCODING, "positiveInteger");
    static final QName QNAME_TYPE_NEGATIVE_INTEGER = new QName(NS_SOAP_ENCODING, "negativeInteger");
    static final QName QNAME_TYPE_NON_NEGATIVE_INTEGER = new QName(NS_SOAP_ENCODING, "nonNegativeInteger");
    static final QName QNAME_TYPE_NON_POSITIVE_INTEGER = new QName(NS_SOAP_ENCODING, "nonPositiveInteger");
    static final QName QNAME_TYPE_INT = new QName(NS_SOAP_ENCODING, "int");
    static final QName QNAME_TYPE_UNSIGNED_INT = new QName(NS_SOAP_ENCODING, "unsignedInt");
    static final QName QNAME_TYPE_LONG = new QName(NS_SOAP_ENCODING, "long");
    static final QName QNAME_TYPE_UNSIGNED_LONG = new QName(NS_SOAP_ENCODING, "unsignedLong");
    static final QName QNAME_TYPE_SHORT = new QName(NS_SOAP_ENCODING, "short");
    static final QName QNAME_TYPE_UNSIGNED_SHORT = new QName(NS_SOAP_ENCODING, "unsignedShort");
    static final QName QNAME_TYPE_DECIMAL = new QName(NS_SOAP_ENCODING, "decimal");
    static final QName QNAME_TYPE_FLOAT = new QName(NS_SOAP_ENCODING, "float");
    static final QName QNAME_TYPE_DOUBLE = new QName(NS_SOAP_ENCODING, "double");
    static final QName QNAME_TYPE_BOOLEAN = new QName(NS_SOAP_ENCODING, "boolean");
    static final QName QNAME_TYPE_TIME = new QName(NS_SOAP_ENCODING, "time");
    static final QName QNAME_TYPE_DATE_TIME = new QName(NS_SOAP_ENCODING, "dateTime");
    static final QName QNAME_TYPE_DURATION = new QName(NS_SOAP_ENCODING, "duration");
    static final QName QNAME_TYPE_DATE = new QName(NS_SOAP_ENCODING, "date");
    static final QName QNAME_TYPE_G_MONTH = new QName(NS_SOAP_ENCODING, "gMonth");
    static final QName QNAME_TYPE_G_YEAR = new QName(NS_SOAP_ENCODING, "gYear");
    static final QName QNAME_TYPE_G_YEAR_MONTH = new QName(NS_SOAP_ENCODING, "gYearMonth");
    static final QName QNAME_TYPE_G_DAY = new QName(NS_SOAP_ENCODING, "gDay");
    static final QName QNAME_TYPE_G_MONTH_DAY = new QName(NS_SOAP_ENCODING, "gMonthDay");
    static final QName QNAME_TYPE_NAME = new QName(NS_SOAP_ENCODING, "Name");
    static final QName QNAME_TYPE_QNAME = new QName(NS_SOAP_ENCODING, "QName");
    static final QName QNAME_TYPE_NCNAME = new QName(NS_SOAP_ENCODING, "NCName");
    static final QName QNAME_TYPE_ANY_URI = new QName(NS_SOAP_ENCODING, "anyURI");
    static final QName QNAME_TYPE_ID = new QName(NS_SOAP_ENCODING, "ID");
    static final QName QNAME_TYPE_IDREF = new QName(NS_SOAP_ENCODING, "IDREF");
    static final QName QNAME_TYPE_IDREFS = new QName(NS_SOAP_ENCODING, "IDREFS");
    static final QName QNAME_TYPE_ENTITY = new QName(NS_SOAP_ENCODING, "ENTITY");
    static final QName QNAME_TYPE_ENTITIES = new QName(NS_SOAP_ENCODING, "ENTITIES");
    static final QName QNAME_TYPE_NOTATION = new QName(NS_SOAP_ENCODING, "NOTATION");
    static final QName QNAME_TYPE_NMTOKEN = new QName(NS_SOAP_ENCODING, "NMTOKEN");
    static final QName QNAME_TYPE_NMTOKENS = new QName(NS_SOAP_ENCODING, "NMTOKENS");
    static final QName QNAME_TYPE_LANGUAGE = new QName(NS_SOAP_ENCODING, "LANGUAGE");

    // SOAP attributes with non-colonized names
    static final QName QNAME_ATTR_ID = new QName("", "id");
    static final QName QNAME_ATTR_HREF = new QName("", "ref");
}
