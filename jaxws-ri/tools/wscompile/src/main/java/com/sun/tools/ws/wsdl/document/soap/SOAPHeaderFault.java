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

package com.sun.tools.ws.wsdl.document.soap;

import com.sun.tools.ws.wsdl.framework.ExtensionImpl;
import com.sun.tools.ws.wsdl.framework.QNameAction;
import com.sun.tools.ws.wsdl.framework.ValidationException;
import org.xml.sax.Locator;

import javax.xml.namespace.QName;

/**
 * A SOAP header fault extension.
 *
 * @author WS Development Team
 */
public class SOAPHeaderFault extends ExtensionImpl {

    public SOAPHeaderFault(Locator locator) {
        super(locator);
    }

    public QName getElementName() {
        return SOAPConstants.QNAME_HEADERFAULT;
    }

    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String s) {
        _namespace = s;
    }

    public SOAPUse getUse() {
        return _use;
    }

    public void setUse(SOAPUse u) {
        _use = u;
    }

    public boolean isEncoded() {
        return _use == SOAPUse.ENCODED;
    }

    public boolean isLiteral() {
        return _use == SOAPUse.LITERAL;
    }

    public String getEncodingStyle() {
        return _encodingStyle;
    }

    public void setEncodingStyle(String s) {
        _encodingStyle = s;
    }

    public String getPart() {
        return _part;
    }

    public void setMessage(QName message) {
        _message = message;
    }

    public QName getMessage() {
        return _message;
    }

    public void setPart(String s) {
        _part = s;
    }

    public void withAllQNamesDo(QNameAction action) {
        super.withAllQNamesDo(action);

        if (_message != null) {
            action.perform(_message);
        }
    }

    public void validateThis() {
        if (_message == null) {
            failValidation("validation.missingRequiredAttribute", "message");
        }
        if (_part == null) {
            failValidation("validation.missingRequiredAttribute", "part");
        }
        if(_use == SOAPUse.ENCODED) {
            throw new ValidationException("validation.unsupportedUse.encoded", getLocator().getLineNumber(),getLocator().getSystemId());
        }
    }

    private String _encodingStyle;
    private String _namespace;
    private String _part;
    private QName _message;
    private SOAPUse _use=SOAPUse.LITERAL;
}
