/*
 * $Id: SOAPOperation.java,v 1.2 2005-07-18 18:14:18 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.soap;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.framework.Extension;

/**
 * A SOAP operation extension.
 *
 * @author WS Development Team
 */
public class SOAPOperation extends Extension {

    public SOAPOperation() {
    }

    public QName getElementName() {
        return SOAPConstants.QNAME_OPERATION;
    }

    public String getSOAPAction() {
        return _soapAction;
    }

    public void setSOAPAction(String s) {
        _soapAction = s;
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

    private String _soapAction;
    private SOAPStyle _style;
}
