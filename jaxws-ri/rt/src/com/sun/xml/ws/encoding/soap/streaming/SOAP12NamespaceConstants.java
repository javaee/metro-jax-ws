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

package com.sun.xml.ws.encoding.soap.streaming;

/**
 * @author WS Development Team
 */
public class SOAP12NamespaceConstants {
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";
    public static final String ENVELOPE =
        "http://www.w3.org/2003/05/soap-envelope";
    public static final String ENCODING =
        "http://www.w3.org/2003/05/soap-encoding";
    public static final String SOAP_RPC = "http://www.w3.org/2002/06/soap-rpc";
    public static final String XSD = "http://www.w3.org/2001/XMLSchema";
    public static final String XSI =
        "http://www.w3.org/2001/XMLSchema-instance";
    public static final String TRANSPORT_HTTP =
        "http://www.w3.org/2003/05/soap/bindings/HTTP/";
    
    public static final String ACTOR_NEXT = "http://www.w3.org/2003/05/soap-envelope/role/next";
        
    public static final String ROLE_NEXT =
        "http://www.w3.org/2003/05/soap-envelope/role/next";
    public static final String ROLE_NONE = "http://www.w3.org/2003/05/soap-envelope/role/none";
    public static final String ROLE_ULTIMATE_RECEIVER = "http://www.w3.org/2003/05/soap-envelope/role/ultimateReceiver"; 
    
    public static final String SOAP_UPGRADE =
        "http://www.w3.org/2002/06/soap-upgrade";

    public static final String TAG_ENVELOPE = "Envelope";
    public static final String TAG_HEADER = "Header";
    public static final String TAG_BODY = "Body";
    public static final String TAG_RESULT = "result";
    public static final String TAG_NOT_UNDERSTOOD = "NotUnderstood";

    public static final String ATTR_ACTOR = "role";
    public static final String ATTR_MUST_UNDERSTAND = "mustUnderstand";
    public static final String ATTR_MISUNDERSTOOD = "missUnderstood";
    public static final String ATTR_ENCODING_STYLE = "encodingStyle";   
    public static final String ATTR_NOT_UNDERSTOOD_QNAME = "qname";
}
