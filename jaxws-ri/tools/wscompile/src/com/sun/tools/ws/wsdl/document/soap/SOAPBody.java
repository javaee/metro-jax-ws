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

package com.sun.tools.ws.wsdl.document.soap;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A SOAP body extension.
 *
 * @author WS Development Team
 */
public class SOAPBody extends Extension {

    public SOAPBody() {
    }

    public QName getElementName() {
        return SOAPConstants.QNAME_BODY;
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

    public String getParts() {
        return _parts;
    }

    public void setParts(String s) {
        _parts = s;
    }

    public void validateThis() {
    }

    private String _encodingStyle;
    private String _namespace;
    private String _parts;
    private SOAPUse _use=SOAPUse.LITERAL;
}
