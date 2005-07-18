/*
 * $Id: HTTPUrlEncoded.java,v 1.2 2005-07-18 18:14:14 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.http;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A HTTP urlEncoded extension.
 *
 * @author WS Development Team
 */
public class HTTPUrlEncoded extends Extension {

    public HTTPUrlEncoded() {}

    public QName getElementName() {
        return HTTPConstants.QNAME_URL_ENCODED;
    }

    public void validateThis() {
    }
}
