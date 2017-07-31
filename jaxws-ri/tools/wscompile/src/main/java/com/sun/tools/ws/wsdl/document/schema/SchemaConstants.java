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

package com.sun.tools.ws.wsdl.document.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author WS Development Team
 */
public interface SchemaConstants {

    // namespace URIs
    static final String NS_XMLNS = "http://www.w3.org/2000/xmlns/";
    static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

    // QNames
    static final QName QNAME_ALL = new QName(NS_XSD, "all");
    static final QName QNAME_ANNOTATION = new QName(NS_XSD, "annotation");
    static final QName QNAME_ANY = new QName(NS_XSD, "any");
    static final QName QNAME_ANY_ATTRIBUTE = new QName(NS_XSD, "anyAttribute");
    static final QName QNAME_ATTRIBUTE = new QName(NS_XSD, "attribute");
    static final QName QNAME_ATTRIBUTE_GROUP = new QName(NS_XSD, "attributeGroup");
    static final QName QNAME_CHOICE = new QName(NS_XSD, "choice");
    static final QName QNAME_COMPLEX_CONTENT = new QName(NS_XSD, "complexContent");
    static final QName QNAME_COMPLEX_TYPE = new QName(NS_XSD, "complexType");
    static final QName QNAME_ELEMENT = new QName(NS_XSD, "element");
    static final QName QNAME_ENUMERATION = new QName(NS_XSD, "enumeration");
    static final QName QNAME_EXTENSION = new QName(NS_XSD, "extension");
    static final QName QNAME_FIELD = new QName(NS_XSD, "field");
    static final QName QNAME_FRACTION_DIGITS = new QName(NS_XSD, "fractionDigits");
    static final QName QNAME_GROUP = new QName(NS_XSD, "group");
    static final QName QNAME_IMPORT = new QName(NS_XSD, "import");
    static final QName QNAME_INCLUDE = new QName(NS_XSD, "include");
    static final QName QNAME_KEY = new QName(NS_XSD, "key");
    static final QName QNAME_KEYREF = new QName(NS_XSD, "keyref");
    static final QName QNAME_LENGTH = new QName(NS_XSD, "length");
    static final QName QNAME_LIST = new QName(NS_XSD, "list");
    static final QName QNAME_MAX_EXCLUSIVE = new QName(NS_XSD, "maxExclusive");
    static final QName QNAME_MAX_INCLUSIVE = new QName(NS_XSD, "maxInclusive");
    static final QName QNAME_MAX_LENGTH = new QName(NS_XSD, "maxLength");
    static final QName QNAME_MIN_EXCLUSIVE = new QName(NS_XSD, "minExclusive");
    static final QName QNAME_MIN_INCLUSIVE = new QName(NS_XSD, "minInclusive");
    static final QName QNAME_MIN_LENGTH = new QName(NS_XSD, "minLength");
    static final QName QNAME_NOTATION = new QName(NS_XSD, "notation");
    static final QName QNAME_RESTRICTION = new QName(NS_XSD, "restriction");
    static final QName QNAME_PATTERN = new QName(NS_XSD, "pattern");
    static final QName QNAME_PRECISION = new QName(NS_XSD, "precision");
    static final QName QNAME_REDEFINE = new QName(NS_XSD, "redefine");
    static final QName QNAME_SCALE = new QName(NS_XSD, "scale");
    static final QName QNAME_SCHEMA = new QName(NS_XSD, "schema");
    static final QName QNAME_SELECTOR = new QName(NS_XSD, "selector");
    static final QName QNAME_SEQUENCE = new QName(NS_XSD, "sequence");
    static final QName QNAME_SIMPLE_CONTENT =
        new QName(NS_XSD, "simpleContent");
    static final QName QNAME_SIMPLE_TYPE = new QName(NS_XSD, "simpleType");
    static final QName QNAME_TOTAL_DIGITS = new QName(NS_XSD, "totalDigits");
    static final QName QNAME_UNIQUE = new QName(NS_XSD, "unique");
    static final QName QNAME_UNION = new QName(NS_XSD, "union");
    static final QName QNAME_WHITE_SPACE = new QName(NS_XSD, "whiteSpace");

