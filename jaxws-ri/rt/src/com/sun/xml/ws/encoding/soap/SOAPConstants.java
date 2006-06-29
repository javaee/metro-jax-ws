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


package com.sun.xml.ws.encoding.soap;

import javax.xml.namespace.QName;

import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

/**
 *
 * @author WS Development Team
 */
public class SOAPConstants {

    public static final String URI_ENVELOPE = SOAPNamespaceConstants.ENVELOPE;
    public static final String URI_HTTP = SOAPNamespaceConstants.TRANSPORT_HTTP;
    public static final String URI_ENCODING  = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String NS_WSDL_SOAP = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final QName QNAME_ENVELOPE_ENCODINGSTYLE = new QName(URI_ENVELOPE, "encodingStyle");

    public final static QName QNAME_SOAP_ENVELOPE             = new QName(URI_ENVELOPE, "Envelope");
    public final static QName QNAME_SOAP_HEADER             = new QName(URI_ENVELOPE, "Header");
    public static final QName QNAME_MUSTUNDERSTAND         = new QName(URI_ENVELOPE, "mustUnderstand");
    public static final QName QNAME_ROLE                   = new QName(URI_ENVELOPE, "actor");
    public final static QName QNAME_SOAP_BODY             = new QName(URI_ENVELOPE, "Body");
    public final static QName QNAME_SOAP_FAULT             = new QName(URI_ENVELOPE, "Fault");
    public final static QName QNAME_SOAP_FAULT_CODE             = new QName("", "faultcode");
    public final static QName QNAME_SOAP_FAULT_STRING             = new QName("", "faultstring");
    public final static QName QNAME_SOAP_FAULT_ACTOR             = new QName("", "faultactor");
    public final static QName QNAME_SOAP_FAULT_DETAIL             = new QName("", "detail");
    public final static QName FAULT_CODE_CLIENT            = new QName(URI_ENVELOPE, "Client");
    public final static QName FAULT_CODE_SERVER            = new QName(URI_ENVELOPE, "Server");
    public final static QName FAULT_CODE_MUST_UNDERSTAND   = new QName(URI_ENVELOPE, "MustUnderstand");

    public final static QName FAULT_CODE_VERSION_MISMATCH  = new QName(URI_ENVELOPE, "VersionMismatch");
    public final static QName FAULT_CODE_DATA_ENCODING_UNKNOWN = new QName(URI_ENVELOPE, "DataEncodingUnknown");
    public final static QName FAULT_CODE_PROCEDURE_NOT_PRESENT = new QName(URI_ENVELOPE, "ProcedureNotPresent");
    public final static QName FAULT_CODE_BAD_ARGUMENTS      = new QName(URI_ENVELOPE, "BadArguments");
}
