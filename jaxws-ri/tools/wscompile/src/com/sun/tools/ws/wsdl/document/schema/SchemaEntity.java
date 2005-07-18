/*
 * $Id: SchemaEntity.java,v 1.2 2005-07-18 18:14:16 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
