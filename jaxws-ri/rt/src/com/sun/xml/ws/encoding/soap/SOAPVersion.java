/*
 *$Id: SOAPVersion.java,v 1.5 2005-09-10 19:47:40 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.encoding.soap;

import javax.xml.ws.soap.SOAPBinding;


/**
 * @author WS Development Team
 */

public class SOAPVersion {

    private final String version;

    private SOAPVersion(String ver) {
        this.version = ver;
    }

    public String toString() {
        return this.version;
    }

    public boolean equals(String strVersion) {
        return version.equals(strVersion);
    }

    public static final SOAPVersion SOAP_11 = new SOAPVersion(SOAPBinding.SOAP11HTTP_BINDING);
    public static final SOAPVersion SOAP_12 = new SOAPVersion(SOAPBinding.SOAP12HTTP_BINDING);
}
