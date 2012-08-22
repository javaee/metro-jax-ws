/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
    public static final QName QNAME_SOAP12OPERATION =
        new QName(NS_WSDL_SOAP12, "operation"); 
    public static final QName QNAME_MUSTUNDERSTAND =
        new QName(URI_ENVELOPE, "mustUnderstand");


}
