/*
 * $Id: HTTPAddress.java,v 1.2 2005-07-18 18:14:14 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.http;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A HTTP address extension.
 *
 * @author WS Development Team
 */
public class HTTPAddress extends Extension {

    public HTTPAddress() {
    }

    public QName getElementName() {
        return HTTPConstants.QNAME_ADDRESS;
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