    // QNames for built-in XSD types
    static final QName QNAME_TYPE_STRING = new QName(NS_XSD, "string");
    static final QName QNAME_TYPE_NORMALIZED_STRING = new QName(NS_XSD, "normalizedString");
    static final QName QNAME_TYPE_TOKEN = new QName(NS_XSD, "token");
    static final QName QNAME_TYPE_BYTE = new QName(NS_XSD, "byte");
    static final QName QNAME_TYPE_UNSIGNED_BYTE = new QName(NS_XSD, "unsignedByte");
    static final QName QNAME_TYPE_BASE64_BINARY = new QName(NS_XSD, "base64Binary");
    static final QName QNAME_TYPE_HEX_BINARY = new QName(NS_XSD, "hexBinary");
    static final QName QNAME_TYPE_INTEGER = new QName(NS_XSD, "integer");
    static final QName QNAME_TYPE_POSITIVE_INTEGER = new QName(NS_XSD, "positiveInteger");
    static final QName QNAME_TYPE_NEGATIVE_INTEGER = new QName(NS_XSD, "negativeInteger");
    static final QName QNAME_TYPE_NON_NEGATIVE_INTEGER = new QName(NS_XSD, "nonNegativeInteger");
    static final QName QNAME_TYPE_NON_POSITIVE_INTEGER = new QName(NS_XSD, "nonPositiveInteger");
    static final QName QNAME_TYPE_INT = new QName(NS_XSD, "int");
    static final QName QNAME_TYPE_UNSIGNED_INT = new QName(NS_XSD, "unsignedInt");
    static final QName QNAME_TYPE_LONG = new QName(NS_XSD, "long");
    static final QName QNAME_TYPE_UNSIGNED_LONG = new QName(NS_XSD, "unsignedLong");
    static final QName QNAME_TYPE_SHORT = new QName(NS_XSD, "short");
    static final QName QNAME_TYPE_UNSIGNED_SHORT = new QName(NS_XSD, "unsignedShort");
    static final QName QNAME_TYPE_DECIMAL = new QName(NS_XSD, "decimal");
    static final QName QNAME_TYPE_FLOAT = new QName(NS_XSD, "float");
    static final QName QNAME_TYPE_DOUBLE = new QName(NS_XSD, "double");
    static final QName QNAME_TYPE_BOOLEAN = new QName(NS_XSD, "boolean");
    static final QName QNAME_TYPE_TIME = new QName(NS_XSD, "time");
    static final QName QNAME_TYPE_DATE_TIME = new QName(NS_XSD, "dateTime");
    static final QName QNAME_TYPE_DURATION = new QName(NS_XSD, "duration");
    static final QName QNAME_TYPE_DATE = new QName(NS_XSD, "date");
    static final QName QNAME_TYPE_G_MONTH = new QName(NS_XSD, "gMonth");
    static final QName QNAME_TYPE_G_YEAR = new QName(NS_XSD, "gYear");
    static final QName QNAME_TYPE_G_YEAR_MONTH = new QName(NS_XSD, "gYearMonth");
    static final QName QNAME_TYPE_G_DAY = new QName(NS_XSD, "gDay");
    static final QName QNAME_TYPE_G_MONTH_DAY = new QName(NS_XSD, "gMonthDay");
    static final QName QNAME_TYPE_NAME = new QName(NS_XSD, "Name");
    static final QName QNAME_TYPE_QNAME = new QName(NS_XSD, "QName");
    static final QName QNAME_TYPE_NCNAME = new QName(NS_XSD, "NCName");
    static final QName QNAME_TYPE_ANY_URI = new QName(NS_XSD, "anyURI");
    static final QName QNAME_TYPE_ID = new QName(NS_XSD, "ID");
    static final QName QNAME_TYPE_IDREF = new QName(NS_XSD, "IDREF");
    static final QName QNAME_TYPE_IDREFS = new QName(NS_XSD, "IDREFS");
    static final QName QNAME_TYPE_ENTITY = new QName(NS_XSD, "ENTITY");
    static final QName QNAME_TYPE_ENTITIES = new QName(NS_XSD, "ENTITIES");
    static final QName QNAME_TYPE_NOTATION = new QName(NS_XSD, "NOTATION");
    static final QName QNAME_TYPE_NMTOKEN = new QName(NS_XSD, "NMTOKEN");
    static final QName QNAME_TYPE_NMTOKENS = new QName(NS_XSD, "NMTOKENS");

    static final QName QNAME_TYPE_LANGUAGE = new QName(NS_XSD, "language");

    // QNames for special types
    static final QName QNAME_TYPE_URTYPE = new QName(NS_XSD, "anyType");
    static final QName QNAME_TYPE_SIMPLE_URTYPE = new QName(NS_XSD, "anySimpleType");
}
