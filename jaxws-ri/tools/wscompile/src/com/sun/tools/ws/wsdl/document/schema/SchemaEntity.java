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

package com.sun.tools.ws.wsdl.document.schema;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Defining;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.GloballyKnown;
import com.sun.tools.ws.wsdl.framework.Kind;

/**
 *
 * @author WS Development Team
 */
public class SchemaEntity extends Entity implements GloballyKnown {

    public SchemaEntity(
        Schema parent,
        SchemaElement element,
        Kind kind,
        QName name) {
        _parent = parent;
        _element = element;
        _kind = kind;
        _name = name;
    }

    public SchemaElement getElement() {
        return _element;
    }

    public QName getElementName() {
        return _element.getQName();
    }

    public String getName() {
        return _name.getLocalPart();
    }

    public Kind getKind() {
        return _kind;
    }

    public Schema getSchema() {
        return _parent;
    }

    public Defining getDefining() {
        return _parent;
    }

    public void validateThis() {
        // do nothing
    }

    private Schema _parent;
    private SchemaElement _element;
    private Kind _kind;
    private QName _name;
}
