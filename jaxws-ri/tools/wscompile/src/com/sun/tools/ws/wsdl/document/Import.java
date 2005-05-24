/*
 * $Id: Import.java,v 1.1 2005-05-24 14:00:45 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Entity;

/**
 * Entity corresponding to the "import" WSDL element.
 *
 * @author JAX-RPC Development Team
 */
public class Import extends Entity{

    public Import() {
    }

    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String s) {
        _namespace = s;
    }

    public String getLocation() {
        return _location;
    }

    public void setLocation(String s) {
        _location = s;
    }

    public QName getElementName() {
        return WSDLConstants.QNAME_IMPORT;
    }

    public Documentation getDocumentation() {
        return _documentation;
    }

    public void setDocumentation(Documentation d) {
        _documentation = d;
    }

    public void accept(WSDLDocumentVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public void validateThis() {
        if (_location == null) {
            failValidation("validation.missingRequiredAttribute", "location");
        }
        if (_namespace == null) {
            failValidation("validation.missingRequiredAttribute", "namespace");
        }
    }

    private Documentation _documentation;
    private String _location;
    private String _namespace;
}
