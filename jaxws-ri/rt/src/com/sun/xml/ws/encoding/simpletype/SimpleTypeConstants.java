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


package com.sun.xml.ws.encoding.simpletype;

import javax.xml.namespace.QName;

import com.sun.xml.ws.encoding.soap.SerializerConstants;
import com.sun.xml.ws.encoding.soap.streaming.SOAPNamespaceConstants;

/**
 *
 * @author WS Development Team
 */
public interface SimpleTypeConstants extends SerializerConstants {

    public static final String URI_XSI = SOAPNamespaceConstants.XSI;
    public static final String URI_XSD = SOAPNamespaceConstants.XSD;

    public static final QName QNAME_XSI_TYPE = new QName(URI_XSI, "type");
    public static final QName QNAME_XSI_NIL = new QName(URI_XSI, "nil");

}
