/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.encoding.soap;

import com.sun.xml.ws.encoding.soap.streaming.SOAP12NamespaceConstants;

import javax.xml.namespace.QName;

/**
 *
 * @author WS Development Team
 */
public class SOAP12Constants {

    public static final String URI_ENVELOPE = SOAP12NamespaceConstants.ENVELOPE;
    public static final String URI_ENCODING = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String URI_HTTP = SOAP12NamespaceConstants.TRANSPORT_HTTP;
    public static final String URI_SOAP_RPC = SOAP12NamespaceConstants.SOAP_RPC;

    
    public static final QName QNAME_SOAP_RPC = new QName(URI_SOAP_RPC, "rpc");
    public static final QName QNAME_SOAP_RESULT = new QName(URI_SOAP_RPC, "result");
    
    public static final QName QNAME_SOAP_ENVELOPE    = new QName(URI_ENVELOPE, "Envelope");
    public static final QName QNAME_SOAP_BODY    = new QName(URI_ENVELOPE, "Body");
    public static final QName QNAME_SOAP_HEADER    = new QName(URI_ENVELOPE, "Header");
    public static final QName QNAME_ENVELOPE_ENCODINGSTYLE = new QName(URI_ENVELOPE, "encodingStyle");
    public static final QName QNAME_SOAP_FAULT             = new QName(URI_ENVELOPE, "Fault");
    public static final QName QNAME_MUSTUNDERSTAND         = new QName(URI_ENVELOPE, "mustUnderstand");
    public static final QName QNAME_ROLE                   = new QName(URI_ENVELOPE, "role");

    public static final QName QNAME_NOT_UNDERSTOOD         = new QName(URI_ENVELOPE, "NotUnderstood");
    
    //fault
    public static final QName QNAME_FAULT_CODE             = new QName(URI_ENVELOPE, "Code");
    public static final QName QNAME_FAULT_SUBCODE          = new QName(URI_ENVELOPE, "Subcode");
    public static final QName QNAME_FAULT_VALUE            = new QName(URI_ENVELOPE, "Value");
    public static final QName QNAME_FAULT_REASON           = new QName(URI_ENVELOPE, "Reason");
    public static final QName QNAME_FAULT_NODE             = new QName(URI_ENVELOPE, "Node");
    public static final QName QNAME_FAULT_ROLE             = new QName(URI_ENVELOPE, "Role");
    public static final QName QNAME_FAULT_DETAIL           = new QName(URI_ENVELOPE, "Detail");
    public static final QName QNAME_FAULT_REASON_TEXT             = new QName(URI_ENVELOPE, "Text");
    public final static QName QNAME_UPGRADE                = new QName(URI_ENVELOPE, "Upgrade");
    public final static QName QNAME_UPGRADE_SUPPORTED_ENVELOPE           = new QName(URI_ENVELOPE, "SupportedEnvelope");
    
    
    //fault codes
    public final static QName FAULT_CODE_MUST_UNDERSTAND   = new QName(URI_ENVELOPE, "MustUnderstand");
    public final static QName FAULT_CODE_MISUNDERSTOOD   = new QName(URI_ENVELOPE, "Misunderstood");
    public final static QName FAULT_CODE_VERSION_MISMATCH  = new QName(URI_ENVELOPE, "VersionMismatch");
    public final static QName FAULT_CODE_DATA_ENCODING_UNKNOWN = new QName(URI_ENVELOPE, "DataEncodingUnknown");
    public final static QName FAULT_CODE_PROCEDURE_NOT_PRESENT = new QName(URI_ENVELOPE, "ProcedureNotPresent");
    public final static QName FAULT_CODE_BAD_ARGUMENTS      = new QName(URI_ENVELOPE, "BadArguments");
}
