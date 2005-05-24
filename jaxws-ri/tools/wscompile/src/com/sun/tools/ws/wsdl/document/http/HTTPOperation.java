/*
 * $Id: HTTPOperation.java,v 1.1 2005-05-24 13:53:25 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.http;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A HTTP operation extension.
 *
 * @author JAX-RPC Development Team
 */
public class HTTPOperation extends Extension {

    public HTTPOperation() {
    }

    public QName getElementName() {
        return HTTPConstants.QNAME_OPERATION;
    }

    public String getLocation() {
        return _location;
    }

    public void setLocation(String s) {
        _location = s;
    }

    public void validateThis() {
        if (_location == null) {
            failValidation("validation.missingRequiredAttribute", "location");
        }
    }

    private String _location;
}
