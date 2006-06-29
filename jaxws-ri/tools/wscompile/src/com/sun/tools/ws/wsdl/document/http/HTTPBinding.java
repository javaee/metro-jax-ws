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

package com.sun.tools.ws.wsdl.document.http;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A HTTP binding extension.
 *
 * @author WS Development Team
 */
public class HTTPBinding extends Extension {

    public HTTPBinding() {
    }

    public QName getElementName() {
        return HTTPConstants.QNAME_BINDING;
    }

    public String getVerb() {
        return _verb;
    }

    public void setVerb(String s) {
        _verb = s;
    }

    public void validateThis() {
        if (_verb == null) {
            failValidation("validation.missingRequiredAttribute", "verb");
        }
    }

    private String _verb;
}
