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

package com.sun.xml.ws.addressing;

import javax.xml.namespace.QName;

/**
 * Constants for W3C Addressing Metadata specification
 * @author Rama Pulavarthi
 */
public class W3CAddressingMetadataConstants {
    public static final String WSAM_NAMESPACE_NAME = "http://www.w3.org/2007/05/addressing/metadata";
    public static final String WSAM_PREFIX_NAME = "wsam";
    public static final QName  WSAM_ACTION_QNAME = new QName(WSAM_NAMESPACE_NAME,"Action",WSAM_PREFIX_NAME);

    public static final String WSAM_ADDRESSING_ASSERTION_NAME="Addressing";
    public static final String WSAM_ANONYMOUS_NESTED_ASSERTION_NAME="AnonymousResponses";
    public static final String WSAM_NONANONYMOUS_NESTED_ASSERTION_NAME="NonAnonymousResponses";

    public static final QName WSAM_ADDRESSING_ASSERTION = new QName(WSAM_NAMESPACE_NAME,
                    WSAM_ADDRESSING_ASSERTION_NAME, WSAM_PREFIX_NAME );

    public static final QName WSAM_ANONYMOUS_NESTED_ASSERTION = new QName(WSAM_NAMESPACE_NAME,
                        WSAM_ANONYMOUS_NESTED_ASSERTION_NAME, WSAM_PREFIX_NAME );

    public static final QName WSAM_NONANONYMOUS_NESTED_ASSERTION = new QName(WSAM_NAMESPACE_NAME,
                        WSAM_NONANONYMOUS_NESTED_ASSERTION_NAME, WSAM_PREFIX_NAME );

    public static final String WSAM_WSDLI_ATTRIBUTE_NAMESPACE="http://www.w3.org/ns/wsdl-instance";
    public static final String WSAM_WSDLI_ATTRIBUTE_PREFIX="wsdli";
    public static final String WSAM_WSDLI_ATTRIBUTE_LOCALNAME="wsdlLocation";

}
