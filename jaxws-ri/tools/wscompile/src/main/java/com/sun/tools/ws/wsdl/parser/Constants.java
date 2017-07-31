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

package com.sun.tools.ws.wsdl.parser;

/**
 * An interface defining constants needed to read and write WSDL documents.
 *
 * @author WS Development Team
 */
public interface Constants {
    // WSDL element tags
    static final String TAG_BINDING = "binding";
    static final String TAG_DEFINITIONS = "definitions";
    static final String TAG_DOCUMENTATION = "documentation";
    static final String TAG_MESSAGE = "message";
    static final String TAG_PART = "part";
    static final String TAG_PORT_TYPE = "portType";
    static final String TAG_TYPES = "types";
    static final String TAG_OPERATION = "operation";
    static final String TAG_INPUT = "input";
    static final String TAG_OUTPUT = "output";
    static final String TAG_FAULT = "fault";
    static final String TAG_SERVICE = "service";
    static final String TAG_PORT = "port";
    static final String TAG_ = "";

    // WSDL attribute names
    static final String ATTR_ELEMENT = "element";
    static final String ATTR_NAME = "name";
    static final String ATTR_REQUIRED = "required";
    static final String ATTR_TARGET_NAMESPACE = "targetNamespace";
    static final String ATTR_TYPE = "type";
    static final String ATTR_MESSAGE = "message";
    static final String ATTR_BINDING = "binding";
    static final String ATTR_LOCATION = "location";
    static final String ATTR_TRANSPORT = "transport";
    static final String ATTR_STYLE = "style";
    static final String ATTR_USE = "use";
    static final String ATTR_NAMESPACE = "namespace";
    static final String ATTR_ENCODING_STYLE = "encodingStyle";
    static final String ATTR_PART = "part";
    static final String ATTR_PARTS = "parts";
    static final String ATTR_SOAP_ACTION = "soapAction";
    static final String ATTR_PARAMETER_ORDER = "parameterOrder";
    static final String ATTR_VERB = "verb";

    // schema attribute names
    static final String ATTR_ID = "id";
    static final String ATTR_VERSION = "version";
    static final String ATTR_ATTRIBUTE_FORM_DEFAULT = "attributeFormDefault";
    static final String ATTR_BLOCK_DEFAULT = "blockDefault";
    static final String ATTR_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    static final String ATTR_FINAL_DEFAULT = "finalDefault";
    static final String ATTR_ABSTRACT = "abstract";
    static final String ATTR_NILLABLE = "nillable";
    static final String ATTR_DEFAULT = "default";
    static final String ATTR_FIXED = "fixed";
    static final String ATTR_FORM = "form";
    static final String ATTR_BLOCK = "block";
    static final String ATTR_FINAL = "final";
    static final String ATTR_REF = "ref";
    static final String ATTR_SUBSTITUTION_GROUP = "substitutionGroup";
    static final String ATTR_MIN_OCCURS = "minOccurs";
    static final String ATTR_MAX_OCCURS = "maxOccurs";
    static final String ATTR_PROCESS_CONTENTS = "processContents";
    static final String ATTR_MIXED = "mixed";
    static final String ATTR_BASE = "base";
    static final String ATTR_VALUE = "value";
    static final String ATTR_XPATH = "xpath";
    static final String ATTR_SCHEMA_LOCATION = "schemaLocation";
    static final String ATTR_REFER = "refer";
    static final String ATTR_ITEM_TYPE = "itemType";
    static final String ATTR_PUBLIC = "public";
    static final String ATTR_SYSTEM = "system";
    static final String ATTR_MEMBER_TYPES = "memberTypes";
    static final String ATTR_ = "";

    // WSDL attribute values
    static final String ATTRVALUE_RPC = "rpc";
    static final String ATTRVALUE_DOCUMENT = "document";
    static final String ATTRVALUE_LITERAL = "literal";
    static final String ATTRVALUE_ENCODED = "encoded";

    // schema attribute values
    static final String ATTRVALUE_QUALIFIED = "qualified";
    static final String ATTRVALUE_UNQUALIFIED = "unqualified";
    static final String ATTRVALUE_ALL = "#all";
    static final String ATTRVALUE_SUBSTITUTION = "substitution";
    static final String ATTRVALUE_EXTENSION = "extension";
    static final String ATTRVALUE_RESTRICTION = "restriction";
    static final String ATTRVALUE_LIST = "list";
    static final String ATTRVALUE_UNION = "union";
    static final String ATTRVALUE_UNBOUNDED = "unbounded";
    static final String ATTRVALUE_PROHIBITED = "prohibited";
    static final String ATTRVALUE_OPTIONAL = "optional";
    static final String ATTRVALUE_REQUIRED = "required";
    static final String ATTRVALUE_LAX = "lax";
    static final String ATTRVALUE_SKIP = "skip";
    static final String ATTRVALUE_STRICT = "strict";
    static final String ATTRVALUE_ANY = "##any";
    static final String ATTRVALUE_LOCAL = "##local";
    static final String ATTRVALUE_OTHER = "##other";
    static final String ATTRVALUE_TARGET_NAMESPACE = "##targetNamespace";
    static final String ATTRVALUE_ = "";

    // namespace URIs
    static final String NS_XML = "http://www.w3.org/XML/1998/namespace";
    static final String NS_XMLNS = "http://www.w3.org/2000/xmlns/";
    static final String NS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
    static final String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
    static final String NS_WSDL_SOAP12 = "http://schemas.xmlsoap.org/wsdl/soap12/";
    static final String NS_WSDL_HTTP = "http://schemas.xmlsoap.org/wsdl/http/";
    static final String NS_WSDL_MIME = "http://schemas.xmlsoap.org/wsdl/mime/";
    static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    static final String NS_ = "";

    // other constants
    static final String XMLNS = "xmlns";
    static final String TRUE = "true";
    static final String FALSE = "false";
}
