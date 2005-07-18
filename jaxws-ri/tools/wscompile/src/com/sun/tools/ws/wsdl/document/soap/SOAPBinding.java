/*
 * $Id: SOAPBinding.java,v 1.2 2005-07-18 18:14:17 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A SOAP binding extension.
 *
 * @author WS Development Team
 */
public class SOAPBinding extends Extension {

    public SOAPBinding() {
        _style = SOAPStyle.DOCUMENT;
    }

    public QName getElementName() {
        return SOAPConstants.QNAME_BINDING;
    }

    public String getTransport() {
        return _transport;
    }

    public void setTransport(String s) {
        _transport = s;
    }

    public SOAPStyle getStyle() {
        return _style;
    }

    public void setStyle(SOAPStyle s) {
        _style = s;
    }

    public boolean isDocument() {
        return _style == SOAPStyle.DOCUMENT;
    }

    public boolean isRPC() {
        return _style == SOAPStyle.RPC;
    }

    public void validateThis() {
    }

    private String _transport;
    private SOAPStyle _style;
}
