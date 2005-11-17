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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.EntityAction;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ExtensionVisitor;
import com.sun.tools.ws.wsdl.framework.QNameAction;

/**
 * A SOAP header extension.
 *
 * @author WS Development Team
 */
public class SOAPHeader extends Extension {

    public SOAPHeader() {
        _faults = new ArrayList();
    }

    public void add(SOAPHeaderFault fault) {
        _faults.add(fault);
    }

    public Iterator faults() {
        return _faults.iterator();
    }

    public QName getElementName() {
        return SOAPConstants.QNAME_HEADER;
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

    public void withAllSubEntitiesDo(EntityAction action) {
        super.withAllSubEntitiesDo(action);

        for (Iterator iter = _faults.iterator(); iter.hasNext();) {
            action.perform((Entity) iter.next());
        }
    }

    public void withAllQNamesDo(QNameAction action) {
        super.withAllQNamesDo(action);

        if (_message != null) {
            action.perform(_message);
        }
    }

    public void accept(ExtensionVisitor visitor) throws Exception {
        visitor.preVisit(this);
        for (Iterator iter = _faults.iterator(); iter.hasNext();) {
            ((SOAPHeaderFault) iter.next()).accept(visitor);
        }
        visitor.postVisit(this);
    }

    public void validateThis() {
        if (_message == null) {
            failValidation("validation.missingRequiredAttribute", "message");
        }
        if (_part == null) {
            failValidation("validation.missingRequiredAttribute", "part");
        }
        // Fix for bug 4851427
        //        if (_use == null) {
        //            failValidation("validation.missingRequiredAttribute", "use");
        //        }
    }

    private String _encodingStyle;
    private String _namespace;
    private String _part;
    private QName _message;
    private SOAPUse _use=SOAPUse.LITERAL;
    private List _faults;
}
