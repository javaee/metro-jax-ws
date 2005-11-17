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

package com.sun.tools.ws.wsdl.document.mime;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A MIME content extension.
 *
 * @author WS Development Team
 */
public class MIMEContent extends Extension {

    public MIMEContent() {
    }

    public QName getElementName() {
        return MIMEConstants.QNAME_CONTENT;
    }

    public String getPart() {
        return _part;
    }

    public void setPart(String s) {
        _part = s;
    }

    public String getType() {
        return _type;
    }

    public void setType(String s) {
        _type = s;
    }

    public void validateThis() {
    }

    private String _part;
    private String _type;
}
