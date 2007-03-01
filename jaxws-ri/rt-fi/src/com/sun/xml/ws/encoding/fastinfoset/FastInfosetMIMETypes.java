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
package com.sun.xml.ws.encoding.fastinfoset;

/**
 * MIME types for Infosets encoded as fast infoset documents.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FastInfosetMIMETypes {
    /**
     * MIME type for a generic Infoset encoded as a fast infoset document.
     */
    static public final String INFOSET = "application/fastinfoset";
    /**
     * MIME type for a SOAP 1.1 Infoset encoded as a fast infoset document.
     */
    static public final String SOAP_11 = "application/fastinfoset";
    /**
     * MIME type for a SOAP 1.2 Infoset encoded as a fast infoset document.
     */
    static public final String SOAP_12 = "application/soap+fastinfoset";    
    
    /**
     * MIME type for a generic Infoset encoded as a stateful fast infoset document.
     */
    static public final String STATEFUL_INFOSET = "application/vnd.sun.stateful.fastinfoset";
    /**
     * MIME type for a SOAP 1.1 Infoset encoded as a stateful fast infoset document.
     */
    static public final String STATEFUL_SOAP_11 = "application/vnd.sun.stateful.fastinfoset";
    /**
     * MIME type for a SOAP 1.2 Infoset encoded as a stateful fast infoset document.
     */
    static public final String STATEFUL_SOAP_12 = "application/vnd.sun.stateful.soap+fastinfoset";    
}
